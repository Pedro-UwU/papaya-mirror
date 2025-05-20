package ar.edu.itba.pf.types

import ar.edu.itba.pf.tools.categoriesWithRange
import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.types.deserializers.EndpointParameterDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@JsonDeserialize(using = EndpointParameterDeserializer::class)
data class EndpointParameter(
    val name: String,
    val value: String? = null,
    val category: Category? = null,
    @Contextual
    val range: Range? = null,
)

val endpointParameterValidator = Validation<EndpointParameter> {
    EndpointParameter::name {
        run(validateName)
    }
    constrain("Invalid combination of `value`, `category`, and `inherited`: only one should be defined") {
        (it.value != null) xor (it.category != null)
    }

    constrain("Range can only be defined for categories: $categoriesWithRange") {
        if (it.range != null) {
            it.category != null && it.category in categoriesWithRange
        } else {
            true
        }
    }
}

fun EndpointParameter.validate() = endpointParameterValidator.validate(this)
