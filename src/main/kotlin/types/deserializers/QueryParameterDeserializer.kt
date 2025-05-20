package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.types.QueryParameter
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode


class QueryParameterDeserializer : JsonDeserializer<QueryParameter>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): QueryParameter {
        val node: JsonNode = p.codec.readTree(p)

        val name = p.currentName ?: node["key"].asText()

        return if (node.isTextual) {
            QueryParameter(name = name, value = node.asText())
        } else {
            val value = node["value"].asText()
            val optional = node["optional"]?.asBoolean() ?: false
            QueryParameter(name = name, value = value, optional = optional)
        }
    }

}