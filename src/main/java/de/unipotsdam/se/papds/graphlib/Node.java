package de.unipotsdam.se.papds.graphlib;

public class Node {
    private String node;
    private boolean isObject;
    private boolean isVariable;

    public Node() {
        node = "";
        isObject = false;
        isVariable = false;
    }

    public Node(String node) {
        this.node = node;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node1 = (Node) o;

        return node != null ? node.equals(node1.node) : node1.node == null;
    }

    @Override
    public int hashCode() {
        return node != null ? node.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Node{" +
                "node='" + node + '\'' +
                '}';
    }

    public void setIsObject() {
        isObject = true;
        isVariable = false;
    }

    public void setIsVariable() {
        isVariable = true;
        isObject = true;
    }

    public boolean getIsObject() {
        return isObject;
    }

    public boolean getIsVariable() {
        return isVariable;
    }
}
