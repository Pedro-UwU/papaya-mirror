package ar.edu.itba.pf.types

import ar.edu.itba.pf.types.graph.ExecutionGraph

data class Context(
    val current: String,
    val content: Map<String, String>,
    val next: Set<String>,
    val globalParameters: Map<String, String>,
    var contextOutputs: Map<String, ContextOutput> = emptyMap(),
    var unsatisfiedDependencies: Set<String> = emptySet(),
    var executionGraph: ExecutionGraph = ExecutionGraph(),
)

data class ContextOutput(
    val endpointName: String,
    val outputs: Map<String, String> = emptyMap()
)

data class ExecutionLog(
    val endpointName: String,
    val executionId: String
)
