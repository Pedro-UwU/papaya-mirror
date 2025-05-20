package types

import ar.edu.itba.pf.types.Endpoint
import ar.edu.itba.pf.types.HTTPMethod
import ar.edu.itba.pf.types.responses.validate
import org.junit.jupiter.api.Test

class EndpointHelpersTest {
    @Test
    fun `Valid endpoint with name and url should validate`() {
        val endpoint = Endpoint(
            name = "name",
            url = "value",
            method = HTTPMethod.GET,
        )

        val validationResult = endpoint.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Name with less than two characters should not be valid`() {
        val endpoint = Endpoint(
            name = "n",
            url = "value",
            method = HTTPMethod.GET,
        )
        assert(!endpoint.validate().isValid) {
            "Only names with at least two characters should be valid"
        }
    }

    @Test
    fun `Name that starts without a letter should not be valid`() {
        val endpoint = Endpoint(
            name = "1name",
            url = "value",
            method = HTTPMethod.GET,
        )
        assert(!endpoint.validate().isValid) {
            "Only name that start with a letter should validate"
        }
    }

    // TODO: Fix in validator - commented out
//    @Test
//    fun `URL with less than two characters should not be valid`() {
//        val endpoint = Endpoint(
//            name = "name",
//            url = "v",
//            method = HTTPMethod.GET,
//        )
//        assert(!endpoint.validate().isValid) {
//            "Only URLs with at least two characters should be valid"
//        }
//    }
//
//    @Test
//    fun `URL that starts without a letter or variable should not be valid`() {
//        val endpoint = Endpoint(
//            name = "name",
//            url = "1value",
//            method = HTTPMethod.GET,
//        )
//        assert(!endpoint.validate().isValid) {
//            "Only URLs that start with a letter or a variable should validate"
//        }
//    }
}