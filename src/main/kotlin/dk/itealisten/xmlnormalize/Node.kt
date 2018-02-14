package dk.itealisten.xmlnormalize

class Node
constructor(val name: String) {

    private val children = ArrayList<Node>()

    fun addChild(child: Node): Node {
        children.add(child)
        return this
    }

    fun getChildren(): Array<Node> {
        return children.toTypedArray()
    }

    override fun toString(): String {
        return "Node(name='$name', children=$children)"
    }


}
