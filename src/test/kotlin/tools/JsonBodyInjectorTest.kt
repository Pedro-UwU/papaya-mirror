package tools

import ar.edu.itba.pf.tools.processJsonObject
import ar.edu.itba.pf.types.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonBodyInjectorTest {
    val jsonMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `Test simple injection`() {
        val jsonElement = jsonMapper.readTree(File("src/test/resources/Jsons/simpleBody.json"))
        val values: Map<String, String> = mapOf("param" to "asd")
        val context = Context("", values, emptySet(), emptyMap())
        val result = processJsonObject(jsonElement, context)
        assertTrue(result != null)
        assertEquals("asd", result.get("body").textValue())
    }

    @Test
    fun `Test simple json array of json objects`() {
       val jsonReader = File("src/test/resources/Jsons/jsonArray.json").bufferedReader()
        val jsonString = jsonReader.readText()
        val jsonElement = jsonMapper.readTree(File("src/test/resources/Jsons/jsonArray.json"))
        val values: Map<String, String> = mapOf(
            "param1" to "This is parameter 1", "param2" to "This is parameter 2", "param3" to "This is parameter 3"
        )
        val context = Context("", values, emptySet(), emptyMap())
        val result = processJsonObject(jsonElement, context)
        assertTrue(result != null)
        assertTrue(result.isArray)
        assertEquals(result.size(), 3)
        assertEquals(result[0].get("value").textValue(), "This is parameter 1")
        assertEquals(result[1].get("value").textValue(), "This is parameter 2")
        assertEquals(result[2].get("value").textValue(), "This is parameter 3")
    }
}

