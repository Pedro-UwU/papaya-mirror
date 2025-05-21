package types

import ar.edu.itba.pf.types.QueryParameter
import ar.edu.itba.pf.types.responses.validate
import ar.edu.itba.pf.types.validate
import org.junit.jupiter.api.Test

class QueryParameterHelpersTest {
    @Test
    fun `Valid query parameter with name and value`() {
        val queryParameter = QueryParameter(
            name = "name",
            value = "value",
        )

        val validationResult = queryParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Valid query parameter with name and optional value`() {
        val queryParameter = QueryParameter(
            name = "name",
            value = "value",
            optional = true
        )

        val validationResult = queryParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Name with less than two characters should not be valid`() {
        val queryParameter = QueryParameter(
            name = "n",
            value = "value",
        )
        assert(!queryParameter.validate().isValid) {
            "Name should have at least two characters"
        }
    }

    @Test
    fun `Name that starts without a letter should not be valid`() {
        val queryParameter = QueryParameter(
            name = "1name",
            value = "value",
        )
        assert(!queryParameter.validate().isValid) {
            "Name should start with a letter"
        }
    }
}