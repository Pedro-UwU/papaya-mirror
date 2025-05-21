package types

import ar.edu.itba.pf.types.Category
import ar.edu.itba.pf.types.Global
import ar.edu.itba.pf.types.responses.validate
import ar.edu.itba.pf.types.validate
import org.junit.jupiter.api.Test

class GlobalHelpersTest {
    @Test
    fun `Valid name and value should validate`() {
        val global = Global(
            name = "Hi my name is",
            value = "Hello World",
            )

        val validationResult = global.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Valid name and category should validate`() {
        val global = Global(
            name = "Hi my name is",
            category = Category.USERNAME )

        val validationResult = global.validate()
        assert(validationResult.isValid) {
            validationResult.errors
        }
    }

    @Test
    fun `Setting value and category at the same time should not validate`() {
        val global = Global(
            name = "Hi my name is",
            value = "Hello World",
            category = Category.USERNAME
        )

        assert(!global.validate().isValid) {
            "Value and category are mutually exclusive."
        }
    }

    @Test
    fun `Not setting value and category should not validate`() {
        val global = Global(
            name = "Hi my name is",
        )

        assert(!global.validate().isValid) {
            "If neither value and category are set, it should not validate."
        }
    }

    @Test
    fun `Name with less than two characters should not be valid`() {
        val global = Global(
            name = "n",
            value = "value",
        )
        assert(!global.validate().isValid) {
            "Name should have at least two characters"
        }
    }

    @Test
    fun `Name that starts without a letter should not be valid`() {
        val global = Global(
            name = "1name",
            value = "value",
        )
        assert(!global.validate().isValid) {
            "Name should start with a letter"
        }
    }
}