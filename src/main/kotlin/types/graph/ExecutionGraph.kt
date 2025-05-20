package ar.edu.itba.pf.types.graph

import ar.edu.itba.pf.types.Context
import java.util.LinkedList

class ExecutionGraph {
    private val heads: LinkedList<ExecutionNode> = LinkedList()
    private val leaves: LinkedList<ExecutionNode> = LinkedList()
    private val nodeMap: MutableMap<String, ExecutionNode> = mutableMapOf()

    fun getNodeByName(name: String): Pair<String, String>? {
        return nodeMap
            .filter { (id, node) -> node.endpointName == name }
            .map { (_, node) -> node.id to node.endpointName }
            .first()
    }

    // Add a root node (one without dependencies)
    fun addRoot(executionId: String, endpointName: String): String {
        val node = ExecutionNode(executionId, endpointName)
        heads.add(node)
        leaves.add(node)
        nodeMap[executionId] = node
        return executionId
    }

    // Add a child node to all current leaves
    fun addChild(executionId: String, endpointName: String): String {
        if (leaves.isEmpty()) {
            throw IllegalStateException("Cannot add child: graph has no leaves")
        }

        val childNode = ExecutionNode(executionId, endpointName)
        nodeMap[executionId] = childNode

        // Add this child to all current leaves and set the leaves as parents
        leaves.forEach { leaf ->
            leaf.addChild(childNode)
            childNode.addParent(leaf)
        }

        // This node becomes the new leaf, removing all previous leaves
        leaves.clear()
        leaves.add(childNode)

        return executionId
    }

    // Get a node by its execution ID
    fun getNode(id: String): ExecutionNode? {
        return nodeMap[id]
    }

    // Creates a deep copy of the graph for isolated execution
    fun copy(): ExecutionGraph {
        val newGraph = ExecutionGraph()
        val nodeMapping = mutableMapOf<String, ExecutionNode>()

        // First pass: create all nodes
        for (nodeId in nodeMap.keys) {
            val originalNode = nodeMap[nodeId]!!
            val newNode = ExecutionNode(originalNode.id, originalNode.endpointName)
            nodeMapping[nodeId] = newNode
            newGraph.nodeMap[nodeId] = newNode
        }

        // Second pass: establish relationships
        for (nodeId in nodeMap.keys) {
            val originalNode = nodeMap[nodeId]!!
            val newNode = nodeMapping[nodeId]!!

            // Add children
            for (child in originalNode.getChildren()) {
                val newChild = nodeMapping[child.id]!!
                newNode.addChild(newChild)
                newChild.addParent(newNode)
            }
        }

        // Third pass: set heads and leaves
        for (head in heads) {
            newGraph.heads.add(nodeMapping[head.id]!!)
        }

        for (leaf in leaves) {
            newGraph.leaves.add(nodeMapping[leaf.id]!!)
        }

        return newGraph
    }

    // Creates a new graph by merging this and another graph
    fun mergeWith(other: ExecutionGraph): ExecutionGraph {
        val mergedGraph = this.copy()
        val nodeMapping = mutableMapOf<String, ExecutionNode>()

        // First, map all nodes that already exist in this graph
        for (nodeId in mergedGraph.nodeMap.keys) {
            nodeMapping[nodeId] = mergedGraph.nodeMap[nodeId]!!
        }

        // Copy nodes from other graph that don't exist in this graph
        for (nodeId in other.nodeMap.keys) {
            if (!mergedGraph.nodeMap.containsKey(nodeId)) {
                val otherNode = other.nodeMap[nodeId]!!
                val newNode = ExecutionNode(otherNode.id, otherNode.endpointName)
                mergedGraph.nodeMap[nodeId] = newNode
                nodeMapping[nodeId] = newNode
            }
        }

        // Establish relationships from the other graph
        for (nodeId in other.nodeMap.keys) {
            val otherNode = other.nodeMap[nodeId]!!
            val mergedNode = nodeMapping[nodeId]!!

            // Add children relationships from other graph
            for (otherChild in otherNode.getChildren()) {
                val mergedChild = nodeMapping[otherChild.id]!!

                // Only add the relationship if it doesn't already exist
                if (!mergedNode.getChildren().any { it.id == otherChild.id }) {
                    mergedNode.addChild(mergedChild)
                    mergedChild.addParent(mergedNode)
                }
            }
        }

        // Update heads: add any head from other that isn't in merged graph's heads
        for (otherHead in other.heads) {
            val correspondingNode = nodeMapping[otherHead.id]!!
            if (!mergedGraph.heads.any { it.id == otherHead.id }) {
                mergedGraph.heads.add(correspondingNode)
            }
        }

        // Update leaves: use union of leaves from both graphs
        mergedGraph.leaves.clear()

        // Add leaves from the first graph
        for (leaf in leaves) {
            val correspondingNode = nodeMapping[leaf.id]!!
            mergedGraph.leaves.add(correspondingNode)
        }

        // Add leaves from the second graph if they don't already exist
        for (otherLeaf in other.leaves) {
            val correspondingNode = nodeMapping[otherLeaf.id]!!
            if (!mergedGraph.leaves.any { it.id == otherLeaf.id }) {
                mergedGraph.leaves.add(correspondingNode)
            }
        }

        return mergedGraph
    }

