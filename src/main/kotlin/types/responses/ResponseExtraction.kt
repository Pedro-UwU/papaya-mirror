package ar.edu.itba.pf.types.responses

import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.tools.validateResponseExtractPath
import ar.edu.itba.pf.types.deserializers.ResponseBodyDeserializer
import ar.edu.itba.pf.types.deserializers.ResponseExtractionDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation


@JsonDeserialize(using = ResponseExtractionDeserializer::class)
data class ResponseExtraction(
    val name: String,
    val path: String,
    val default: String? = null
)
val responseExtractionValidator = Validation<ResponseExtraction> {
    ResponseExtraction::path {run(validateResponseExtractPath)}
}

