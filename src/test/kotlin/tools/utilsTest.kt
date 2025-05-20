package tools

import ar.edu.itba.pf.tools.injectParameters
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class utilsTest {

    @Test
    fun `Given String with Two Placeholders And values with same placeholders should inject the String`() {
        val values = mapOf(
            "userName" to "John Doe",
            "email" to "john.doe@mail.com",
        )
        val template = "The user name is \${userName} and email is \${email}"
        val result = template injectParameters values
        assertEquals("The user name is John Doe and email is john.doe@mail.com", result)
    }

    @Test
    fun `Given String with Two Placeholders and one missing value should inject only the present value`() {
        val values = mapOf(
            "userName" to "John Doe"
        )
        val template = "The user name is \${userName} and email is \${email}"
        val result = template injectParameters values
        assertEquals("The user name is John Doe and email is \${email}", result)
    }
}