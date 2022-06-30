package de.unipotsdam.se.papds.analysis;

import com.ibm.wala.ipa.callgraph.propagation.HeapModel;

public class PDSPointsToAnalysis {

    private HeapModel hm;


    PDSPointsToAnalysis(HeapModel hm) {
        this.hm = hm;
    }

}
