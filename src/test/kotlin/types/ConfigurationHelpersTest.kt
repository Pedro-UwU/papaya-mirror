package types

import ar.edu.itba.pf.types.Configuration
import ar.edu.itba.pf.types.Global
import ar.edu.itba.pf.types.Options
import ar.edu.itba.pf.types.responses.validate
import org.junit.jupiter.api.Test

class ConfigurationHelpersTest {

    private val simpleGlobal = Global(
        name = "Hi my name is",
        value = "Hello World",
    )

    @Test
    fun `Basic validation`() {
        val configuration =
            Configuration(Options(1, 1), globalParameters = mapOf(Pair("Hello", simpleGlobal)), endpoints = mapOf())

        val validationResult = configuration.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }
}