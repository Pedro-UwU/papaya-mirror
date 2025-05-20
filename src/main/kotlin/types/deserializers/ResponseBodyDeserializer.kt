package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.types.responses.ResponseBody
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode


class ResponseBodyDeserializer : JsonDeserializer<ResponseBody>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ResponseBody {
        val node: JsonNode = p.codec.readTree(p)

        val name = p.currentName ?: node["key"].asText()

        return if (node.isTextual) {
            ResponseBody(name = name, fieldName = node.asText())
        } else {
            val fieldName = node["fieldName"].asText()
            ResponseBody(name = name, fieldName = fieldName)
        }
    }

}