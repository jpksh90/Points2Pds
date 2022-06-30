package de.unipotsdam.se.papds.pdscallgraph;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.MonitorUtil;
import de.unipotsdam.se.papds.analysis.PDSPointerAnalysis;

public class PDSCallGraphBuilder implements CallGraphBuilder {

    private IClassHierarchy cha;
    private AnalysisCache cache;

    public PDSCallGraphBuilder(IClassHierarchy cha, AnalysisCache cache) {
        this.cha = cha;
        this.cache = cache;
    }

    @Override
    public CallGraph makeCallGraph(AnalysisOptions options, MonitorUtil.IProgressMonitor monitor) throws IllegalArgumentException {
        return new PDSCallGraph(cha, cache, options, monitor);
    }

    @Override
    public PointerAnalysis getPointerAnalysis() {

        return new PDSPointerAnalysis(cha);
    }

    @Override
    public IAnalysisCacheView getAnalysisCache() {
        return cache;
    }

    @Override
    public IClassHierarchy getClassHierarchy() {
        return cha;
    }


    public void addCallTarget(CGNode t, CGNode m) {

    }


}