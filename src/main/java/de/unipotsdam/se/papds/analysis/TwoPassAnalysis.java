package de.unipotsdam.se.papds.analysis;

import de.unipotsdam.se.papds.graphlib.Graph;

public interface TwoPassAnalysis {

    //Transforms a PointsToGraph to a Push Down Setup
    void setUpAnalysis(Graph ptg, boolean fieldphase, boolean contextphase);

    //
    void doAnalysis(Graph ptg);
}
