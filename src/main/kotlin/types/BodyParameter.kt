package ar.edu.itba.pf.types

import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.types.deserializers.BodyParameterDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation

@JsonDeserialize(using = BodyParameterDeserializer::class)
data class BodyParameter(
    val name: String,
    val value: String,
    val optional: Boolean = false,
)

val bodyParameterValidator = Validation<BodyParameter> {
    BodyParameter::name { run(validateName) }
}

fun BodyParameter.validate() = bodyParameterValidator.validate(this)
