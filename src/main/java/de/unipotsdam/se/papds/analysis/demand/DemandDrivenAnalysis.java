package de.unipotsdam.se.papds.analysis.demand;

import com.ibm.wala.sourcepos.Debug;
import de.unipotsdam.se.papds.analysis.FunctionBasedHeapModel;
import de.unipotsdam.se.papds.analysis.PTG2PDS;
import de.unipotsdam.se.papds.analysis.TwoPassAnalysis;
import de.unipotsdam.se.papds.graphlib.Graph;
import de.unipotsdam.se.papds.graphlib.VariableNode;
import de.unipotsdam.se.papds.main.Main;
import de.unipotsdam.se.papds.pds.PAutomaton;
import de.unipotsdam.se.papds.pds.PDS;
import de.unipotsdam.se.papds.pds.StackSymbol;
import de.unipotsdam.se.utils.Utils;

import java.io.IOException;

public class DemandDrivenAnalysis implements TwoPassAnalysis {

    private Graph graph;

    PTG2PDS ptg2PDS;

    PAutomaton initAutomaton;

    private PDS pds = new PDS();

    private final boolean debug = Main.debug;

    private Query query;


    @Override
    public void setUpAnalysis(Graph ptg, boolean fieldphase, boolean contextphase) {
        this.graph = ptg;
        long begin = System.currentTimeMillis();
        Debug.debug("\tBuilding PDS ");
        ptg2PDS = new PTG2PDS(pds, ptg, query);
        initAutomaton = ptg2PDS.getInitPAutomaton();
        ptg2PDS.buildPDS(fieldphase, contextphase);
        Debug.info(ptg2PDS.getPdsStatitics());

        long end = System.currentTimeMillis();
        Debug.debug("\tEnd Building PDS : Time Elapsed=" + (end - begin) / 1000.0 + "s");
    }


    public void addQuery(Query q) {
        this.query = q;
    }

    /**
     * Adds a final state to PAutomaton
     *
     * Returns true if the method is successful for the given query, false otherwise
     */
    private boolean setUpFinalStatePAutomaton() {
        /*
            Canonical representation of VariableNode and PDS program location are same. So, use the location
            that maps to this node
         */
        VariableNode vn = new VariableNode(FunctionBasedHeapModel.getVariableKey(query.getMethod(),query.getVn()));
        initAutomaton = ptg2PDS.getInitPAutomaton();


        if (ptg2PDS.getNodeLocationBiMap().containsKey(vn)) {
            PAutomaton.State state = pds.getState(ptg2PDS.getNodeLocationBiMap().get(vn));
            PAutomaton.State finalState = initAutomaton.addState(new PAutomaton.State(state.toString() + "_f"));
            StackSymbol s = new StackSymbol(query.getMethod().toString());
            initAutomaton.addTransition(state, s, finalState);
            return true;
        } else {
            return false;
        }
    }



    @Override
    public void doAnalysis(Graph ptg) {
        //Setup the Context Phase
        setUpAnalysis(ptg, true, false);
        boolean finalStateSucceed = setUpFinalStatePAutomaton();

        if (finalStateSucceed) {

            Debug.debug("Setting up demand driven pointer analysis...");
            Debug.debug("Allow Graph Printing=" + String.valueOf(debug));
            Debug.debug(String.format("Graph Information:\n\t\tNodes: %d\n\t\tEdges:%d", ptg.getNumberOfNodes(), ptg.getNumberOfEdges()));


            PAutomaton preStar;

            setUpAnalysis(ptg, true, false);
            Debug.debug("Computing PreStar");
            preStar = pds.preStarAutomaton(initAutomaton);

            if (debug) {
                try {
                    Utils.writePAutomatonToFile("test_final_field", preStar);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            setUpAnalysis(ptg, false, true);
            preStar = pds.preStarAutomaton(initAutomaton);

            if (debug) {
                try {
                    Utils.writePAutomatonToFile("test_final_context", preStar);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Debug.error("Failed for the given query" + this.query);
        }
    }
}
