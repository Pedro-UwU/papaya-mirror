package ar.edu.itba.pf.tools.contenttype

import com.fasterxml.jackson.databind.JsonNode

class ContentTypeHandler(
    private val formUrlEncodedHandler: FormUrlEncodedHandler = FormUrlEncodedHandler()
) {
    fun toContentTypeString(content: JsonNode, contentType: String): String {
        return when (contentType) {
            "application/json" -> content.toString()
            "application/x-www-form-urlencoded" -> formUrlEncodedHandler.encode(content)
            else -> ""
        }
    }
}