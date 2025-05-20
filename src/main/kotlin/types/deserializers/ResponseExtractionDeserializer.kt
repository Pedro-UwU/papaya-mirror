package ar.edu.itba.pf.types.deserializers

import ar.edu.itba.pf.types.responses.ResponseExtraction
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode

class ResponseExtractionDeserializer : JsonDeserializer<ResponseExtraction>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ResponseExtraction {
        val node = p.codec.readTree<JsonNode>(p)
        val fieldName = p.currentName ?: throw JsonParseException(p, "Field name is missing for ResponseExtraction")

        return when {
            node.isTextual -> {
                ResponseExtraction(
                    name = fieldName,
                    path = node.asText()
                )
            }
            node.isObject -> {
                val path = node.get("path")?.asText()
                    ?: throw JsonParseException(p, "Path is required for ResponseExtraction")

                val default = node.get("default")?.asText()

                ResponseExtraction(
                    name = fieldName,
                    path = path,
                    default = default
                )
            }
            else -> throw JsonParseException(
                p,
                "Invalid format for ResponseExtraction. Expected a string or an object with 'path' field."
            )
        }
    }
}