package de.unipotsdam.se.papds.analysis;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.util.intset.IntSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class PDSHeapGraph implements HeapGraph {
    @Override
    public Collection<Object> getReachableInstances(Set set) {
        return null;
    }

    @Override
    public HeapModel getHeapModel() {
        return null;
    }

    @Override
    public PointerAnalysis getPointerAnalysis() {
        return null;
    }

    @Override
    public void removeNodeAndEdges(Object o) throws UnsupportedOperationException {

    }

    @Override
    public IntSet getSuccNodeNumbers(Object o) {
        return null;
    }

    @Override
    public IntSet getPredNodeNumbers(Object o) {
        return null;
    }

    @Override
    public Iterator getPredNodes(Object o) {
        return null;
    }

    @Override
    public int getPredNodeCount(Object o) {
        return 0;
    }

    @Override
    public Iterator getSuccNodes(Object o) {
        return null;
    }

    @Override
    public int getSuccNodeCount(Object o) {
        return 0;
    }

    @Override
    public void addEdge(Object o, Object t1) {

    }

    @Override
    public void removeEdge(Object o, Object t1) throws UnsupportedOperationException {

    }

    @Override
    public void removeAllIncidentEdges(Object o) throws UnsupportedOperationException {

    }

    @Override
    public void removeIncomingEdges(Object o) throws UnsupportedOperationException {

    }

    @Override
    public void removeOutgoingEdges(Object o) throws UnsupportedOperationException {

    }

    @Override
    public boolean hasEdge(Object o, Object t1) {
        return false;
    }

    @Override
    public int getNumber(Object o) {
        return 0;
    }

    @Override
    public Object getNode(int i) {
        return null;
    }

    @Override
    public int getMaxNumber() {
        return 0;
    }

    @Override
    public Iterator iterateNodes(IntSet intSet) {
        return null;
    }

    @Override
    public Iterator iterator() {
        return null;
    }

    @Override
    public int getNumberOfNodes() {
        return 0;
    }

    @Override
    public void addNode(Object o) {

    }

    @Override
    public void removeNode(Object o) throws UnsupportedOperationException {

    }

    @Override
    public boolean containsNode(Object o) {
        return false;
    }
}
