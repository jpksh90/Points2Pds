package de.unipotsdam.se.papds.pdscallgraph;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;
import de.unipotsdam.se.papds.analysis.FunctionBasedHeapModel;
import de.unipotsdam.se.papds.analysis.coreStructures.VariableKey;

import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;

public class PDSCGNode implements CGNode {

    private IMethod method;
    private IR ir;
    private Vector<Pair<CallSiteReference, CGNode>> callsitesMap;
    private int number;


    private VariableKey returnKey;

    public PDSCGNode(IMethod method) {
        this.method = method;

        //Add parameter nodes for each parameter
        getParameters();
        returnKey = FunctionBasedHeapModel.getVariableKey(method, Integer.MAX_VALUE);
    }


    private void getParameters() {
        for (int i = 0; i < method.getNumberOfParameters(); i++) {
            FunctionBasedHeapModel.getVariableKey(method, i);
        }
    }


    public VariableKey getReturnKey() {
        return returnKey;
    }

    /**
     * Return the {@link IMethod method} this CGNode represents.
     * This value will never be <code>null</code>.
     *
     * @return the target IMethod for this CGNode.
     */
    @Override
    public IMethod getMethod() {
        return this.method;
    }

    /**
     * Return the {@link Context context} this CGNode represents.
     * This value will never be <code>null</code>.
     *
     * @return the Context for this CGNode.
     */
    @Override
    public Context getContext() {
        Assertions.UNREACHABLE(); //Since, we are not going for a
        return null;
    }

    /**
     * This is for use only by call graph builders ... not by the general
     * public.  Clients should not use this.
     * <p>
     * Record that a particular call site might resolve to a call to a
     * particular target node.  Returns true if this is a new target
     *
     * @param site
     * @param target
     */
    @Override
    public boolean addTarget(CallSiteReference site, CGNode target) {
        Pair<CallSiteReference, CGNode> pair = Pair.make(site, target);
        if (!callsitesMap.contains(pair)) {
            callsitesMap.add(pair);
            return true;
        } else {
            return false;
        }
    }


    /**
     * @return the "default" IR for this node used by the governing call graph
     */
    @Override
    public IR getIR() {
        this.ir = new AnalysisCacheImpl().getIRFactory().makeIR(this.method, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
        return ir;
    }

    /**
     * @return DefUse for the "default" IR for this node used by the governing call graph
     */
    @Override
    public DefUse getDU() {
        Assertions.UNREACHABLE();
        return null;
    }

    /**
     * @return an Iterator of the types that may be allocated by a given
     * method in a given context.
     */
    @Override
    public Iterator<NewSiteReference> iterateNewSites() {
        return this.ir.iterateNewSites();
    }

    /**
     * @return an Iterator of the call statements that may execute
     * in a given method for a given context
     */
    @Override
    public Iterator<CallSiteReference> iterateCallSites() {
        return this.ir.iterateCallSites();
    }

    @Override
    public IClassHierarchy getClassHierarchy() {
        return null;
    }

    /**
     * A non-negative integer which serves as an identifier for this node in
     * it's "dominant" graph.  Initially this number is -1; a NumberedGraph
     * will set it to a non-negative value when this node is inserted into
     * the graph
     *
     * @return the identifier
     */
    @Override
    public int getGraphNodeId() {
        return 0;
    }

    /**
     * @param number
     */
    @Override
    public void setGraphNodeId(int number) {
        this.number = Objects.hashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PDSCGNode pdscgNode = (PDSCGNode) o;
        return Objects.equals(method, pdscgNode.method);
    }

    @Override
    public int hashCode() {

        return Objects.hash(method);
    }


}
