package ar.edu.itba.pf.tools

import ar.edu.itba.pf.tools.contexts.ContextRegistry
import ar.edu.itba.pf.tools.infoLoggers.InfoLoggers
import ar.edu.itba.pf.tools.infoLoggers.MinimalLogger
import ar.edu.itba.pf.tools.infoLoggers.StdoutLogger
import ar.edu.itba.pf.tools.infoLoggers.TsvLogger
import ar.edu.itba.pf.tools.infoLoggers.tools.infoLoggers.JsonLogger
import ar.edu.itba.pf.types.Configuration
import ar.edu.itba.pf.types.Context
import ar.edu.itba.pf.types.InfoLogger
import ar.edu.itba.pf.types.graph.DependencyGraph
import ar.edu.itba.pf.types.infoBlocks.InfoBlock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class WorkManager(
    private val configuration: Configuration,
) {
    private val maximumCapacityPerWorker = 10000

    fun run(context: List<Context>, contextGenerator: ContextGenerator) {
        val processingRequests = AtomicInteger(0)
        val processingContexts = AtomicBoolean(false)
        val processingInfoBlocks = AtomicBoolean(false)

        val loggers = initializeLoggers(configuration)
        val numberOfWorkers = configuration.options.numberOfWorkers
        val dependencyGraph = DependencyGraph(configuration)
        dependencyGraph.reduceTransitivity()

        runBlocking {
            val loggingChannel =
                Channel<InfoBlock>(numberOfWorkers * configuration.options.requestsPerEndpoint * maximumCapacityPerWorker)
            val infoProcessor = InfoProcessor(loggingChannel, loggers, processingInfoBlocks)

            val infoProcessorJob = launch {
                infoProcessor.start()
            }
            val contextRegistryQueue =
                Channel<Context>(numberOfWorkers * configuration.options.requestsPerEndpoint * maximumCapacityPerWorker)
            val contextQueue =
                Channel<Context>(numberOfWorkers * configuration.options.requestsPerEndpoint * maximumCapacityPerWorker)

            val contextRegistry = ContextRegistry(contextRegistryQueue, contextQueue, processingContexts)
            val contextRegistryJob = launch {
                contextRegistry.start()
            }

            val workers = List(numberOfWorkers) { _ ->
                Worker(
                    queue = contextQueue,
                    contextRegistryQueue = contextRegistryQueue,
                    configuration = configuration,
                    processingRequests = processingRequests,
                    infoBlockQueue = loggingChannel,
                    contextGenerator = contextGenerator,
                    dependencyGraph = dependencyGraph
                )
            }
            val workerJobs = workers.map { worker -> launch { worker.start() } }

            context.forEach { contextQueue.send(it) }
            delay(5000)
            while (processingRequests.get() > 0
                || processingContexts.get()
                || processingInfoBlocks.get()

            ) {
                delay(1000)
                println("Workers: ${processingRequests.get()} - Context: ${processingContexts.get()} - Info: ${processingInfoBlocks.get()}")
            }
            while (isDebuggerAttached()) {
                print("A")
                delay(1000)
            }

            // Stop workers
            workers.forEach { it.stop() }
            workerJobs.forEach { it.join() }
            infoProcessor.stop()
            infoProcessorJob.join()
            contextRegistry.stop()
            contextRegistryJob.join()
            contextQueue.close()

            // Stop logging
//            infoProcessor.stop()
            infoProcessorJob.join()
            contextRegistryJob.join()
            loggingChannel.close()
        }
    }

    private fun initializeLoggers(configuration: Configuration): List<InfoLogger> {
        return configuration.options.loggers.mapNotNull { logger ->
            when (logger.logger) {
                InfoLoggers.STDOUT -> StdoutLogger()
                InfoLoggers.MINIMAL -> MinimalLogger()
                InfoLoggers.TSV -> TsvLogger(logger.path)
                InfoLoggers.JSON -> JsonLogger(logger.path)
                InfoLoggers.NONE -> null
            }
        }
    }
}