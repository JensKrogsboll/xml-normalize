package dk.itealisten.xmlnormalize

class Configuration
constructor(ignoreNodes: Array<Node>, sortNodes: Array<Node>) {

    private val ignore = ArrayList<Node>()
    private val sort = ArrayList<Node>()

    init {
        ignore.addAll(ignoreNodes)
        sort.addAll(sortNodes)
    }

    fun getIgnores(): Array<Node> {
        return ignore.toTypedArray()
    }

    fun getSorts(): Array<Node> {
        return sort.toTypedArray()
    }

    override fun toString(): String {
        return "Configuration(ignore=$ignore, sort=$sort)"
    }


}
