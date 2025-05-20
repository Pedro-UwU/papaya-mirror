package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.types.BodyParameter
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode


class BodyParameterDeserializer : JsonDeserializer<BodyParameter>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BodyParameter {
        val node: JsonNode = p.codec.readTree(p)

        val name = p.currentName ?: node["key"].asText()

        return if (node.isTextual) {
            BodyParameter(name = name, value = node.asText())
        } else {
            val value = node["value"].asText()
            val optional = node["optional"]?.asBoolean() ?: false
            BodyParameter(name = name, value = value, optional = optional)
        }
    }

}