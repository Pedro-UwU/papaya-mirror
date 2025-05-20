package ar.edu.itba.pf.tools.contenttype

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class FormUrlEncodedHandlerTest {
    private val handler = FormUrlEncodedHandler()
    private val objectMapper = ObjectMapper()

    @Test
    fun `Given empty object Return empty string`() {
        val jsonNode = objectMapper.readTree("{}")
        val result = handler.encode(jsonNode)
        assertEquals("", result)
    }

    @Test
    fun `Given simple object with primitive values Return valid form urlencoded string`() {
        val jsonNode = objectMapper.readTree("""
            {
                "name": "John Doe",
                "age": 30,
                "email": "john.doe@example.com"
            }
        """)

        val result = handler.encode(jsonNode)

        // Parse result back to verify correct encoding
        val params = parseUrlEncodedParams(result)
        assertEquals(3, params.size)
        assertEquals("John Doe", params["name"])
        assertEquals("30", params["age"])
        assertEquals("john.doe@example.com", params["email"])
    }

    @Test
    fun `Given object with array values Return valid form urlencoded string with repeated keys`() {
        val jsonNode = objectMapper.readTree("""
            {
                "name": "John Doe",
                "hobbies": ["reading", "swimming", "coding"]
            }
        """)

        val result = handler.encode(jsonNode)

        // Check that hobbies appear multiple times with different values
        val params = parseUrlEncodedParams(result)
        val uniqueParams = result.split("&").map { it.split("=").first() }

        assertEquals("John Doe", params["name"])

        // Count the number of times "hobbies" appears
        val hobbiesCount = uniqueParams.count { it == "hobbies" }
        assertEquals(3, hobbiesCount)
        assertTrue(result.contains("hobbies=reading"))
        assertTrue(result.contains("hobbies=swimming"))
        assertTrue(result.contains("hobbies=coding"))
    }

    @Test
    fun `Given object with nested objects Return flattened form urlencoded string`() {
        val jsonNode = objectMapper.readTree("""
            {
                "user": {
                    "name": "John",
                    "address": {
                        "city": "New York",
                        "zipCode": "10001"
                    }
                }
            }
        """)

        val result = handler.encode(jsonNode)

        val params = parseUrlEncodedParams(result)
        assertEquals("John", params["user.name"])
        assertEquals("New York", params["user.address.city"])
        assertEquals("10001", params["user.address.zipCode"])
    }

    @Test
    fun `Given object with special characters Return properly encoded string`() {
        val jsonNode = objectMapper.readTree("""
            {
                "query": "search term & filter",
                "path": "/some/path with spaces",
                "special": "!@#$%^&*()"
            }
        """)

        val result = handler.encode(jsonNode)

        val params = parseUrlEncodedParams(result)
        assertEquals("search term & filter", params["query"])
        assertEquals("/some/path with spaces", params["path"])
        assertEquals("!@#$%^&*()", params["special"])
    }

    @Test
    fun `Given array as root Return indexed key-value pairs`() {
        val jsonNode = objectMapper.readTree("""
            ["value1", "value2", "value3"]
        """)

        val result = handler.encode(jsonNode)

        val params = parseUrlEncodedParams(result)
        assertEquals("value1", params["0"])
        assertEquals("value2", params["1"])
        assertEquals("value3", params["2"])
    }

    @Test
    fun `Given complex nested structure Return properly flattened form urlencoded string`() {
        val jsonNode = objectMapper.readTree("""
            {
                "user": {
                    "name": "John",
                    "roles": ["admin", "user"],
                    "settings": {
                        "notifications": true,
                        "theme": "dark"
                    }
                },
                "metadata": {
                    "version": "1.0.0"
                }
            }
        """)

        val result = handler.encode(jsonNode)

        val params = parseUrlEncodedParams(result)
        // Verify flattened structure
        assertEquals("John", params["user.name"])
        assertTrue(result.contains("user.roles=user"))
        assertTrue(result.contains("user.roles=admin"))
        assertEquals("true", params["user.settings.notifications"])
        assertEquals("dark", params["user.settings.theme"])
        assertEquals("1.0.0", params["metadata.version"])
    }

    private fun parseUrlEncodedParams(encoded: String): Map<String, String> {
        if (encoded.isEmpty()) return emptyMap()

        return encoded.split("&").associate { param ->
            val parts = param.split("=", limit = 2)
            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name())
            val value = if (parts.size > 1) URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name()) else ""
            key to value.removeSurrounding("\"")
        }
    }

    private fun assertTrue(condition: Boolean) {
        assertEquals(true, condition)
    }
}