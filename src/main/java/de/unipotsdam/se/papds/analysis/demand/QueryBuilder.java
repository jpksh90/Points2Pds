package de.unipotsdam.se.papds.analysis.demand;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class QueryBuilder {

    private Set<CGNode> entryNodes;


    public QueryBuilder(Set<CGNode> entryNodes) {
        this.entryNodes = entryNodes;
    }

    public List<Query> buildPointerQueries() {
        List<Query> queries = new ArrayList<>();
        for (CGNode entryNode : entryNodes) {
            IR mainIR = entryNode.getIR();
            for (int i = 1; i < mainIR.getSymbolTable().getMaxValueNumber(); ++i) {
                Query q = new Query(entryNode.getMethod(),i);
                queries.add(q);
            }
        }
        return queries;
    }
}
