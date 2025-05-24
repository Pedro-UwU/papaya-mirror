package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.types.Options
import ar.edu.itba.pf.types.types.InfoLoggerOption
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper


class OptionsDeserializer : JsonDeserializer<Options>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Options {
        val node: JsonNode = p.codec.readTree(p)

        val nRequests = node["requestsPerEndpoint"]?.asInt() ?: 1
        val nWorkers = node["numberOfWorkers"]?.asInt() ?: 1
        val loggers = parseList(node["loggers"], InfoLoggerOption::class.java, p)
        return Options(requestsPerEndpoint = nRequests, numberOfWorkers = nWorkers, loggers = loggers)
    }

    private fun <T> parseList(node: JsonNode?, valueType: Class<T>, parser: JsonParser): List<T> {
        if (node == null || !node.isArray) {
            return emptyList()
        }

        val result = mutableListOf<T>()
        val mapper = parser.codec as ObjectMapper
        node.forEach { element ->
            val value = when {
                element.isTextual -> mapper.readValue(element.asText(), valueType)
                else -> mapper.treeToValue(element, valueType)
            }
            result.add(value)
        }

        return result
    }
}