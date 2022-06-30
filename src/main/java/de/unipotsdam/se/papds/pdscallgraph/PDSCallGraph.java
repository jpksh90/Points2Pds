package de.unipotsdam.se.papds.pdscallgraph;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.callgraph.impl.FakeWorldClinitMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.MonitorUtil;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.intset.IntSet;

import java.util.*;

/**
 * Implements a Function sensitive heap abstraction
 */

public class PDSCallGraph implements CallGraph {


    /*
        CallGraph is defined as a mutable graph
     */
    MutableGraph<CGNode> callgraph = GraphBuilder.directed().allowsSelfLoops(true).build();


    /*
        Map each call graph node with IMethod and Context
     */
    Map<IMethod, Map<Context, CGNode>> methodContextToCGNode;


    /*
        Map each method reference to a node
     */
    Map<MethodReference, CGNode> methodRefToNode;

    /*
        Set of entry nodes
     */
    Set<CGNode> entryNodes = Collections.EMPTY_SET;

    /*

     */
    Set<CGNode> callgraphnodes = new HashSet<CGNode>();


    private IClassHierarchy cha;
    private AnalysisCache cache;
    private AnalysisOptions options;
    private MonitorUtil.IProgressMonitor iProgressMonitor;


    public PDSCallGraph(IClassHierarchy cha, AnalysisCache cache, AnalysisOptions options, MonitorUtil.IProgressMonitor iProgressMonitor) {
        this.cha = cha;
        this.cache = cache;
        this.options = options;
        this.iProgressMonitor = iProgressMonitor;
        this.methodContextToCGNode = HashMapFactory.make();
        this.methodRefToNode = HashMapFactory.make();

    }

    @Override
    public CGNode getFakeRootNode() {
        PDSCGNode fakeRoot = new PDSCGNode(new FakeRootMethod(cha, options, cache));
        callgraph.addNode(fakeRoot);
        return fakeRoot;
    }

    @Override
    public CGNode getFakeWorldClinitNode() {
        PDSCGNode fakeWorldClinitNode = new PDSCGNode(new FakeWorldClinitMethod(cha, options, cache));
        callgraph.addNode(fakeWorldClinitNode);
        return fakeWorldClinitNode;
    }

    private Set<IMethod> methodforMethodReference(MethodReference mr) {
        return cha.getPossibleTargets(mr);
    }


    public boolean addCGNode(CGNode t) {
        if (callgraphnodes.contains(t)) {
            return false; //Not added
        }
        callgraphnodes.add(t);
        return true;
    }

    @Override
    public Collection<CGNode> getEntrypointNodes() {
        return entryNodes;
    }

    @Override
    public CGNode getNode(IMethod method, Context C) {
        return null;
    }

    @Override
    public Set<CGNode> getNodes(MethodReference m) {
        return null;
    }

    @Override
    public IClassHierarchy getClassHierarchy() {
        return cha;
    }

    @Override
    public Set<CGNode> getPossibleTargets(CGNode node, CallSiteReference site) {
        //TODO: Finish this part
        return null;
    }

    @Override
    public int getNumberOfTargets(CGNode node, CallSiteReference site) {
        return 0;
    }

    @Override
    public Iterator<CallSiteReference> getPossibleSites(CGNode src, CGNode target) {
        return null;
    }

    @Override
    public void removeNodeAndEdges(CGNode n) throws UnsupportedOperationException {
        callgraph.adjacentNodes(n).forEach(q -> callgraph.removeEdge(n, q));
        callgraph.removeNode(n);
    }

    @Override
    public Iterator<CGNode> iterator() {
        return null;
    }

    @Override
    public int getNumberOfNodes() {
        return callgraph.nodes().size();
    }

    @Override
    public void addNode(CGNode n) {

        callgraph.addNode(n);
    }

    @Override
    public void removeNode(CGNode n) throws UnsupportedOperationException {
        callgraph.removeNode(n);
    }

    @Override
    public boolean containsNode(CGNode n) {
        return callgraph.nodes().contains(n);
    }

    @Override
    public IntSet getSuccNodeNumbers(CGNode node) {
        return null;
    }

    @Override
    public IntSet getPredNodeNumbers(CGNode node) {
        return null;
    }

    @Override
    public Iterator<CGNode> getPredNodes(CGNode n) {
        return callgraph.predecessors(n).iterator();
    }

    @Override
    public int getPredNodeCount(CGNode n) {
        return callgraph.predecessors(n).size();
    }

    @Override
    public Iterator<CGNode> getSuccNodes(CGNode n) {
        return callgraph.successors(n).iterator();
    }

    @Override
    public int getSuccNodeCount(CGNode N) {
        return callgraph.successors(N).size();
    }

    @Override
    public void addEdge(CGNode src, CGNode dst) {
        callgraph.putEdge(src, dst);
    }

    @Override
    public void removeEdge(CGNode src, CGNode dst) throws UnsupportedOperationException {
        callgraph.removeEdge(src, dst);
    }

    @Override
    public void removeAllIncidentEdges(CGNode node) throws UnsupportedOperationException {
        removeIncomingEdges(node);
        removeOutgoingEdges(node);
    }

    @Override
    public void removeIncomingEdges(CGNode node) throws UnsupportedOperationException {
        Set<CGNode> pred = callgraph.predecessors(node);
        pred.forEach(q -> callgraph.removeEdge(q, node));
    }

    @Override
    public void removeOutgoingEdges(CGNode node) throws UnsupportedOperationException {
        Set<CGNode> succ = callgraph.successors(node);
        succ.forEach(q -> callgraph.removeEdge(node, q));
    }

    @Override
    public boolean hasEdge(CGNode src, CGNode dst) {
        return callgraph.hasEdgeConnecting(src, dst);
    }

    @Override
    public int getNumber(CGNode N) {
        return 0;
    }

    @Override
    public CGNode getNode(int number) {
        return null;
    }

    @Override
    public int getMaxNumber() {
        return callgraph.nodes().size();
    }

    @Override
    public Iterator<CGNode> iterateNodes(IntSet s) {
        return null;
    }


    public void printCallGraph() {

    }
}
