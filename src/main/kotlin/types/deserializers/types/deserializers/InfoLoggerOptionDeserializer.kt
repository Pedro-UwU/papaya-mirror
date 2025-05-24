package ar.edu.itba.pf.types.deserializers.types.deserializers


import ar.edu.itba.pf.tools.infoLoggers.InfoLoggers
import ar.edu.itba.pf.types.types.InfoLoggerOption
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import java.io.File


class InfoLoggerOptionDeserializer : JsonDeserializer<InfoLoggerOption>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): InfoLoggerOption {
        val node: JsonNode = p.codec.readTree(p)
        return if (node.isTextual) {
            InfoLoggerOption(InfoLoggers.fromString(node.asText()), path = null)
        } else {
            val loggerField =
                node.fieldNames().asSequence().firstOrNull { it != "output" } ?: throw InvalidFormatException(
                    p,
                    "Logger type not found",
                    node,
                    InfoLoggerOption::class.java
                )
            val path = node[loggerField]["output"]?.asText()
            InfoLoggerOption(InfoLoggers.fromString(loggerField), path?.let { File(it) })
        }
    }


}