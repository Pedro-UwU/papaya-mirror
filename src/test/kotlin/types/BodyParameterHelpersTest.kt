package types

import ar.edu.itba.pf.types.BodyParameter
import ar.edu.itba.pf.types.responses.validate
import org.junit.jupiter.api.Test

class BodyParameterHelpersTest {
    @Test
    fun `Valid body parameter with name and value`() {
        val bodyParameter = BodyParameter(
            name = "name",
            value = "value",
        )

        val validationResult = bodyParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Valid body parameter with name and optional value`() {
        val bodyParameter = BodyParameter(
            name = "name",
            value = "value",
            optional = true
        )

        val validationResult = bodyParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Name with less than two characters should not be valid`() {
        val bodyParameter = BodyParameter(
            name = "n",
            value = "value",
        )
        assert(!bodyParameter.validate().isValid) {
            "Name should have at least two characters"
        }
    }

    @Test
    fun `Name that starts without a letter should not be valid`() {
        val bodyParameter = BodyParameter(
            name = "1name",
            value = "value",
        )
        assert(!bodyParameter.validate().isValid) {
            "Name should start with a letter"
        }
    }
}