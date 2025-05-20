package ar.edu.itba.pf.types.deserializers


import ar.edu.itba.pf.types.Category
import ar.edu.itba.pf.types.EndpointParameter
import ar.edu.itba.pf.types.Range
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode


class EndpointParameterDeserializer : JsonDeserializer<EndpointParameter>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EndpointParameter {
        val node: JsonNode = p.codec.readTree(p)

        val name = p.currentName ?: node["key"].asText()

        return if (node.isTextual) {
            EndpointParameter(name = name, value = node.asText())
        } else {
            val value = node["value"]?.asText()
            val category = node["category"]?.asText()?.let { Category.fromString(it) }
            var range = node["range"]?.traverse(p.codec)?.readValueAs(Range::class.java)

            // User may input a range with one or both `from` and `to` being a Long but category representing
            // a floating/fixed point type
            // In those cases, we need to manually convert the types in Range to Double to match the expected type
            // and avoid type cast Exceptions ahead
            if (category != null && range != null && category in listOf(Category.CURRENCY, Category.FLOAT)
            ) {
                if (range.from is Long) {
                    range = Range(from = (range.from as Long).toDouble(), to = range.to)
                }

                if (range.to is Long) {
                    range = Range(from = range.from, to = (range.to as Long).toDouble())
                }
            }

            EndpointParameter(name = name, value = value, category = category, range = range)
        }
    }

}