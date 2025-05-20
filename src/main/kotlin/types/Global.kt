package ar.edu.itba.pf.types

import ar.edu.itba.pf.tools.validateName
import ar.edu.itba.pf.types.deserializers.GlobalDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.konform.validation.Validation

@JsonDeserialize(using = GlobalDeserializer::class)
data class Global(
    val name: String,
    val value: String? = null,
    val category: Category? = null,
)

val globalValidator = Validation<Global> {
    Global::name { run(validateName) }

    constrain("Invalid combination of `value` and `category`: only one should be defined") {
        (it.value != null) xor (it.category != null)
    }
}

fun Global.validate() = globalValidator.validate(this)
