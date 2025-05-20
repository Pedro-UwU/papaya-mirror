package ar.edu.itba.pf.tools.contexts

import ar.edu.itba.pf.types.Context
import ar.edu.itba.pf.types.Endpoint

fun buildOutputs(endpoint: Endpoint, context: Context, response: Map<String, String>): Map<String, String> {
    val outputsToExtract = endpoint.outputs
    if (outputsToExtract.isEmpty())
        return emptyMap()
    return outputsToExtract.map { (name, path) ->
        name to when(path.split(".")[0]) {
            "response" -> extractOutput(path, response)
            "parameters" -> extractParameter(path, context)
            else -> ""
        }
    }.toMap()
}

private fun extractOutput(path: String, response: Map<String, String>): String {
    val tokens = path.split(".")
    if (tokens.size <= 1) return ""
    return response[tokens[1]] ?: ""
}

private fun extractParameter(path: String, context: Context): String {
    val tokens = path.split(".")
    if (tokens.size <= 1) return ""
    return context.content[tokens[1]] ?: ""
}

