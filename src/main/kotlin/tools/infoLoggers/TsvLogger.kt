package ar.edu.itba.pf.tools.infoLoggers

import ar.edu.itba.pf.types.InfoLogger
import ar.edu.itba.pf.types.infoBlocks.ExecutionData
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import ar.edu.itba.pf.types.infoBlocks.RequestData
import ar.edu.itba.pf.types.infoBlocks.ResponseData
import java.io.File

class TsvLogger(outputFile: File?) : InfoLogger {
    private val writer = outputFile?.bufferedWriter() ?: File("./output.tsv").bufferedWriter()

    init {
        val header = listOf(
            "ID",
            // ExecutionData
            "Start",
            "End",
            "Status",
            "Error Message",
            // RequestData
            "Request: Query Params",
            "Request: Body",
            // ResponseData
            "Response: Code",
            "Response: Headers",
            "Response: Body",
            "Response: Body Size",
        ).joinToString(separator = "\t", postfix = "\n")
        writer.write(header)
    }

    override fun stop() {
        writer.close()
    }

    override fun processBlock(infoBlock: InfoBlock) {
        val result = listOf(
            infoBlock.executionId,
            executionDataToString(infoBlock.executionData),
            requestDataToString(infoBlock.requestData),
            responseDataToString(infoBlock.responseData),
        ).joinToString(separator = "\t", postfix = "\n")

        writer.write(result)
    }

    fun mapToString(map: Map<String, String>): String {
        return if (map.isEmpty()) {
            "null"
        } else {
            map.entries.joinToString(separator = " | ", postfix = "") {
                "${it.key}: ${it.value.replace("\t", ",")}"
            }
        }
    }

    fun mapToStringList(map: Map<String, List<String>>): String {
        return if (map.isEmpty()) {
            "null"
        } else {
            map.entries.joinToString(separator = " | ", postfix = "") {
                "${it.key}: [${it.value.joinToString(";") { v -> v.replace("\t", ",") }}]"
            }
        }
    }

    fun executionDataToString(executionData: ExecutionData): String {
        return listOf(
            executionData.start,
            executionData.end,
            executionData.success,
            executionData.errorMessage ?: "null",
        ).joinToString(separator = "\t", postfix = "")
    }

    fun requestDataToString(requestData: RequestData): String {
        return listOf(
            mapToString(requestData.queryParams),
            requestData.body?.asText()?.replace("\t", ",") ?: "null" // Escape commas
        ).joinToString(separator = "\t", postfix = "")
    }

    fun responseDataToString(responseData: ResponseData?): String {
        return listOf(
            responseData?.code ?: "null",
            responseData?.let { mapToStringList(it.headers) } ?: "null",
            responseData?.body?.replace("\t", ",") ?: "null",
            responseData?.bodySize ?: "null"
        ).joinToString(separator = "\t", postfix = "")
    }
}