package tools

import ar.edu.itba.pf.tools.ContextGenerator
import ar.edu.itba.pf.tools.Worker
import ar.edu.itba.pf.types.*
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.mockito.kotlin.mock
import java.io.File
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals


class WorkerTest {
    val server_port = "8000"
    val jsonMapper = ObjectMapper();

    companion object {
        private const val PYTHON_SERVER_PATH = "src/test/resources/simple_python_server"
        private var serverProcess: Process? = null

        @BeforeAll
        @JvmStatic
        fun startServer() {
            val processBuilder = ProcessBuilder("python3", "main.py")
                .directory(File(PYTHON_SERVER_PATH))
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .redirectError(ProcessBuilder.Redirect.INHERIT)

            serverProcess = processBuilder.start()

            // Wait a bit to ensure the server is up
            Thread.sleep(3000)
        }

        @AfterAll
        @JvmStatic
        fun stopServer() {
            serverProcess?.destroy()
            serverProcess?.waitFor()
        }
    }

    private lateinit var contextQueueMock: Channel<Context>
    private lateinit var infoBlockQueueMock: Channel<InfoBlock>

    @BeforeEach
    fun start() {
        contextQueueMock = mock<Channel<Context>>()
        infoBlockQueueMock = Channel<InfoBlock>(10)
    }

    @AfterEach
    fun stop() {
        infoBlockQueueMock.cancel()
    }

    @Test
    fun `Simple HTTP request to real server on localhost`() {
        val endpoint = Endpoint(
            name = "test",
            url = "http://localhost:\${port}/foo",
            method = HTTPMethod.GET
        )
        val config = Configuration(Options(1, 1), emptyMap(), mapOf("test" to endpoint))

        val worker = Worker(
            queue = contextQueueMock,
            configuration = config,
            processingRequests = AtomicInteger(1),
            infoBlockQueue = infoBlockQueueMock,
            mock<ContextGenerator>()
        )
        val content = mapOf(
            "port" to server_port
        )
        val context = Context("test", content, emptySet())
        runBlocking {
            worker.run(context)
        }

        val infoBlock = infoBlockQueueMock.tryReceive().getOrNull()
        assertEquals(HttpStatusCode.OK.value, infoBlock?.responseData?.code)
    }

    @Test
    fun `Simple HTTP POST with body to real server on localhost`() {
        val endpoint = Endpoint(
            name = "test",
            url = "http://localhost:\${port}/bar",
            method = HTTPMethod.POST,
            body = jsonMapper.valueToTree(mapOf(
                "username" to BodyParameter("username", "\${userName}"),
                "email" to BodyParameter("email", "\${userEmail}"),
                "password" to BodyParameter("password", "\${userPassword}"),
                "name" to BodyParameter("name", "\${Name}")
            ))
        )

        val config = Configuration(Options(1, 1), emptyMap(), mapOf("test" to endpoint))
        val worker = Worker(
            queue = contextQueueMock,
            configuration = config,
            processingRequests = AtomicInteger(1),
            infoBlockQueue = infoBlockQueueMock,
            mock<ContextGenerator>()
        )
        val random_number = (0..100_000_000).random()
        val content = mapOf(
            "port" to server_port,
            "userName" to "Pedro$random_number",
            "userEmail" to "pedro@gmail.com",
            "userPassword" to "12345",
            "Name" to "Pedro$random_number",
        )
        val context = Context("test", content, emptySet())
        runBlocking {
            worker.run(context)
        }

        val infoBlock = infoBlockQueueMock.tryReceive().getOrNull()
        assertEquals(HttpStatusCode.Created.value, infoBlock?.responseData?.code)
    }

    @Test
    fun `Simple HTTP POST with body with form to real server on localhost`() {
        val endpoint = Endpoint(
            name = "test",
            url = "http://localhost:\${port}/bar",
            method = HTTPMethod.POST,
            contentType = "application/x-www-form-urlencoded",
            body = jsonMapper.valueToTree(mapOf(
                "username" to  "\${userName}",
                "email" to "\${userEmail}",
                "password" to "\${userPassword}",
                "name" to "\${Name}"
            ))
        )

        val config = Configuration(Options(1, 1), emptyMap(), mapOf("test" to endpoint))
        val worker = Worker(
            queue = contextQueueMock,
            configuration = config,
            processingRequests = AtomicInteger(1),
            infoBlockQueue = infoBlockQueueMock,
            mock<ContextGenerator>()
        )
        val random_number = (0..100_000_000).random()
        val content = mapOf(
            "port" to server_port,
            "userName" to "Pedro$random_number",
            "userEmail" to "pedro@gmail.com",
            "userPassword" to "12345",
            "Name" to "Pedro$random_number",
        )
        val context = Context("test", content, emptySet())
        runBlocking {
            worker.run(context)
        }

        val infoBlock = infoBlockQueueMock.tryReceive().getOrNull()
        assertEquals(HttpStatusCode.Created.value, infoBlock?.responseData?.code)
    }
}