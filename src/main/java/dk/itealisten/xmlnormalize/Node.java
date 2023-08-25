package dk.itealisten.xmlnormalize;

import java.util.ArrayList;
import java.util.List;

public class Node {


    private final String name;

    public Node(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    private List<Node> children = new ArrayList<>();

    public Node addChild(Node child) {
        children.add(child);
        return this;
    }

    public List<Node> getChildren() {
        return children;
    }

    public String toString() {
        return "Node(name='" + name + "', children=" + children.toString() + ")";
    }
}
