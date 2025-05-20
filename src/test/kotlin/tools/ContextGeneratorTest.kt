package tools

import ar.edu.itba.pf.tools.ContextGenerator
import ar.edu.itba.pf.types.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ContextGeneratorTest {

    val jsonMapper = ObjectMapper()

    @Test
    fun `Given Single Context with One Initial Endpoint and one that depends on it Should Return Single Context`() {
        val contextGenerator = ContextGenerator(createConfiguration())

        val generated = contextGenerator.generate()
        assertEquals(generated.size, 1)
        assertTrue(generated[0].next.contains("submit-description"))
    }

    @Test
    fun `Given Single Context with Generate 2 Should Return Two Contexts`() {
        val contextGenerator = ContextGenerator(createConfiguration())

        val generated = contextGenerator.generate(2)
        assertEquals(generated.size, 2)
        assertTrue(generated[0].next.contains("submit-description"))
        assertTrue(generated[1].next.contains("submit-description"))
    }

    @Test
    fun `Given Single Context with Generate -1 Should Return 0 Contexts`() {
        val contextGenerator = ContextGenerator(createConfiguration())

        val generated = contextGenerator.generate(-1)
        assertEquals(generated.size, 0)
    }


    private fun createConfiguration(): Configuration {
        val bodyMap = mapOf(
            "username" to BodyParameter("username", "\${userName}"),
            "password" to BodyParameter("password", "\${password}"),
            "email" to BodyParameter("email", "\${email}"))
        val body = jsonMapper.writeValueAsString(bodyMap)
        val endpoint1 = Endpoint(
            name = "create-user",
            url = "http://localhost:8080/user",
            method = HTTPMethod.POST,
            headers = mapOf(
                "Content-type" to HeaderParameter("Content-type", "application/json")
            ),
            body = jsonMapper.valueToTree(body),
            query = mapOf(),
            parameters = mapOf(
                "userName" to EndpointParameter(name = "userName", category = Category.USERNAME),
                "password" to EndpointParameter(name = "password", category = Category.PASSWORD),
                "email" to EndpointParameter(name = "email", category = Category.EMAIL)
            )

        )

        val endpoint2 = Endpoint(
            name = "submit-description",
            url = "http://localhost:8080/user/description",
            dependsOn = "create-user",
            method = HTTPMethod.POST,
            headers = mapOf(
                "Content-type" to HeaderParameter(name = "Content-type", value = "application/json")
            ),
            body = jsonMapper.valueToTree(null),
            query = mapOf()
        )

        return Configuration(
            options = Options(1, 1),
            globalParameters = mapOf(),
            endpoints = mapOf(
                "create-user" to endpoint1,
                "submit-description" to endpoint2
            )
        )
    }
}

