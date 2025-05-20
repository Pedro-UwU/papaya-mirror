package ar.edu.itba.pf.types.responses

import ar.edu.itba.pf.tools.validateName
import io.konform.validation.Validation
import io.konform.validation.constraints.maximum
import io.konform.validation.constraints.minimum

data class Response(
    val responseCode: Int,
    val responseExtract: Map<String, ResponseExtraction> = emptyMap(),
) {
    companion object {
        val DEFAULT = Response(200)
    }
}

val responseValidator = Validation<Response> {
    Response::responseCode {
        minimum(100)
        maximum(600)
    }
}

fun Response.validate() = responseValidator.validate(this)
