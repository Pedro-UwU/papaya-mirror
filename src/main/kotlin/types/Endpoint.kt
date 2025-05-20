package ar.edu.itba.pf.types

import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.types.deserializers.EndpointDeserializer
import ar.edu.itba.pf.types.responses.Response
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation

@JsonDeserialize(using = EndpointDeserializer::class)
data class Endpoint(
    val name: String,
    val url: String,
    val dependsOn: List<String> = emptyList(),
    val method: HTTPMethod,
    val headers: Map<String, HeaderParameter> = emptyMap(),
    val body: JsonNode? = null,
    val query: Map<String, QueryParameter> = emptyMap(),
    val response: Response = Response.DEFAULT,
    val parameters: Map<String, EndpointParameter> = emptyMap(),
    val contentType: String = "application/json",
    val outputs: Map<String, String>,
) {
    val isRoot: Boolean = dependsOn.isEmpty()
}

val endpointValidator = Validation {
    Endpoint::name { run(validateName) }
    // TODO
//    Endpoint::url { run(validateUrl) }
    Endpoint::headers onEach {
        Map.Entry<String, HeaderParameter>::key {
            run(validateName)
        }
        Map.Entry<String, HeaderParameter>::value {
            run(headerParameterValidator)
        }
    }
    // Endpoint::body onEach {
    //     Map.Entry<String, BodyParameter>::key {
    //         run(validateName)
    //     }
    //     Map.Entry<String, BodyParameter>::value {
    //         run(bodyParameterValidator)
    //     }
    // }
    Endpoint::query onEach {
        Map.Entry<String, QueryParameter>::key {
            run(validateName)
        }
        Map.Entry<String, QueryParameter>::value {
            run(queryParameterValidator)
        }
    }
}

fun Endpoint.validate() = endpointValidator.validate(this)