    // Get the current leaf nodes
    fun getLeaves(): List<ExecutionNode> {
        return leaves.toList()
    }

    // Get the head nodes
    fun getHeads(): List<ExecutionNode> {
        return heads.toList()
    }

    // For debugging and visualization
    fun printGraph() {
        println("Execution Graph:")
        println("Heads: ${heads.map { it.id }.joinToString()}")
        println("Leaves: ${leaves.map { it.id }.joinToString()}")
        println("Structure:")
        heads.forEach { head ->
            printNode(head, 0, mutableSetOf())
        }
    }

    private fun printNode(node: ExecutionNode, depth: Int, visited: MutableSet<String>) {
        if (node.id in visited) {
            println("${" ".repeat(depth * 2)}${node.endpointName} (${node.id}) [CYCLE DETECTED]")
            return
        }

        visited.add(node.id)
        println("${" ".repeat(depth * 2)}${node.endpointName} (${node.id})")

        node.getChildren().forEach { child ->
            printNode(child, depth + 1, visited.toMutableSet())
        }
    }

    /**
     * Finds the closest common ancestors between the leaves of this graph
     * and another graph from a different context.
     * Uses endpointName instead of id for comparing nodes.
     */
    fun findCommonAncestors(otherGraph: ExecutionGraph): List<String> {
        // Get leaves from both graphs
        val thisLeaves = this.getLeaves()
        val otherLeaves = otherGraph.getLeaves()

        // If either graph has no leaves, there can't be common ancestors
        if (thisLeaves.isEmpty() || otherLeaves.isEmpty()) {
            return emptyList()
        }

        // Find all ancestors for each leaf with their distances
        val ancestorsWithDistance1 = mutableMapOf<String, Int>() // endpointName -> distance
        val ancestorsWithDistance2 = mutableMapOf<String, Int>() // endpointName -> distance

        // Process leaves from first graph
        for (leaf in thisLeaves) {
            findAncestorsWithBFS(leaf, ancestorsWithDistance1)
        }

        // Process leaves from second graph
        for (leaf in otherLeaves) {
            findAncestorsWithBFS(leaf, ancestorsWithDistance2)
        }

        // Find common ancestors (by endpointName)
        val commonAncestors = ancestorsWithDistance1.keys.intersect(ancestorsWithDistance2.keys)

        // If no common ancestors, return empty list
        if (commonAncestors.isEmpty()) {
            return emptyList()
        }

        // Find closest common ancestors (those with minimum sum of distances)
        var minDistance = Int.MAX_VALUE
        val closestAncestors = mutableListOf<String>()

        for (ancestorEndpoint in commonAncestors) {
            val distance1 = ancestorsWithDistance1[ancestorEndpoint] ?: Int.MAX_VALUE
            val distance2 = ancestorsWithDistance2[ancestorEndpoint] ?: Int.MAX_VALUE
            val totalDistance = distance1 + distance2

            if (totalDistance < minDistance) {
                minDistance = totalDistance
                closestAncestors.clear()
                closestAncestors.add(ancestorEndpoint)
            } else if (totalDistance == minDistance) {
                closestAncestors.add(ancestorEndpoint)
            }
        }

        return closestAncestors
    }

    /**
     * Uses BFS to find all ancestors of a node with their distances.
     * Takes advantage of the parent references in nodes.
     * Maps endpointName to distance instead of id.
     */
    private fun findAncestorsWithBFS(
        startNode: ExecutionNode,
        result: MutableMap<String, Int>
    ) {
        val visited = mutableSetOf<String>() // Track visited nodes by id to avoid cycles
        val queue = LinkedList<Pair<ExecutionNode, Int>>() // (node, distance)

        // Start with the node itself (distance 0)
        queue.add(Pair(startNode, 0))

        while (queue.isNotEmpty()) {
            val (currentNode, distance) = queue.poll()

            // Skip if already visited with a shorter path
            if (currentNode.id in visited &&
                (result[currentNode.endpointName] ?: Int.MAX_VALUE) <= distance
            ) {
                continue
            }

            // Mark as visited by id
            visited.add(currentNode.id)

            // Update result with minimum distance using endpointName as key
            val currentMinDistance = result[currentNode.endpointName] ?: Int.MAX_VALUE
            if (distance < currentMinDistance) {
                result[currentNode.endpointName] = distance
            }

            // Add all parents to the queue with incremented distance
            currentNode.getParents().forEach { parent ->
                queue.add(Pair(parent, distance + 1))
            }
        }
    }

    class ExecutionNode(val id: String, val endpointName: String) {
        private val children: LinkedList<ExecutionNode> = LinkedList()
        private val parents: LinkedList<ExecutionNode> = LinkedList()

        fun addChild(node: ExecutionNode) {
            if (!children.any { it.id == node.id }) {
                children.add(node)
            }
        }

        fun addParent(node: ExecutionNode) {
            if (!parents.any { it.id == node.id }) {
                parents.add(node)
            }
        }

        fun getChildren(): List<ExecutionNode> {
            return children.toList()
        }

        fun getParents(): List<ExecutionNode> {
            return parents.toList()
        }
    }
}