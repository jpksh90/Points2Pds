package de.unipotsdam.se.papds.graphlib;

import java.util.Objects;

public class Edge {
    private Node u;
    private Node v;
    private Label label;

    public Edge(Node u, Node v, Label label) {
        this.u = u;
        this.v = v;
        this.label = label;
    }

    public Node getU() {
        return u;
    }

    public Node getV() {
        return v;
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Edge edge = (Edge) o;
        return Objects.equals(u, edge.u) &&
                Objects.equals(v, edge.v) &&
                Objects.equals(label, edge.label);
    }

    @Override
    public int hashCode() {

        return Objects.hash(u, v, label);
    }

    @Override
    public String toString() {
        return "Edge{" +
                "u=" + u +
                ", v=" + v +
                ", label=" + label +
                '}';
    }
}
