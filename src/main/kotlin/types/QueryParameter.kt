package ar.edu.itba.pf.types

import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.types.deserializers.QueryParameterDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation

@JsonDeserialize(using = QueryParameterDeserializer::class)
data class QueryParameter(
    val name: String,
    val value: String,
    val optional: Boolean = false,
)

val queryParameterValidator = Validation {
    QueryParameter::name { run(validateName) }
}

fun QueryParameter.validate() = queryParameterValidator.validate(this)
