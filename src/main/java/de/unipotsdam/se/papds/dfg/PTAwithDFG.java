/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 * This program compares PTAwithDFG for a demand flow graph implementation in WALA
 */

package de.unipotsdam.se.papds.dfg;

import com.ibm.wala.demandpa.flowgraph.SimpleDemandPointerFlowGraph;
import com.ibm.wala.demandpa.util.MemoryAccessMap;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * This is for experimental purpose. We need to compare it with DemandPointerFlowGraph
 */
public class PTAwithDFG {

    private SimpleDemandPointerFlowGraph dfg;

    public PTAwithDFG(CallGraph cg, MemoryAccessMap mam, IClassHierarchy h, HeapModel model) {
        dfg = new SimpleDemandPointerFlowGraph(cg, model, mam, h);
    }

    //travel the DFG


    public void travelDFG() {
        for (int i = 0; i < dfg.getNumberOfNodes(); ++i) {
            System.out.println(dfg.getNode(i));
        }
    }


}
