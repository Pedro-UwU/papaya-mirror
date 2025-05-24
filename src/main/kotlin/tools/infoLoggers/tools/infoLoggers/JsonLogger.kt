package ar.edu.itba.pf.tools.infoLoggers.tools.infoLoggers

import ar.edu.itba.pf.types.InfoLogger
import ar.edu.itba.pf.types.infoBlocks.ExecutionData
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import ar.edu.itba.pf.types.infoBlocks.RequestData
import ar.edu.itba.pf.types.infoBlocks.ResponseData
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean


class JsonLogger(outputFile: File?) : InfoLogger {
    private val writer = outputFile?.bufferedWriter() ?: File("./output.json").bufferedWriter()
    private val isFirstBlock = AtomicBoolean(true)
    private val mapper = jacksonObjectMapper()


    init {
        writer.write("[")
    }

    override fun stop() {
        writer.write("]")
        writer.close()
    }

    override fun processBlock(infoBlock: InfoBlock) {
        val result = mapOf<String, Any?>(
            "id" to infoBlock.executionId,
            "execution" to executionDataToString(infoBlock.executionData),
            "request" to requestDataToString(infoBlock.requestData),
            "response" to responseDataToString(infoBlock.responseData),
        )

        if (isFirstBlock.getAndSet(false)) {
            writer.write(mapper.writeValueAsString(result))
        } else {
            writer.write(",${mapper.writeValueAsString(result)}")
        }
    }

    fun executionDataToString(executionData: ExecutionData): Map<String, Any?> {
        return mapOf(
            "start" to executionData.start.toString(),
            "end" to executionData.end.toString(),
            "status" to executionData.success.toString(),
            "errorMessage" to executionData.errorMessage,
        )
    }

    fun requestDataToString(requestData: RequestData): Map<String, Any?> {
        return mapOf(
            "queryParams" to requestData.queryParams,
            "body" to requestData.body
        )
    }

    fun responseDataToString(responseData: ResponseData?): Map<String, Any?> {
        return mapOf(
            "code" to responseData?.code,
            "headers" to responseData?.headers,
            "body" to responseData?.body,
            "bodySize" to responseData?.bodySize
        )
    }
}