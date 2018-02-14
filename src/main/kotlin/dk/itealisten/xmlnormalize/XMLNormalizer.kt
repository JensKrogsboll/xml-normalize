package dk.itealisten.xmlnormalize

import com.google.common.base.Joiner
import java.io.*
import java.util.*
import javax.xml.transform.*
import javax.xml.transform.sax.SAXResult
import javax.xml.transform.sax.SAXTransformerFactory
import javax.xml.transform.sax.TransformerHandler
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

/**
 * XMLNormalizer
 */
class XMLNormalizer @Throws(TransformerConfigurationException::class)
constructor(configuration: Configuration) {

    private val cfg = configuration
    private val stf = TransformerFactory.newInstance() as SAXTransformerFactory
    private val transformer: Transformer
    private val templates = ArrayList<Templates>()

    init {
        // Add templates for namespace removal and node sorting
        templates.add(stf.newTemplates(StreamSource(StringReader(XSLT_REMOVE_NS))))
        templates.add(stf.newTemplates(StreamSource(StringReader(XSLT_SORT_NODES))))

        // Add template for ignores
        if (!cfg.getIgnores().isEmpty()) {
            templates.add(stf.newTemplates(StreamSource(StringReader(buildIgnoreXslt(cfg.getIgnores())))))
        }

        // Sort the sortMap to make sure inner lists are sorted before outer
        val sortedSortMap = LinkedHashMap<String, Collection<String>>()
        sortSortMap(buildSortMap(cfg.getSorts()), sortedSortMap, null, "|")

        // Add templates for sorts
        for (sort in sortedSortMap.entries) {
            templates.add(stf.newTemplates(StreamSource(StringReader(buildSortXslt(sort)))))
        }

        // Add template for prettifying
        templates.add(stf.newTemplates(StreamSource(StringReader(XSLT_PRETTY))))

        // Create the transformer
        this.transformer = stf.newTransformer()
    }

    /**
     * Build the sort map based on the input sort list
     * @param sorts
     * @return
     */
    private fun buildSortMap(sorts: Array<Node>): MutableMap<String, Collection<String>> {
        val sortMap = TreeMap<String, Collection<String>>()
        for (parent in sorts) {
            if (sortMap.keys.contains(parent.name)) {
                throw IllegalArgumentException("Duplicate sort parent: " + parent.name)
            }
            sortMap[parent.name] = parent.getChildren().map { it.name }
        }
        return sortMap
    }

    /**
     * Sort the sort map to make sure inner lists are sorted before outer
     * @param sortMap
     * @param sortedMap
     * @param entry
     * @param stack
     */
    private fun sortSortMap(
        sortMap: MutableMap<String, Collection<String>>,
        sortedMap: MutableMap<String, Collection<String>>,
        entry: String?,
        stack: String) {

        if (entry != null) {
            for (sort in sortMap[entry]!!) {
                if (!stack.contains("|$sort|") && sortMap.containsKey(sort)) {
                    sortSortMap(sortMap, sortedMap, sort, stack + sort + "|")
                }
            }

            if (!sortedMap.containsKey(entry)) {
                sortedMap[entry] = sortMap[entry]!!
                sortMap.remove(entry)
            }
        }

        for (nextEntry in sortMap.keys) {
            sortSortMap(sortMap, sortedMap, nextEntry, "|$nextEntry|")
            break
        }
    }

    /**
     * Build the XSLT for ignores
     * @param ignores
     * @return
     */
    private fun buildIgnoreXslt(ignores: Array<Node>): String {
        return XSLT_IGNORE
            .replace("{ignoreList}", Joiner.on('|').join(ignores.map { it.name }))
    }


    /**
     * Build XSLT for a single sort parent
     * @param sort
     * @return
     */
    private fun buildSortXslt(sort: Map.Entry<String, Collection<String>>): String {
        val allSorts = StringBuilder()
        val childSorts = StringBuilder()
        val childNodes = StringBuilder()
        for (child in sort.value) {
            childSorts.append(XSLT_SORT_LIST_CHILD.replace("{listItemNodeName}", child))
            childNodes.append("|self::").append(child)
        }
        childNodes.deleteCharAt(0)
        allSorts.append(
            XSLT_SORT_LIST_PARENT
                .replace("{listParentNodeName}", sort.key)
                .replace("{listItemNodeNames}", childNodes.toString())
                .replace("{listItemSort}", childSorts.toString())
        ).append("\n")
        return XSLT_SORT_LISTS
            .replace("{listSortTemplates}", allSorts.toString())
    }

    /**
     * Transform
     * @param source
     * @param result
     * @throws TransformerException
     * @throws IOException
     */
    @Throws(TransformerException::class, IOException::class)
    fun transform(source: Source, result: Writer) {
        val handlers = arrayOfNulls<TransformerHandler>(templates.size)
        for (i in handlers.indices) {
            handlers[i] = stf.newTransformerHandler(templates[i])
        }
        for (i in 0 until handlers.size - 1) {
            handlers[i]!!.setResult(SAXResult(handlers[i + 1]))
        }
        handlers[handlers.size - 1]!!.setResult(StreamResult(result))
        try {
            transformer.transform(source, SAXResult(handlers[0]))
        } finally {
            result.close()
        }
    }

    /**
     * Transform
     * @param source
     * @param target
     * @throws TransformerException
     * @throws IOException
     */
    @Throws(TransformerException::class, IOException::class)
    fun transform(source: File, target: File) {
        transform(StreamSource(FileReader(source)), FileWriter(target))
    }

    /**
     * Transform
     * @param xml
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    @Throws(TransformerException::class, IOException::class)
    fun transform(xml: String): String {
        val result = StringWriter()
        transform(StreamSource(StringReader(xml)), result)
        return result.toString().replace("&#13;".toRegex(), "")
    }

    companion object {
        private val XSLT_REMOVE_NS = XMLNormalizer::class.java.getResource("/xslt/remove-namespaces.xslt").readText()
        private val XSLT_SORT_NODES = XMLNormalizer::class.java.getResource("/xslt/sort-nodes.xslt").readText()
        private val XSLT_IGNORE = XMLNormalizer::class.java.getResource("/xslt/ignore.xslt").readText()
        private val XSLT_SORT_LISTS = XMLNormalizer::class.java.getResource("/xslt/sort-lists.xslt").readText()
        private val XSLT_SORT_LIST_PARENT = XMLNormalizer::class.java.getResource("/xslt/sort-list-parent.xslt").readText()
        private val XSLT_SORT_LIST_CHILD = XMLNormalizer::class.java.getResource("/xslt/sort-list-child.xslt").readText()
        private val XSLT_PRETTY = XMLNormalizer::class.java.getResource("/xslt/pretty.xslt").readText()
    }
}
