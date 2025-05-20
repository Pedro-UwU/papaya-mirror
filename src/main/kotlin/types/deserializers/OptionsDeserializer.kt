package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.types.Options
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode


class OptionsDeserializer : JsonDeserializer<Options>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Options {
        val node: JsonNode = p.codec.readTree(p)

        return if (node.isTextual) {
            Options(requestsPerEndpoint = node.asInt(), numberOfWorkers = node.asInt())
        } else {
            val nRequests = node["requestsPerEndpoint"]?.asInt() ?: 1
            val nWorkers = node["numberOfWorkers"]?.asInt() ?: 1
            Options(requestsPerEndpoint = nRequests, numberOfWorkers = nWorkers)
        }
    }

}