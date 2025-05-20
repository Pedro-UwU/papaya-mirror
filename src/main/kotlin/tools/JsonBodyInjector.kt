package ar.edu.itba.pf.tools

import ar.edu.itba.pf.types.Context
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

fun processJsonObject(node: JsonNode?, context: Context): JsonNode? {
    if (node == null) return null
    val jsonMapper = ObjectMapper()
    return when {
        node.isObject -> {
            val newFields = mutableMapOf<String, JsonNode>()
            node.fields().forEach { (fieldName, fieldNode) ->
                processJsonObject(fieldNode, context)?.let { processedNode ->
                    newFields[fieldName] = processedNode
                }
            }
            jsonMapper.valueToTree(newFields)
        }

        node.isArray -> {
            val newArray = node.mapNotNull { processJsonObject(it, context) }
            jsonMapper.createArrayNode().addAll(newArray)
        }

        node.isValueNode -> {
            when {
                node.isTextual -> jsonMapper.valueToTree(node.textValue().injectParameters(context))
                else -> node
            }
        }

        else -> null
    }
}


