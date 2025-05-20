package ar.edu.itba.pf.tools.contenttype

import com.fasterxml.jackson.databind.JsonNode
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class FormUrlEncodedHandler: ContentHandler {
    override fun encode(content: JsonNode): String {
        return when {
            content.isObject -> encodeObject(content)
            content.isArray -> encodeArray(content)
            else -> ""
        }
    }

    private fun encodeObject(content: JsonNode): String {
        return content.fields()
            .asSequence()
            .flatMap { (key, value) -> encodeNode(key, value) }
            .joinToString("&")
    }

    private fun encodeArray(content: JsonNode): String {
        return content.asSequence()
            .mapIndexed { index, element -> encodeNode(index.toString(), element) }
            .flatten()
            .joinToString("&")
    }


    private fun encodeNode(key: String, value: JsonNode): Sequence<String> {
        return when {
            value.isValueNode -> sequenceOf("${encodeComponent(key)}=${encodeComponent(value.asText())}")
            value.isArray -> value.asSequence()
                .flatMap { element -> encodeNode(key, element) }
            value.isObject -> value.fields()
                .asSequence()
                .flatMap { (fieldName, fieldValue) -> encodeNode("$key.${fieldName}", fieldValue)
                }
            else -> emptySequence()
        }
    }

    private fun encodeComponent(component: String): String =
        URLEncoder.encode(component, StandardCharsets.UTF_8.name())
            .replace("+", "%20")
}