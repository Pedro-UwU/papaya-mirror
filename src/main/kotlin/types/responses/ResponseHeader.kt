package ar.edu.itba.pf.types.responses

import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.types.deserializers.ResponseHeaderDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation

@JsonDeserialize(using = ResponseHeaderDeserializer::class)
data class ResponseHeader(
    val name: String,
    val fieldName: String,
)

val responseHeaderValidator = Validation<ResponseHeader> {
    ResponseHeader::name { run(validateName) }
}

fun ResponseHeader.validate() = responseHeaderValidator.validate(this)
