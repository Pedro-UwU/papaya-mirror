package types

import ar.edu.itba.pf.types.Category
import ar.edu.itba.pf.types.EndpointParameter
import ar.edu.itba.pf.types.Range
import ar.edu.itba.pf.types.responses.validate
import ar.edu.itba.pf.types.validate
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

class EndpointParameterHelpersTest {
    @Test
    fun `Valid endpoint parameter with name and value should validate`() {
        val endpointParameter = EndpointParameter(
            name = "name",
            value = "value",
        )

        val validationResult = endpointParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Valid endpoint and category without range should validate`() {
        val endpointParameter = EndpointParameter(
            name = "name",
            category = Category.EMAIL
        )

        val validationResult = endpointParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Valid endpoint and category with range should validate`() {
        val endpointParameter = EndpointParameter(
            name = "name",
            category = Category.INTEGER,
            range = Range(from = 1, to = 10)
        )

        val validationResult = endpointParameter.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }


    @Test
    fun `Name with less than two characters should not be valid`() {
        val endpointParameter = EndpointParameter(
            name = "n",
            value = "value",
        )
        assert(!endpointParameter.validate().isValid) {
            "Name should have at least two characters"
        }
    }

    @Test
    fun `Name that starts without a letter should not be valid`() {
        val endpointParameter = EndpointParameter(
            name = "1name",
            value = "value",
        )
        assert(!endpointParameter.validate().isValid) {
            "Name should start with a letter"
        }
    }

    @Test
    fun `Setting value and category at the same time should not validate`() {
        val endpointParameter = EndpointParameter(
            name = "Hi my name is",
            value = "Hello World",
            category = Category.USERNAME
        )

        assert(!endpointParameter.validate().isValid) {
            "Value and category are mutually exclusive properties and should not validate."
        }
    }

    @Test
    fun `Defining a range for an invalid category should not validate`() {
        val endpointParameter = EndpointParameter(
            name = "Hi my name is",
            category = Category.USERNAME,
            range = Range(from = 1, to = 10)
        )

        assert(!endpointParameter.validate().isValid) {
            "Categories without range should not validate."
        }
    }

    @Test
    fun `Valid endpoint and category with LONG range and FLOAT category should validate and be able to cast it to Double`() {
        val rawEndpointParameter = """
          parameterName:
            category: "float"
            range:
              from: 13
              to: 90
        """

        val endpointParameter = EndpointParameter(
            name = "parameterName",
            category = Category.FLOAT,
            range = Range(from = 13.0, to = 90.0)
        )

        val objectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val deserializedEndpointParameterMap: Map<String, EndpointParameter> =
            objectMapper.readValue(rawEndpointParameter, object : TypeReference<Map<String, EndpointParameter>>() {})
        val deserializedEndpointParameter = deserializedEndpointParameterMap["parameterName"]

        assertNotNull(deserializedEndpointParameter)
        assertNotNull(deserializedEndpointParameter!!.range)
        assertNotNull(deserializedEndpointParameter.range!!.from)
        assertNotNull(deserializedEndpointParameter.range!!.to)

        assertDoesNotThrow("FLOAT category should allow the user to cast to Double even when 'from' is typed as a Long in the YAML configuration") {
            deserializedEndpointParameter.range!!.from as Double
        }
        assertDoesNotThrow("FLOAT category should allow the user to cast to Double even when 'from' is typed as a Long in the YAML configuration") {
            deserializedEndpointParameter.range!!.to as Double
        }

        assertEquals(endpointParameter.name, deserializedEndpointParameter.name)
        assertEquals(endpointParameter.category, deserializedEndpointParameter.category)
        assertEquals(Category.FLOAT, deserializedEndpointParameter.category)
        assertEquals(endpointParameter.range!!.from, deserializedEndpointParameter.range!!.from)
        assertEquals(endpointParameter.range!!.to, deserializedEndpointParameter.range!!.to)
    }


    @Test
    fun `Valid endpoint and category with LONG range and CURRENCY category should validate and be able to cast it to Double`() {
        val rawEndpointParameter = """
          parameterName:
            category: "currency"
            range:
              from: 13
              to: 90
        """

        val endpointParameter = EndpointParameter(
            name = "parameterName",
            category = Category.CURRENCY,
            range = Range(from = 13.0, to = 90.0)
        )

        val objectMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
        val deserializedEndpointParameterMap: Map<String, EndpointParameter> =
            objectMapper.readValue(rawEndpointParameter, object : TypeReference<Map<String, EndpointParameter>>() {})
        val deserializedEndpointParameter = deserializedEndpointParameterMap["parameterName"]

        assertNotNull(deserializedEndpointParameter)
        assertNotNull(deserializedEndpointParameter!!.range)
        assertNotNull(deserializedEndpointParameter.range!!.from)
        assertNotNull(deserializedEndpointParameter.range!!.to)

        assertDoesNotThrow("CURRENCY category should allow the user to cast to Double even when 'from' is typed as a Long in the YAML configuration") {
            deserializedEndpointParameter.range!!.from as Double
        }
        assertDoesNotThrow("CURRENCY category should allow the user to cast to Double even when 'from' is typed as a Long in the YAML configuration") {
            deserializedEndpointParameter.range!!.to as Double
        }

        assertEquals(endpointParameter.name, deserializedEndpointParameter.name)
        assertEquals(endpointParameter.category, deserializedEndpointParameter.category)
        assertEquals(Category.CURRENCY, deserializedEndpointParameter.category)
        assertEquals(endpointParameter.range!!.from, deserializedEndpointParameter.range!!.from)
        assertEquals(endpointParameter.range!!.to, deserializedEndpointParameter.range!!.to)
    }
}