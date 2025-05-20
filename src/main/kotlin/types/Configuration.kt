package ar.edu.itba.pf.types

import ar.edu.itba.pf.tools.validateName
import io.konform.validation.Validation

data class Configuration(
    val options: Options,
    val globalParameters: Map<String, Global>,
    val endpoints: Map<String, Endpoint>
)

fun Configuration.validate() = Validation {
    Configuration::globalParameters onEach {
        Map.Entry<String, Global>::key {
            run(validateName)
        }
        Map.Entry<String, Global>::value {
            run(globalValidator)
        }
    }

    Configuration::endpoints onEach {
        Map.Entry<String, Endpoint>::key {
            run(validateName)
        }
        Map.Entry<String, Endpoint>::value {
            run(endpointValidator)
        }
    }
}.validate(this)
