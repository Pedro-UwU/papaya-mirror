package ar.edu.itba.pf.types.graph

import ar.edu.itba.pf.types.Configuration
import ar.edu.itba.pf.types.Endpoint
import kotlin.system.exitProcess

class DependencyGraph(config: Configuration) {
    private val children: HashMap<Endpoint, ArrayList<Endpoint>> = HashMap()
    private val parents: HashMap<Endpoint, ArrayList<Endpoint>> = HashMap()

    init {
        initGraph(config)
    }

    fun reduceTransitivity(): DependencyGraph {
        val adjacencyMatrix = computeAdjacencyMatrix()
        val transitiveClosure = computeTransitiveClosure(adjacencyMatrix)
        val transitiveMatrix = computeTransitiveReduction(adjacencyMatrix, transitiveClosure)
        val sortedNodes = children.keys.toList().sortedBy { it.name }
        transitiveMatrix.forEachIndexed { index, row ->
            val endpoint = sortedNodes[index]
            val children = row
                .mapIndexed { j, connected -> sortedNodes[j] to connected }
                .filter { (_, connected) -> connected == 1 }
                .map { (j, _) -> j }
            this.children[endpoint]!!.removeIf { !children.contains(it) }
        }
        parents.clear()
        createParentsGraph()
        return this
    }

    fun getParents(name: String): List<String> {
        return parents.filter { (endpoint, parents) -> endpoint.name == name }
            .map { (_, par) -> par }
            .flatten()
            .map { it.name }
    }

    fun getChildren(name: String): List<String> {
        return children.filter { (endpoint, _) -> endpoint.name == name }
            .map { (_, child) -> child }
            .flatten()
            .map { it.name }
    }

    private fun initGraph(config: Configuration) {
        config.endpoints.forEach {
            try {
                addEndpoint(it.value, config)
            } catch (_: NullPointerException) {
                System.err.println("Invalid node in configuration. Are dependencies in order? Do you have circular dependencies?")
                exitProcess(1)
            }
        }
        createParentsGraph()
    }


    private fun createParentsGraph() {
        children.keys.forEach {
            parents.putIfAbsent(it, ArrayList())
        }
        children.entries.forEach { (endpoint, childEndpoints) ->
            childEndpoints.forEach { child ->
                parents.putIfAbsent(child, ArrayList())
                parents[child]!!.add(endpoint)
            }
        }

    }

    private fun addEndpoint(endpoint: Endpoint, config: Configuration) {
        children.putIfAbsent(endpoint, ArrayList())
        endpoint.dependsOn.forEach {
            val parentEndpoint = config.endpoints[it]!!
            children.putIfAbsent(parentEndpoint, ArrayList())
            children[parentEndpoint]!!.add(endpoint)
        }
    }


    private fun computeAdjacencyMatrix(): Array<IntArray> {
        val endpoints = children.keys.toList().sortedBy { it.name }
        val n = endpoints.size
        val adjacencyMatrix = Array(n) { IntArray(n) { 0 } }
        for (i in 0 until n) {
            val endpoint = endpoints[i]
            val dependencies = children[endpoint] ?: emptyList()

            for (dependency in dependencies) {
                val j = endpoints.indexOf(dependency)
                if (j != -1) {
                    adjacencyMatrix[i][j] = 1
                }
            }
        }

        return adjacencyMatrix
    }

    private fun computeTransitiveClosure(adjacencyMatrix: Array<IntArray>): Array<IntArray> {
        val n = adjacencyMatrix.size
        val transitiveClosure = Array(n) { i ->
            adjacencyMatrix[i].copyOf()
        }
        for (k in 0 until n) {
            for (i in 0 until n) {
                for (j in 0 until n) {
                    if (transitiveClosure[i][k] == 1 && transitiveClosure[k][j] == 1) {
                        transitiveClosure[i][j] = 1
                    }
                }
            }
        }
        return transitiveClosure
    }

    private fun computeTransitiveReduction(
        adjacencyMatrix: Array<IntArray>,
        transitiveClosure: Array<IntArray>
    ): Array<IntArray> {
        val n = adjacencyMatrix.size

        // Compute the matrix product A*B (paths of length 2 or more)
        val matrixProduct = Array(n) { IntArray(n) { 0 } }

        for (i in 0 until n) {
            for (j in 0 until n) {
                // We're looking for paths that go through at least one intermediate vertex
                for (k in 0 until n) {
                    // There must be an edge from i to k and a path from k to j
                    // But k must not be j (otherwise it's just a direct edge)
                    if (k != j && adjacencyMatrix[i][k] == 1 && transitiveClosure[k][j] == 1) {
                        matrixProduct[i][j] = 1
                        break
                    }
                }
            }
        }

        // An edge (i,j) is in the reduction if it's in A but there's no alternate path
        val transitiveReduction = Array(n) { i ->
            adjacencyMatrix[i].copyOf()
        }

        for (i in 0 until n) {
            for (j in 0 until n) {
                // If there's a direct edge AND a path of length 2 or more,
                // remove the direct edge since it's redundant
                if (adjacencyMatrix[i][j] == 1 && matrixProduct[i][j] == 1) {
                    transitiveReduction[i][j] = 0
                }
            }
        }

        return transitiveReduction
    }
}