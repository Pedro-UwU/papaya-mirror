package ar.edu.itba.pf.tools

import ar.edu.itba.pf.types.*
import java.util.*
import kotlin.random.Random

class ContextGenerator(
    private val configuration: Configuration,
    private val parameterGenerator: ParameterGenerator = ParameterGenerator()
) {
    fun generate(times: Int = 1): List<Context> {
        val globals = generateGlobals(configuration)
        val initialEndpoints = getInitialEndpoints().sortedBy { (name, _) -> name }
        return (1..times).flatMap { _ ->
            val contents = initialEndpoints.map { (_: String, endpoint: Endpoint) -> getParameters(endpoint, globals) }
            val nextEndpoints = initialEndpoints.map { (name, _) -> getNextEndpoints(name, configuration) }
            contents.zip(nextEndpoints).mapIndexed { i, (content, next) ->
                Context(
                    initialEndpoints[i].second.name,
                    content,
                    next.toSet(),
                    globals
                )
            }
        }
    }

    fun generateOnlyWithCategory(endpoint: Endpoint): Map<String, String> =
        endpoint.parameters
            .filter { (_, param) -> param.category != null }
            .map { (name, param) -> name to generateParameters(param) }.toMap()

    private fun getInitialEndpoints(): List<Pair<String, Endpoint>> =
        this.configuration.endpoints.filter { (_, endpoint) -> endpoint.dependsOn.isEmpty()}.toList()

    private fun generateParameters(endpoint: Endpoint): Map<String, String> =
        endpoint.parameters.map { (name, parameter) -> name to generateParameters(parameter) }.toMap()

    private fun generateGlobals(configuration: Configuration): Map<String, String> =
        configuration.globalParameters.map { (name, parameter) -> name to generateGlobalParameter(parameter) }.toMap()

    private fun getParameters(endpoint: Endpoint, globals: Map<String, String>): Map<String, String> =
        generateParameters(endpoint) + globals

    private fun generateCategoryParameter(category: Category?, default: String? = ""): String =
        when (category) {
            Category.USERNAME -> parameterGenerator.generateUsername()
            Category.PASSWORD -> parameterGenerator.generatePassword()
            Category.NAME -> parameterGenerator.generateName()
            Category.SURNAME -> parameterGenerator.generateSurname()
            Category.EMAIL -> parameterGenerator.generateEmail()
            Category.INTEGER, Category.TIMESTAMP -> Random.nextLong().toString()
            Category.FLOAT -> Random.nextDouble().toString()
            else -> default.orEmpty()
        }

    private fun generateRangeParameter(category: Category?, default: String? = "", range: Range): String =
        when (category) {
            Category.CURRENCY -> String.format(
                "%.2f", // Ends up being "inclusive" in the `to` due to String.format
                Random.nextDouble(range.from as Double, range.to as Double)
            )

            Category.INTEGER, Category.TIMESTAMP -> Random.nextLong(range.from as Long, range.to as Long).toString()
            Category.FLOAT -> Random.nextDouble(range.from as Double, range.to as Double).toString()
            else -> default.orEmpty()
        }

    private fun generateParameters(endpointParameter: EndpointParameter): String =
        if (endpointParameter.range == null) {
            generateCategoryParameter(endpointParameter.category, endpointParameter.value)
        } else {
            generateRangeParameter(endpointParameter.category, endpointParameter.value, endpointParameter.range)
        }

    private fun generateGlobalParameter(global: Global): String =
        generateCategoryParameter(global.category, global.value)

    private fun getNextEndpoints(name: String, config: Configuration): List<String> =
        configuration.endpoints
            .filter { (_, endpoint) -> endpoint.dependsOn.contains(name) }
            .map { (name, _) -> name }
            .toList()

}