/**
 * Wrapper over guava graph library
 *
 * @author : jp
 */
package de.unipotsdam.se.papds.graphlib;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.ibm.wala.sourcepos.Debug;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Graph {


    private MutableValueGraph<Node, Label> graph;

    public Graph() {
        graph = ValueGraphBuilder.directed().allowsSelfLoops(true).build();
    }

    public void addNode(Node u) {
        graph.addNode(u);
    }

    public void addEdge(Node u, Node v, Label l) {
        graph.putEdgeValue(u, v, l);
        Debug.debug("Added a new edge  {" + u + "," + v + "," + l + "}");
    }

    public boolean hasEdge(Node u, Node v) {
        return graph.hasEdgeConnecting(u, v);
    }

    public Iterator<EndpointPair<Node>> getEdges() {
        return graph.edges().iterator();
    }

    /**
     * @return A triple containing edges and labels
     */
    public Iterator<Edge> getEdgesAndLabels() {
        HashSet<Edge> result = new HashSet<>();
        graph.edges().forEach(q -> {
            Edge e = new Edge(q.nodeU(), q.nodeV(), graph.edgeValueOrDefault(q.nodeU(), q.nodeV(), new Label(lbl.DUMMY)));
            result.add(e);
        });
        return result.iterator();
    }

    public int getNumberOfNodes() {
        return graph.nodes().size();
    }

    public int getNumberOfEdges() {
        return graph.edges().size();
    }


    public Set<Node> getNodes() {
        return this.graph.nodes();
    }
}