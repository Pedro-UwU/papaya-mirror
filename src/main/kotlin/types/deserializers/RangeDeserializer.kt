package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.types.Range
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode


class RangeDeserializer : JsonDeserializer<Range>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Range {
        val node: JsonNode = p.codec.readTree(p)

        val from = node["from"]?.let { parseAny(it) } ?: 0
        val to = node["to"]?.let { parseAny(it) } ?: Long.MAX_VALUE
        return Range(from, to)
    }

    private fun parseAny(node: JsonNode): Any {
        return when {
            node.isInt -> node.asLong()
            node.isDouble -> node.asDouble()
            node.isTextual -> node.asText()
            else -> throw IllegalArgumentException("Unsupported type in range: $node")
        }
    }
}