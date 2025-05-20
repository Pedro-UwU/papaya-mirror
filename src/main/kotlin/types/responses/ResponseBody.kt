package ar.edu.itba.pf.types.responses

import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.types.deserializers.ResponseBodyDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation

@JsonDeserialize(using = ResponseBodyDeserializer::class)
data class ResponseBody(
    val name: String,
    val fieldName: String,
)

val responseBodyValidator = Validation<ResponseBody> {
    ResponseBody::name { run(validateName) }
}

fun ResponseBody.validate() = responseBodyValidator.validate(this)
