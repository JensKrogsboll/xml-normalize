package dk.itealisten.xmlnormalize;

import java.util.List;

public class Configuration {

    private final List<Node> ignore;
    private final List<Node> sort;

    public Configuration(List<Node> ignore, List<Node> sort) {
        this.ignore = ignore;
        this.sort = sort;
    }

    public List<Node> getIgnore() {
        return ignore;
    }

    public List<Node> getSort() {
        return sort;
    }
}
