package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.tools.toStringMap
import ar.edu.itba.pf.types.*
import ar.edu.itba.pf.types.responses.Response
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode


class EndpointDeserializer : JsonDeserializer<Endpoint>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Endpoint {
        val node: JsonNode = p.codec.readTree(p)

        val name = p.currentName
        val url = node["url"].asText()
        val dependsOn = when {
            node["dependsOn"] == null -> emptyList()
            node["dependsOn"].isArray -> node["dependsOn"].map { it.asText() }
            else -> listOf(node["dependsOn"].asText())
        }

        val method = HTTPMethod.fromString(node["method"].asText()) ?: HTTPMethod.GET

        val headers = parseMap(node["headers"], HeaderParameter::class.java, p)
        val query = parseMap(node["query"], QueryParameter::class.java, p)
        val response = node["response"]?.traverse(p.codec)?.readValueAs(Response::class.java) ?: Response.DEFAULT
        val parameters = parseMap(node["parameters"], EndpointParameter::class.java, p)
        val outputs = node["output"]?.toStringMap() ?: emptyMap()

        return Endpoint(
            name = name,
            url = url,
            dependsOn = dependsOn,
            method = method,
            headers = headers,
            body = node["body"],
            query = query,
            response = response,
            parameters = parameters,
            outputs = outputs
        )
    }

    private fun <T> parseMap(node: JsonNode?, valueType: Class<T>, parser: JsonParser): Map<String, T> {
        if (node == null || !node.isObject) {
            return emptyMap()
        }

        val result = mutableMapOf<String, T>()
        val fields = node.fields()
        val mapper = parser.codec as ObjectMapper


        while (fields.hasNext()) {
            val (key, valueNode) = fields.next()

            // Hack to inject the `name` in any type of node
            val enrichedNode = if (valueNode.isObject) {
                (valueNode.deepCopy<ObjectNode>()).apply { put("key", key) }
            } else {
                ObjectNode(mapper.nodeFactory).apply {
                    put("key", key)
                    set<JsonNode>("value", valueNode) // Add value field for primitive nodes
                }
            }

            result[key] = mapper.convertValue(enrichedNode, valueType)
        }

        return result
    }


}