package de.unipotsdam.se.papds.analysis.full;

import com.ibm.wala.sourcepos.Debug;
import de.unipotsdam.se.papds.analysis.PTG2PDS;
import de.unipotsdam.se.papds.analysis.TwoPassAnalysis;
import de.unipotsdam.se.papds.graphlib.Graph;
import de.unipotsdam.se.papds.main.Main;
import de.unipotsdam.se.papds.pds.PAutomaton;
import de.unipotsdam.se.papds.pds.PConfig;
import de.unipotsdam.se.papds.pds.PDS;
import de.unipotsdam.se.utils.Utils;

import java.io.IOException;
import java.util.Set;

public class FullPointerAnalysis implements TwoPassAnalysis {

    private final boolean debug = Main.debug;
    PTG2PDS ptg2PDS;
    PAutomaton initAutomaton;
    private PDS pds = new PDS();

    @Override
    public void setUpAnalysis(Graph ptg, boolean fieldphase, boolean contextphase) {


        //Get the location for each element, state for each element and then add a final state

        long begin = System.currentTimeMillis();
        Debug.info("\tBuilding PDS ");
        ptg2PDS = new PTG2PDS(pds, ptg);

        initAutomaton = ptg2PDS.getInitPAutomaton();

        ptg2PDS.buildPDS(fieldphase, contextphase);

        Debug.info(ptg2PDS.getPdsStatitics());

        long end = System.currentTimeMillis();

        Debug.info("\tEnd Building PDS : Time Elapsed=" + (end - begin) / 1000.0 + "s");

        //Map all the new allocation sites to the set
        int i = 0;
        Set<PConfig> analysis = ptg2PDS.getNewAllocSiteConfig();
        //Get the first PConfig for the analysis
//        PConfig a = analysis.iterator().next();
//        PAutomaton.State s = pds.getLocationtoStateMap().get(a.getL());
//        if (s != null) {
//            initAutomaton.addTransition(s,a.getSt().elementAt(0),s);
//            s.setFinal(true);
//        }
        for (PConfig p : analysis) {
            //Add the P-Config and  its corresponding states
            PAutomaton.State s = pds.getLocationtoStateMap().get(p.getL());
            if (s != null) {
                PAutomaton.State finalSt = new PAutomaton.State("final_state_" + i);
                initAutomaton.addState(finalSt);
                initAutomaton.addTransition(s, p.getSt()[0], finalSt);
                s.setFinal(true);
                ++i;
            }
        }

        if (debug) {
            try {
                Utils.drawGraphImage(ptg, "pointsTo");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Debug.info(String.format("Initial PAutomaton Statistics\n\tStates:%d\n\tTransitions:%d", initAutomaton.getStates().size(), initAutomaton.getTransitions().size()));
    }

    @Override
    public void doAnalysis(Graph ptg) {

        //First build a field-sensitive points-to analysis
        Debug.info("Set up Full Pointer Analysis...");
        Debug.info("Graph Printing=" + String.valueOf(debug));
        Debug.info(String.format("Graph Information:\n\t\tNodes: %d\n\t\tEdges:%d", ptg.getNumberOfNodes(), ptg.getNumberOfEdges()));




        Debug.info("Starting Pointer Analysis");
        Debug.info("===FIELD SENSITIVE PHASE======");

        //Setup field sensitive phase
        setUpAnalysis(ptg, true, false);
        PAutomaton initAutomaton = ptg2PDS.getInitPAutomaton(); //Initial P-Automaton
        if (debug) {
            try {
                Utils.writePAutomatonToFile("test_initial_field", initAutomaton);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        PAutomaton pAutomaton = pds.postStar(initAutomaton);

        if (debug) {
            try {
                Utils.writePAutomatonToFile("test_final_field", pAutomaton);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        Debug.info(String.format("PAutomaton Statistics\n\tStates:%d\n\tTransitions:%d", pAutomaton.getStates().size(), pAutomaton.getTransitions().size()));
        Debug.info("Finished Field Sensitive Phase");

        if (debug) {
            try {
                Utils.drawPAutomaton(pAutomaton, "pAutomaton_Field");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Setup context sensitive phase
        Debug.info("===CONTEXT SENSITIVE PHASE==========");
        setUpAnalysis(ptg, false, true);
        initAutomaton = ptg2PDS.getInitPAutomaton();
        Debug.debug("End pointer analysis");
    }

}
