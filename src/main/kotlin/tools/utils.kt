package ar.edu.itba.pf.tools

import ar.edu.itba.pf.types.Category
import ar.edu.itba.pf.types.Context
import ar.edu.itba.pf.types.ParameterType
import ar.edu.itba.pf.types.Range
import com.fasterxml.jackson.databind.JsonNode
import org.jetbrains.annotations.Nullable
import java.lang.management.ManagementFactory

infix fun String.injectParameters(context: Context): String {
    val valueRegex = "\\$\\{(.+?)}".toRegex()
    val outputsRegex = "\\$\\{\\{(.+?)}}".toRegex()
    val values = context.content
    val outputs = context.contextOutputs

    var result = this

    // Replace ${} placeholders with values from context.content
    valueRegex.findAll(result).forEach { matchResult ->
        val placeholder = matchResult.value
        val key = matchResult.groupValues[1]
        val value = values[key] ?: matchResult.value
        result = result.replace(placeholder, value)
    }

    // Replace ${{}}} placeholders with values from context.contextOutputs
    outputsRegex.findAll(result).forEach { matchResult ->
        val placeholder = matchResult.value
        val value: String
        val tokens = matchResult.groupValues[1].split(".")
        value = if (tokens.size != 3 || "outputs" != tokens[0]) {
            placeholder
        } else {
            val endpoint = outputs[tokens[1]]
            if (endpoint == null)
                placeholder
            else
                endpoint.outputs[tokens[2]] ?: placeholder
        }
        result = result.replace(placeholder, value)
    }

    return result
}

@Nullable
fun valueWithParameterType(value: String, type: ParameterType): Any? {
    return when (type) {
        ParameterType.STRING -> value
        ParameterType.INTEGER, ParameterType.TIMESTAMP -> value.toLongOrNull()
        ParameterType.FLOAT -> value.toDoubleOrNull()
    }
}

fun valueWithParameterTypeInRange(value: String, type: ParameterType, range: Range): Boolean? {
    if (type == ParameterType.STRING) {
        return null
    }

    val fromValue = valueWithParameterType(range.from.toString(), type) ?: return null
    val toValue = valueWithParameterType(range.to.toString(), type) ?: return null
    val typedValue = valueWithParameterType(value, type) ?: return null

    return when (type) {
        ParameterType.INTEGER, ParameterType.TIMESTAMP -> typedValue as Long in fromValue as Long..toValue as Long
        ParameterType.FLOAT -> typedValue as Double in fromValue as Double..toValue as Double
        else -> null
    }
}

@Nullable
fun valueWithCategory(value: String, type: Category): Any? {
    return when (type) {
        Category.INTEGER, Category.TIMESTAMP -> value.toLongOrNull()
        Category.FLOAT -> value.toDoubleOrNull()
        else -> null
    }
}

fun isNullOrBlanck(value: String?): Boolean =
    value?.isBlank() ?: true

fun isDebuggerAttached(): Boolean {
    val runtimeMXBean = ManagementFactory.getRuntimeMXBean()
    return runtimeMXBean.inputArguments.toString().contains("-agentlib:jdwp")
}

fun JsonNode.toStringMap(): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val fields = this.fields()
    while (fields.hasNext()) {
        val (key, value) = fields.next()
        val rawText = value.asText()
        val formattedValue = if (rawText.startsWith("\"") && rawText.endsWith("\"")) {
            rawText.substring(1, rawText.length - 1)
        } else {
            rawText
        }
        result[key] = formattedValue
    }
    return result
}


