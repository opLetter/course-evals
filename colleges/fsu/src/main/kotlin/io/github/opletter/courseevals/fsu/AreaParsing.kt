package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.substringAfterBefore

// Note: almost all the code in this file was written by ChatGPT

data class AreaEntry(
    val id: Int,
    val code: String,
    val name: String,
    val indentLevel: Int,
) {
    val cleanName get() = name.substringAfter("$code - ").replace("&amp;", "&")

    companion object {
        fun fromString(s: String): AreaEntry {
            val name = s
                .replace("&#160;", "")
                .substringAfterBefore(">", "<")
            return AreaEntry(
                id = s.substringAfterBefore("value=\"", "\"").toInt(),
                code = name.substringBefore(" - ", ""),
                name = name,
                indentLevel = (s.split("&#160;").size - 1) / 5,
            )
        }
    }
}

data class Node(val value: AreaEntry?, val children: MutableList<Node> = mutableListOf())

fun buildTree(objects: List<AreaEntry>): Map<String, Node> {
    val root = Node(null)
    val stack = mutableListOf<Node>()
    val nodes = mutableMapOf<String, Node>()
    stack.add(root)
    nodes[root.value?.name ?: ""] = root
    var prevLevel = -1
    for (obj in objects) {
        val level = obj.indentLevel
        val node = Node(obj)
        nodes[node.value!!.name] = node
        if (level == prevLevel + 1) {
            stack.last().children.add(node)
            stack.add(node)
        } else {
            while (stack.size > 1 && level <= stack.last().value!!.indentLevel) {
                stack.removeLast()
            }
            stack.last().children.add(node)
            stack.add(node)
        }
        prevLevel = level
    }
    return nodes
}

fun findChildren(node: Node): List<Node> {
    val children = mutableListOf<Node>()
    for (child in node.children) {
        children.add(child)
        children.addAll(findChildren(child))
    }
    return children
}

fun buildChildParentMap(nodes: Map<String, Node>, uniqueCodes: Set<String>): Map<String, String> {
    val childParentMap = mutableMapOf<String, String>()
    for ((value, node) in nodes) {
        if (node.value?.indentLevel != 2) continue
        value.substringBefore(" - ", "").takeIf { it in uniqueCodes }?.let {
            childParentMap[it] = value
        }
        childParentMap[value] = value
        val children = findChildren(node)
        for (child in children) {
            child.value!!.name.substringBefore(" - ", "").takeIf { it in uniqueCodes }?.let {
                childParentMap[it] = value
            }
            childParentMap[child.value.name] = value
        }
    }
//    childParentMap["Panama City Campus"] = "Panama City Campus"
//    childParentMap["Sarasota Campus"] = "Sarasota Campus"
    return childParentMap
}
