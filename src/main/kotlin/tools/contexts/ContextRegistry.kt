package ar.edu.itba.pf.tools.contexts

import ar.edu.itba.pf.types.Context
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean

class ContextRegistry(
    private val contextQueue: Channel<Context>,
    private val workersQueue: Channel<Context>,
    private val processingContexts: AtomicBoolean,
) {
    private var running = false
    private val incompleteContexts: ArrayList<Context> = ArrayList()

    suspend fun start() {
        running = true
        while (running) {
            processingContexts.getAndSet(true)
            val context = contextQueue.tryReceive().getOrNull()
//            println("Processing counter Contexts: ${processingContexts.get()}")
            if (context != null) {
                processContext(context)
            }
            else {
                processingContexts.getAndSet(false)
            }
            yield()
        }
    }

    private suspend fun processContext(context: Context) {
        if (isComplete(context)) {
            // Send to workers Queue
            workersQueue.send(context)
            return
        }
        val possibleContexts = incompleteContexts
            .filter { it.current == context.current }
            .filter { containsAnyUnsatisfiedDependencies(it, context) }
        for (incompleteContext: Context in possibleContexts) {
            val commonAncestors = getCommonAncestors(context, incompleteContext)
            if (commonAncestorsAreTheSameOrHeads(context, incompleteContext, commonAncestors)) {
                incompleteContext.executionGraph = incompleteContext.executionGraph.mergeWith(context.executionGraph)
                incompleteContext.unsatisfiedDependencies = incompleteContext.unsatisfiedDependencies
                    .intersect(context.unsatisfiedDependencies)
                incompleteContext.contextOutputs += context.contextOutputs
                if (incompleteContext.unsatisfiedDependencies.isEmpty()) {
                    incompleteContexts.remove(incompleteContext)
                    workersQueue.send(incompleteContext)
                }
                return
            }
        }
        incompleteContexts.add(context)
    }

    fun stop() {
        running = false
    }

    private fun isComplete(context: Context): Boolean =
        context.unsatisfiedDependencies.isEmpty()

    private fun getCommonAncestors(context1: Context, context2: Context): List<String> {
        val executionGraph1 = context1.executionGraph
        val executionGraph2 = context2.executionGraph
        return executionGraph1.findCommonAncestors(executionGraph2)
    }

    private fun getPrevExecution(context: Context): List<String> =
        context.executionGraph.getLeaves()
            .map{ it.endpointName }

    private fun containsAnyUnsatisfiedDependencies(context: Context, other: Context): Boolean {
        return context.unsatisfiedDependencies.intersect(getPrevExecution(other).toSet()).isNotEmpty()
    }

    private fun commonAncestorsAreTheSameOrHeads(
        context1: Context,
        context2: Context,
        commonAncestors: List<String>
    ): Boolean {
        val ancestors1 = commonAncestors.map { context1.executionGraph.getNodeByName(it) }
        val ancestors2 = commonAncestors.map { context2.executionGraph.getNodeByName(it) }
        val ancestorsAreTheSame = if (commonAncestors.isEmpty()) {
            false
        } else {
            ancestors1.zip(ancestors2).all { (ancestor1, ancestor2) -> ancestor1?.equals(ancestor2) ?: false }
        }
        if (!ancestorsAreTheSame) {
            val executionHeads1 = context1.executionGraph.getHeads().map { it.endpointName }
            val executionHeads2 = context2.executionGraph.getHeads().map { it.endpointName }
            return executionHeads1.intersect(executionHeads2.toSet()).isEmpty()
        }
        return true
    }
}

