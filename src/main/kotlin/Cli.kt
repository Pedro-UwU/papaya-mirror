package ar.edu.itba.pf

import ar.edu.itba.pf.tools.ContextGenerator
import ar.edu.itba.pf.tools.WorkManager
import ar.edu.itba.pf.types.Configuration
import ar.edu.itba.pf.types.graph.DependencyGraph
import ar.edu.itba.pf.types.validate
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.file

class Cli : CliktCommand() {
    private val configFile by argument().file(mustExist = true, canBeDir = false, mustBeReadable = true)
        .help("YAML Configuration file")

    override fun run() {
        val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule.Builder().build())
        mapper.findAndRegisterModules()

        val configuration: Configuration = mapper.readValue(configFile)
        println(configuration)

        val validationResult = configuration.validate()
        if (!validationResult.isValid) {
            println(validationResult.errors)
            return
        }

        val contextGenerator = ContextGenerator(configuration = configuration)
        val contexts = contextGenerator.generate(configuration.options.requestsPerEndpoint)
        val workManager = WorkManager(configuration)
        workManager.run(contexts, contextGenerator)
    }
}