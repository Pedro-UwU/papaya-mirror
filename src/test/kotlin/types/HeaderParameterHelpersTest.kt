package types

import ar.edu.itba.pf.types.HeaderParameter
import ar.edu.itba.pf.types.responses.validate
import org.junit.jupiter.api.Test

class HeaderParameterHelpersTest {
    @Test
    fun `Valid header parameter with name and value`() {
        val headerParameter = HeaderParameter(
            name = "name",
            value = "value",
        )

        val validationResult = headerParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Valid header parameter with name and optional value`() {
        val headerParameter = HeaderParameter(
            name = "name",
            value = "value",
            optional = true
        )

        val validationResult = headerParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Name with less than two characters should not be valid`() {
        val headerParameter = HeaderParameter(
            name = "n",
            value = "value",
        )
        assert(!headerParameter.validate().isValid) {
            "Name should have at least two characters"
        }
    }

    @Test
    fun `Name that starts without a letter should not be valid`() {
        val headerParameter = HeaderParameter(
            name = "1name",
            value = "value",
        )
        assert(!headerParameter.validate().isValid) {
            "Name should start with a letter"
        }
    }
}