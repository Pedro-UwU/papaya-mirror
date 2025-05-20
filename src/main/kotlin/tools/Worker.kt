package ar.edu.itba.pf.tools

import ar.edu.itba.pf.tools.contenttype.ContentTypeHandler
import ar.edu.itba.pf.tools.contexts.buildOutputs
import ar.edu.itba.pf.types.*
import ar.edu.itba.pf.types.graph.DependencyGraph
import ar.edu.itba.pf.types.infoBlocks.ExecutionData
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import ar.edu.itba.pf.types.infoBlocks.RequestData
import ar.edu.itba.pf.types.infoBlocks.ResponseData
import ar.edu.itba.pf.types.responses.ResponseDataExtractor
import ar.edu.itba.pf.types.responses.ResponseSuccess
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.yield
import kotlinx.serialization.json.*
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class Worker(
    val queue: Channel<Context>,
    val contextRegistryQueue: Channel<Context>,
    val configuration: Configuration,
    val processingRequests: AtomicInteger,
    val infoBlockQueue: Channel<InfoBlock>,
    val contextGenerator: ContextGenerator,
    private val contentTypeHandler: ContentTypeHandler = ContentTypeHandler(),
    val dependencyGraph: DependencyGraph
) {

    private var executionId: String = ""
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json()
        }
    }

    private var running = AtomicBoolean(false)

    suspend fun start() {
        running.set(true)
        while (running.get()) {
            yield()
            processingRequests.getAndAdd(1)
            val context = queue.tryReceive().getOrNull()
            processingRequests.getAndAdd(-1)
            if (context == null) {
                continue
            }
            run(context)
        }
    }

    fun stop() {
        running.set(false)
    }

    suspend fun run(context: Context) {
        val endpoint = configuration.endpoints[context.current]
        val content = context.content
        executionId = UUID.randomUUID().toString()
        if (endpoint == null) {
            throw RuntimeException("NULL Endpoint found")
        }

        val requestUrl = endpoint.url injectParameters context
        val requestMethod = endpoint.method.toKtorMethod()
        val queryParams =
            endpoint.query.map { (name, method) -> name to method.value.injectParameters(context) }.toMap()
        val bodyParams = processJsonObject(endpoint.body, context)
        val headers = endpoint.headers.map { (name, value) -> name to value.value }.toMap()

        val startInstant = Instant.now()
        val infoBlockRequestData = RequestData(headers = headers, queryParams = queryParams, body = bodyParams)
        val response: HttpResponse?
        try {
            response = client.request(requestUrl) {
                method = requestMethod
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                queryParams.forEach { (key, value) ->
                    url.parameters.append(key, value)
                }
                bodyParams?.let {
                    contentType(getContentType(endpoint.contentType))
                    setBody(contentTypeHandler.toContentTypeString(it, endpoint.contentType))
                }
            }
        } catch (e: Exception) {
            sendExceptionOcurred(
                context,
                infoBlockRequestData,
                startInstant,
                e,
            )
            return
        }


        val infoBlockResponseData = ResponseData(
            response.status.value,
            headers = response.headers.toMap(),
            body = response.bodyAsText(),
            bodySize = response.bodyAsText().length
        )

        val stopInstant = Instant.now()

        val expectedStatus = endpoint.response?.responseCode ?: 0
        if (expectedStatus != response.status.value) {
            sendInvalidStatus(
                context,
                infoBlockRequestData,
                infoBlockResponseData,
                response,
                startInstant,
                stopInstant,
                expectedStatus
            )
            return
        }

        val executionData = ExecutionData(
            start = startInstant,
            end = stopInstant,
            success = ResponseSuccess.SUCCESS
        )
        sendInfoBlockToQueue(context, infoBlockRequestData, infoBlockResponseData, executionData)
        processResponse(endpoint, response, context)
    }

    private fun HTTPMethod.toKtorMethod(): HttpMethod =
        when (this) {
            HTTPMethod.GET -> HttpMethod.Get
            HTTPMethod.POST -> HttpMethod.Post
            HTTPMethod.PUT -> HttpMethod.Put
            HTTPMethod.PATCH -> HttpMethod.Patch
            HTTPMethod.DELETE -> HttpMethod.Delete
            HTTPMethod.HEAD -> HttpMethod.Head
        }

    private suspend fun extractFromResponse(response: HttpResponse): Map<String, String> {
        val jsonElement = Json.parseToJsonElement(response.bodyAsText())
        val extractedValues = mutableMapOf<String, String>()

        fun extractValues(element: JsonElement, currentPath: String = "") {
            when (element) {
                is JsonObject -> {
                    element.entries.forEach { (key, value) ->
                        val newPath = if (currentPath.isEmpty()) key else "$currentPath.$key"
                        extractValues(value, newPath)
                    }
                }

                is JsonArray -> {
                    element.forEachIndexed { index, value ->
                        val newPath = "$currentPath[$index]"
                        extractValues(value, newPath)
                    }
                }

                is JsonPrimitive -> {
                    // Only add primitive values that are strings, numbers, or booleans
                    when {
                        element.isString -> extractedValues[currentPath] = element.content
                        element.intOrNull != null -> extractedValues[currentPath] = element.int.toString()
                        element.longOrNull != null -> extractedValues[currentPath] = element.long.toString()
                        element.floatOrNull != null -> extractedValues[currentPath] = element.float.toString()
                        element.doubleOrNull != null -> extractedValues[currentPath] = element.double.toString()
                        element.booleanOrNull != null -> extractedValues[currentPath] = element.boolean.toString()
                    }
                }

                JsonNull -> {}
            }
        }
        extractValues(jsonElement)
        return extractedValues
    }

    private fun getContentType(contentType: String): ContentType {
        val types = contentType.split("/")
        return ContentType(types[0], types[1])
    }

    private fun createNewContexts(
        endpoint: Endpoint,
        context: Context,
        outputs: Map<String, ContextOutput>,
    ): List<Context> =
        getNextFrom(context.current)
            .map { name ->
                val nextContent = context.globalParameters
                val nextGeneratedValues = contextGenerator.generateOnlyWithCategory(configuration.endpoints[name]!!)
                val nexts = getNextFrom(name).toSet()
                val endpointDependencies = getUnstaisfiedDependencies(name, context.current)
                val executionGraph = context.executionGraph.copy()
                if (endpoint.isRoot)
                    executionGraph.addRoot(executionId, endpoint.name)
                else executionGraph.addChild(executionId, endpoint.name)

                Context(
                    current = name,
                    content = nextContent + nextGeneratedValues,
                    next = nexts,
                    globalParameters = context.globalParameters,
                    contextOutputs = outputs + context.contextOutputs,
                    unsatisfiedDependencies = endpointDependencies.toSet(),
                    executionGraph,
                )
            }

    private fun getNextFrom(name: String): List<String> =
        dependencyGraph.getChildren(name)

    private suspend fun processResponse(
        endpoint: Endpoint,
        response: HttpResponse,
        context: Context,
    ) {
        val endpointResponse = endpoint.response
        val extractor = ResponseDataExtractor(endpointResponse)
        val responseData =  extractor.extractData(response)
        val outputs = buildOutputs(endpoint, context, responseData)
        val contextDependency = mapOf(context.current to ContextOutput(context.current, outputs))
        val updatedContexts =
            createNewContexts(
                endpoint,
                context,
                contextDependency,
            )

        updatedContexts.forEach { contextRegistryQueue.send(it) }
    }

    private suspend fun sendInfoBlockToQueue(
        context: Context,
        requestData: RequestData,
        responseData: ResponseData?,
        executionData: ExecutionData,
    ) {
        infoBlockQueue.send(InfoBlock(context, requestData, responseData, executionData, executionId))
    }

    private suspend fun sendInvalidStatus(
        context: Context,
        infoBlockRequestData: RequestData,
        infoBlockResponseData: ResponseData,
        response: HttpResponse,
        startInstant: Instant,
        stopInstant: Instant,
        expectedStatus: Int,
    ) {
        val executionData = ExecutionData(
            start = startInstant,
            end = stopInstant,
            success = ResponseSuccess.WRONG_STATUS_CODE,
            errorMessage = "Expected status $expectedStatus but received status ${response.status.value} instead",
        )

        sendInfoBlockToQueue(
            context,
            infoBlockRequestData,
            infoBlockResponseData,
            executionData,
        )
    }

    private suspend fun sendExceptionOcurred(
        context: Context,
        requestData: RequestData,
        startInstant: Instant,
        exception: Exception,
    ) {
        val executionData = ExecutionData(
            start = startInstant,
            Instant.now(),
            ResponseSuccess.EXCEPTION,
            exception.message.orEmpty(),
        )

        sendInfoBlockToQueue(
            context,
            requestData,
            null,
            executionData,
        )
    }

    private fun getUnstaisfiedDependencies(current: String, parent: String): List<String> {
        return dependencyGraph.getParents(current)
            .filter { it != parent }
    }
}