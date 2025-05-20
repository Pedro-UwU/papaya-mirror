package ar.edu.itba.pf.types

import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.types.deserializers.HeaderParameterDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation

@JsonDeserialize(using = HeaderParameterDeserializer::class)
data class HeaderParameter(
    val name: String,
    val value: String,
    val optional: Boolean = false,
)

val headerParameterValidator = Validation {
    HeaderParameter::name { run(validateName) }
}

fun HeaderParameter.validate() = headerParameterValidator.validate(this)