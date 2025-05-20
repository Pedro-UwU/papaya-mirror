package ar.edu.itba.pf.tools

import ar.edu.itba.pf.types.Category
import io.konform.validation.Validation
import io.konform.validation.constraints.minLength
import io.konform.validation.required

val validateName = Validation<String?> {
    required {
        minLength(2) hint "Name must have at least 2 characters."
        constrain("Name must start with a letter.") { it.firstOrNull()?.isLetter() == true }
    }
}

val validateUrl = Validation<String?> {
    required {
        minLength(2) hint "URL must have at least 2 characters."
        constrain("URL must start with a letter or a variable.") {
            it.firstOrNull()?.isLetter() == true or (it.firstOrNull()?.equals('$') == true)
        }
    }
}

val validateResponseExtractPath = Validation<String?> {
    required {
        constrain("Path must not be empty") { isNullOrBlanck(it) }
        constrain("Path must start with \"body\", \"headers\", \"cookies\"")
        {
            !it.startsWith("body")
                && !it.startsWith("headers")
                && !it.startsWith("cookies")
        }
    }
}


val categoriesWithRange = setOf(
    Category.FLOAT, Category.INTEGER, Category.TIMESTAMP, Category.CURRENCY
)
