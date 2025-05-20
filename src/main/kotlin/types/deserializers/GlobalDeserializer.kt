package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.types.Category
import ar.edu.itba.pf.types.Global
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode


class GlobalDeserializer : JsonDeserializer<Global>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Global {
        val node: JsonNode = p.codec.readTree(p)

        val name = p.currentName ?: node["key"].asText()

        return if (node.isTextual) {
            Global(name = name, value = node.asText())
        } else {
            val value = node["value"]?.asText()
            val category = node["category"]?.asText()?.let { Category.fromString(it) }
            Global(name = name, value = value, category = category)
        }
    }

}