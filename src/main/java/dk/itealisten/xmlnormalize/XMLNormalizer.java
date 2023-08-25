package dk.itealisten.xmlnormalize;

import com.google.common.base.Joiner;
import com.google.common.io.Resources;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class XMLNormalizer {

    private static final String XSLT_REMOVE_NS;
    private static final String XSLT_SORT_NODES;
    private static final String XSLT_IGNORE;
    private static final String XSLT_SORT_LISTS;
    private static final String XSLT_SORT_LIST_PARENT;
    private static final String XSLT_SORT_LIST_CHILD;
    private static final String XSLT_PRETTY;

    static {
        try {
            XSLT_REMOVE_NS = Resources.toString(XMLNormalizer.class.getResource("/xslt/remove-namespaces.xslt"), StandardCharsets.UTF_8);
            XSLT_SORT_NODES = Resources.toString(XMLNormalizer.class.getResource("/xslt/sort-nodes.xslt"), StandardCharsets.UTF_8);
            XSLT_IGNORE = Resources.toString(XMLNormalizer.class.getResource("/xslt/ignore.xslt"), StandardCharsets.UTF_8);
            XSLT_SORT_LISTS = Resources.toString(XMLNormalizer.class.getResource("/xslt/sort-lists.xslt"), StandardCharsets.UTF_8);
            XSLT_SORT_LIST_PARENT = Resources.toString(XMLNormalizer.class.getResource("/xslt/sort-list-parent.xslt"), StandardCharsets.UTF_8);
            XSLT_SORT_LIST_CHILD = Resources.toString(XMLNormalizer.class.getResource("/xslt/sort-list-child.xslt"), StandardCharsets.UTF_8);
            XSLT_PRETTY = Resources.toString(XMLNormalizer.class.getResource("/xslt/pretty.xslt"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final SAXTransformerFactory stf = (SAXTransformerFactory) TransformerFactory.newInstance();
    private final List<Templates> templates = new ArrayList<>();
    private final Transformer transformer;

    public XMLNormalizer(Configuration cfg) throws TransformerConfigurationException {
        // Add templates for namespace removal and node sorting
        templates.add(stf.newTemplates(new StreamSource(new StringReader(XSLT_REMOVE_NS))));
        templates.add(stf.newTemplates(new StreamSource(new StringReader(XSLT_SORT_NODES))));

        // Add template for ignores
        if (!cfg.getIgnore().isEmpty()) {
            templates.add(stf.newTemplates(new StreamSource(new StringReader(buildIgnoreXslt(cfg.getIgnore())))));
        }

        // Sort the sortMap to make sure inner lists are sorted before outer
        Map<String, List<String>> sortedSortMap = new LinkedHashMap<>();
        sortSortMap(buildSortMap(cfg.getSort()), sortedSortMap, null, "|");

        // Add templates for sorts
        for (Map.Entry<String, List<String>> sort : sortedSortMap.entrySet()) {
            templates.add(stf.newTemplates(new StreamSource(new StringReader(buildSortXslt(sort)))));
        }

        // Add template for prettifying
        templates.add(stf.newTemplates(new StreamSource(new StringReader(XSLT_PRETTY))));

        // Create the transformer
        this.transformer = stf.newTransformer();

    }

    /**
     * Build the sort map based on the input sort list
     *
     * @param sorts
     * @return
     */
    private Map<String, List<String>> buildSortMap(List<Node> sorts) {
        Map<String, List<String>> sortMap = new TreeMap<>();
        for (Node parent : sorts) {
            if (sortMap.keySet().contains(parent.getName())) {
                throw new IllegalArgumentException("Duplicate sort parent: " + parent.getName());
            }
            sortMap.put(parent.getName(), parent.getChildren().stream().map(Node::getName).collect(Collectors.toList()));
        }
        return sortMap;
    }

    /**
     * Sort the sort map to make sure inner lists are sorted before outer
     *
     * @param sortMap
     * @param sortedMap
     * @param entry
     * @param stack
     */
    private void sortSortMap(
            Map<String, List<String>> sortMap,
            Map<String, List<String>> sortedMap,
            String entry,
            String stack) {

        if (entry != null) {
            for (String sort : sortMap.get(entry)) {
                if (!stack.contains("|$sort|") && sortMap.containsKey(sort)) {
                    sortSortMap(sortMap, sortedMap, sort, stack + sort + "|");
                }
            }

            sortedMap.computeIfAbsent(entry, sortMap::remove);
        }

        sortMap.keySet().stream()
            .findFirst().ifPresent(nextEntry -> sortSortMap(sortMap, sortedMap, nextEntry, "|$nextEntry|"));
    }

    /**
     * Build the XSLT for ignores
     *
     * @param ignores
     * @return
     */
    private String buildIgnoreXslt(List<Node> ignores) {
        return XSLT_IGNORE
                .replace("{ignoreList}", Joiner.on('|').join(ignores.stream().map(Node::getName).collect(Collectors.toList())));
    }


    /**
     * Build XSLT for a single sort parent
     *
     * @param sort
     * @return
     */
    private String buildSortXslt(Map.Entry<String, List<String>> sort) {
        StringBuilder allSorts = new StringBuilder();
        StringBuilder childSorts = new StringBuilder();
        StringBuilder childNodes = new StringBuilder();
        for (String child : sort.getValue()) {
            childSorts.append(XSLT_SORT_LIST_CHILD.replace("{listItemNodeName}", child));
            childNodes.append("|self::").append(child);
        }
        childNodes.deleteCharAt(0);
        allSorts.append(
                XSLT_SORT_LIST_PARENT
                        .replace("{listParentNodeName}", sort.getKey())
                        .replace("{listItemNodeNames}", childNodes.toString())
                        .replace("{listItemSort}", childSorts.toString())
        ).append("\n");
        return XSLT_SORT_LISTS
                .replace("{listSortTemplates}", allSorts.toString());
    }

    /**
     * Transform
     *
     * @param source
     * @param result
     * @throws TransformerException
     * @throws IOException
     */
    private void transform(Source source, Writer result) throws TransformerException, IOException {
        final TransformerHandler[] handlers = new TransformerHandler[(templates.size())];
        for (int i = 0; i < handlers.length; i++) {
            handlers[i] = stf.newTransformerHandler(templates.get(i));
        }
        for (int i = 0; i < handlers.length - 1; i++) {
            handlers[i].setResult(new SAXResult(handlers[i + 1]));
        }
        handlers[handlers.length - 1].setResult(new StreamResult(result));
        try {
            transformer.transform(source, new SAXResult(handlers[0]));
        } finally {
            result.close();
        }
    }

    /**
     * Transform
     *
     * @param source
     * @param target
     * @throws TransformerException
     * @throws IOException
     */
    public void transform(File source, File target) throws IOException, TransformerException {
        transform(new StreamSource(new FileReader(source)), new FileWriter(target));
    }

    /**
     * Transform
     *
     * @param xml
     * @return
     * @throws TransformerException
     * @throws IOException
     */
    public String transform(String xml) throws IOException, TransformerException {
        StringWriter result = new StringWriter();
        transform(new StreamSource(new StringReader(xml)), result);
        return result.toString().replace("&#13;", "");
    }

}
