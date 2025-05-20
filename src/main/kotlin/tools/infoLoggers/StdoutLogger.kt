package ar.edu.itba.pf.tools.infoLoggers

import ar.edu.itba.pf.types.Context
import ar.edu.itba.pf.types.InfoLogger
import ar.edu.itba.pf.types.infoBlocks.ExecutionData
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import ar.edu.itba.pf.types.infoBlocks.RequestData
import ar.edu.itba.pf.types.infoBlocks.ResponseData

class StdoutLogger : InfoLogger {
    override fun processBlock(infoBlock: InfoBlock) {
        fun indent(level: Int) = " ".repeat(level)

        fun mapToString(map: Map<String, String>, indentLevel: Int): String {
            return map.entries.joinToString(separator = "\n") {
                "${indent(indentLevel)}${it.key}: ${it.value}"
            }
        }

        fun mapToStringList(map: Map<String, List<String>>, indentLevel: Int): String {
            return map.entries.joinToString(separator = "\n") {
                "${indent(indentLevel)}${it.key}: ${it.value.joinToString(", ")}"
            }
        }

        fun contextToString(context: Context?, indentLevel: Int): String {
            return if (context == null) {
                "${indent(indentLevel)}null"
            } else {
                """
            |${indent(indentLevel)}current: ${context.current}
            |${indent(indentLevel)}content:
            |${mapToString(context.content, indentLevel + 1)}
            |${indent(indentLevel)}next: ${context.next.joinToString(", ")}
            """.trimMargin()
            }
        }

        fun executionDataToString(executionData: ExecutionData, indentLevel: Int): String {
            return """
        |${indent(indentLevel)}start: ${executionData.start}
        |${indent(indentLevel)}end: ${executionData.end}
        |${indent(indentLevel)}success: ${executionData.success}
        |${indent(indentLevel)}errorMessage: ${executionData.errorMessage ?: "null"}
        """.trimMargin()
        }

        fun requestDataToString(requestData: RequestData, indentLevel: Int): String {
            return """
        |${indent(indentLevel)}headers:
        |${mapToString(requestData.headers, indentLevel + 1)}
        |${indent(indentLevel)}queryParams:
        |${mapToString(requestData.queryParams, indentLevel + 1)}
        |${indent(indentLevel)}body:
        |${requestData.body.toString()}
        """.trimMargin()
        }

        fun responseDataToString(responseData: ResponseData?, indentLevel: Int): String {
            return if (responseData == null) {
                "${indent(indentLevel)}null"
            } else {
                """
            |${indent(indentLevel)}code: ${responseData.code}
            |${indent(indentLevel)}headers:
            |${mapToStringList(responseData.headers, indentLevel + 1)}
            |${indent(indentLevel)}body:
            |${indent(indentLevel)} ${responseData.body}
            |${indent(indentLevel)}bodySize: ${responseData.bodySize}
            """.trimMargin()
            }
        }

        val result = """
    | ###########################################################################
    |InfoBlock:
    |${indent(1)}executionId: ${infoBlock.executionId}
    |${indent(1)}context:
    |${contextToString(infoBlock.context, 2)}
    |${indent(1)}requestData:
    |${requestDataToString(infoBlock.requestData, 2)}
    |${indent(1)}responseData:
    |${responseDataToString(infoBlock.responseData, 2)}
    |${indent(1)}executionData:
    |${executionDataToString(infoBlock.executionData, 2)}
    | ###########################################################################
    """.trimMargin()

        println(result)
    }
}