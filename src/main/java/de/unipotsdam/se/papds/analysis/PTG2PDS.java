package de.unipotsdam.se.papds.analysis;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.ibm.wala.sourcepos.Debug;
import de.unipotsdam.se.papds.analysis.demand.Query;
import de.unipotsdam.se.papds.graphlib.*;
import de.unipotsdam.se.papds.pds.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Builds a PDS from a given Points-To graph
 */
public class PTG2PDS {


    /*
        Used for a full pointer analysis. This structure keeps the set of <o,m> where o is an object allocation site
     */
    public Set<PConfig> newAllocSiteConfig = new HashSet<>();


    /*
        Map Graph Nodes to Location which is later queried after analysis to determine Points-To Set and Alias Set
        Node -> Location
    */
    BiMap<Node, Location> nodeLocationBiMap;

    private PDS pds;

    private Graph ptg;

    private PAutomaton initAutomaton;

    private boolean fullAnalysisMode;

    /*
        Incase of a demand driven analysis, we update the
     */
    private Query query;


    /**
     * Constructor called in case of Full Analysis Mode
     * Why Java don't have default variables!!!!
     * @param pds
     * @param ptg
     */
    public PTG2PDS(PDS pds, Graph ptg) {
        this.pds = pds;
        this.ptg = ptg;
        initAutomaton = new PAutomaton();
        nodeLocationBiMap = HashBiMap.create();
        this.fullAnalysisMode = true;
    }

    public PTG2PDS(PDS pds, Graph ptg, Query query) {
        this.pds = pds;
        this.ptg = ptg;
        initAutomaton = new PAutomaton();
        nodeLocationBiMap = HashBiMap.create();
        this.fullAnalysisMode = false;
        this.query = query;
    }



    /**
     * This function buildPDS from Points-To graph and also build  PAutomaton for a Post* analysis. A Post* analysis gives us a full-Pointer analysis
     */
    public void buildPDS(boolean fieldMode, boolean callSiteMode) {
        Iterator<Edge> edges = ptg.getEdgesAndLabels();

        while (edges.hasNext()) {
            Edge e = edges.next();
            if (e.getLabel().label == lbl.NEW) {
                ObjectNode u = (ObjectNode) e.getU();
                VariableNode v = (VariableNode) e.getV();

                //Create a new location for each node in the graph
                Location ul = new Location(u.toString());
                Location vl = new Location(v.toString());
                pds.addLocation(ul);
                pds.addLocation(vl);
                //StackSymbol m1 = pds.addStackSymbols(u.getMethod().toString());
                StackSymbol m1 = pds.addStackSymbols(ul.toString());
                StackSymbol m2 = pds.addStackSymbols(v.getMethod().toString());

                //Map Node to Location
                nodeLocationBiMap.put(u, ul);
                nodeLocationBiMap.put(v, vl);


                //In this case, make a rule <o,m> ~~> <v,m>
                PConfig tmp1 = new PConfig(ul, PDS.makeStack(m1));
                PConfig tmp2 = new PConfig(vl, PDS.makeStack(m2));
                pds.makeAndAddRule(tmp1, tmp2);
                newAllocSiteConfig.add(tmp1);

            } else if (e.getLabel().label == lbl.ASSIGN) {
                //Assign is always between two variableKey
                VariableNode v1 = (VariableNode) e.getU();
                VariableNode v2 = (VariableNode) e.getV();

                Location v1l = new Location(v1.toString());
                Location v2l = new Location(v2.toString());
                pds.addLocation(v1l);
                pds.addLocation(v2l);
                StackSymbol m1 = pds.addStackSymbols(v1.getMethod().toString());
                StackSymbol m2 = pds.addStackSymbols(v2.getMethod().toString());

                nodeLocationBiMap.put(v1, v1l);
                nodeLocationBiMap.put(v2, v2l);

                //In this case, make a rule <v,m> ~~> <u,m>
                pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2)));
            } else if (e.getLabel().label == lbl.CALL) {
                VariableNode v1 = (VariableNode) e.getU();
                VariableNode v2 = (VariableNode) e.getV();

                Location v1l = new Location(v1.toString());
                Location v2l = new Location(v2.toString());
                pds.addLocation(v1l);
                pds.addLocation(v2l);
                StackSymbol m1 = pds.addStackSymbols(v1.getMethod().toString());
                StackSymbol m2 = pds.addStackSymbols(v2.getMethod().toString());

                nodeLocationBiMap.put(v1, v1l);
                nodeLocationBiMap.put(v2, v2l);

                if (callSiteMode) {
                    StackSymbol ctxt = pds.addStackSymbols(e.getLabel().context);
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2, ctxt)));
                } else {
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2)));
                }
            } else if (e.getLabel().label == lbl.PUTFIELD) {
                VariableNode v1 = (VariableNode) e.getU();
                VariableNode v2 = (VariableNode) e.getV();

                Location v1l = new Location(v1.toString());
                Location v2l = new Location(v2.toString());
                pds.addLocation(v1l);
                pds.addLocation(v2l);
                StackSymbol m1 = pds.addStackSymbols(v1.getMethod().toString());
                StackSymbol m2 = pds.addStackSymbols(v2.getMethod().toString());

                nodeLocationBiMap.put(v1, v1l);
                nodeLocationBiMap.put(v2, v2l);
                if (fieldMode) {
                    StackSymbol ctxt = pds.addStackSymbols(e.getLabel().context);
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2, ctxt)));
                } else { //Silent the field contexts
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2)));
                }
            } else if (e.getLabel().label == lbl.RETURN) {
                VariableNode v1 = (VariableNode) e.getU();
                VariableNode v2 = (VariableNode) e.getV();

                Location v1l = new Location(v1.toString());
                Location v2l = new Location(v2.toString());
                pds.addLocation(v1l);
                pds.addLocation(v2l);
                StackSymbol m1 = pds.addStackSymbols(v1.getMethod().toString());
                StackSymbol m2 = pds.addStackSymbols(v2.getMethod().toString());

                nodeLocationBiMap.put(v1, v1l);
                nodeLocationBiMap.put(v2, v2l);
                if (callSiteMode) {
                    StackSymbol ctxt = pds.addStackSymbols(e.getLabel().context);

                    //Add a dummy location
                    Location retTemp = new Location(v1.toString() + ctxt);
                    pds.addLocation(retTemp);

                    //Make the rules <p,m'> ~~> <p',\varepsilon>; <p',C> ~~-> <c,m>
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(retTemp, PDS.makeStack()));
                    pds.makeAndAddRule(new PConfig(retTemp, PDS.makeStack(ctxt)), new PConfig(v2l, PDS.makeStack(m2)));
                } else {
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2)));
                }
            } else if (e.getLabel().label == lbl.GETFIELD) {
                VariableNode v1 = (VariableNode) e.getU();
                VariableNode v2 = (VariableNode) e.getV();

                Location v1l = new Location(v1.toString());
                Location v2l = new Location(v2.toString());
                pds.addLocation(v1l);
                pds.addLocation(v2l);
                StackSymbol m1 = pds.addStackSymbols(v1.getMethod().toString());
                StackSymbol m2 = pds.addStackSymbols(v2.getMethod().toString());

                nodeLocationBiMap.put(v1, v1l);
                nodeLocationBiMap.put(v2, v2l);
                if (fieldMode) {
                    StackSymbol ctxt = pds.addStackSymbols(e.getLabel().context);

                    //Add a dummy location
                    Location retTemp = new Location(v1.toString() + ctxt);
                    pds.addLocation(retTemp);

                    //Make the rules <p,m'> ~~> <p',\varepsilon>; <p',C> ~~-> <c,m>
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(retTemp, PDS.makeStack()));
                    pds.makeAndAddRule(new PConfig(retTemp, PDS.makeStack(ctxt)), new PConfig(v2l, PDS.makeStack(m2)));
                } else {
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2)));
                }
            } else if (e.getLabel().label == lbl.STATICGETFIELD) {
                StaticFieldNode v1 = (StaticFieldNode) e.getU();
                VariableNode v2 = (VariableNode) e.getV();

                Location v1l = new Location(v1.toString());
                Location v2l = new Location(v2.toString());
                pds.addLocation(v1l);
                pds.addLocation(v2l);
                StackSymbol m1 = pds.addStackSymbols(v1.getClass().toString());
                StackSymbol m2 = pds.addStackSymbols(v2.getMethod().toString());

                nodeLocationBiMap.put(v1, v1l);
                nodeLocationBiMap.put(v2, v2l);
                if (fieldMode) {
                    StackSymbol ctxt = pds.addStackSymbols(e.getLabel().context);

                    //Add a dummy location
                    Location retTemp = new Location(v1.toString() + ctxt);
                    pds.addLocation(retTemp);

                    //Make the rules <p,m'> ~~> <p',\varepsilon>; <p',C> ~~-> <c,m>
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(retTemp, PDS.makeStack()));
                    pds.makeAndAddRule(new PConfig(retTemp, PDS.makeStack(ctxt)), new PConfig(v2l, PDS.makeStack(m2)));
                } else {
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2)));
                }

            } else if (e.getLabel().label == lbl.STATICPUTFIELD) {
                VariableNode v1 = (VariableNode) e.getU();
                StaticFieldNode v2 = (StaticFieldNode) e.getV();

                Location v1l = new Location(v1.toString());
                Location v2l = new Location(v2.toString());
                pds.addLocation(v1l);
                pds.addLocation(v2l);
                StackSymbol m1 = pds.addStackSymbols(v1.getMethod().toString());
                StackSymbol m2 = pds.addStackSymbols(v2.getClass().toString());

                nodeLocationBiMap.put(v1, v1l);
                nodeLocationBiMap.put(v2, v2l);
                if (fieldMode) {
                    StackSymbol ctxt = pds.addStackSymbols(e.getLabel().context);
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2, ctxt)));
                } else { //Silent the field contexts
                    pds.makeAndAddRule(new PConfig(v1l, PDS.makeStack(m1)), new PConfig(v2l, PDS.makeStack(m2)));
                }

            } else {
                Debug.debug("Wrong Label");
                System.exit(10);
            }

        }

        initializePAutomaton();
    }

    private void initializePAutomaton() {
        //For each location in the PDS, add a new location state
        for (Location loc : pds.getLocationSet()) {
            PAutomaton.State state = new PAutomaton.State(loc.toString());
            initAutomaton.addState(state);
            pds.initalizeLocationAndStates(loc, state);
            state.setInitial(true);
        }
    }


    public Set<PConfig> getNewAllocSiteConfig() {
        return newAllocSiteConfig;
    }

    public BiMap<Node, Location> getNodeLocationBiMap() {
        return nodeLocationBiMap;
    }

    public PAutomaton getInitPAutomaton() {
        return initAutomaton;
    }

    public String getPdsStatitics() {
        return String.format("PDS:\n" +
                "-------------------------------------------------------------------\n" +
                "Number of Program Locations: \t%d\n" +
                "Number of Stack Symbols:\t%d\n" +
                "Number of Push Rules:\t%d\n" +
                "Number of Pop Rules:\t%d\n" +
                "Number of Internal Rules:\t%d\n" +
                "Number of Total Rules:\t%d\n",
                pds.getLocationSetSize(),
                pds.getStackAlphabetSize(),
                pds.getNumberOfPushRules(),
                pds.getNumberOfPopRules(),
                pds.getNumberOfIntRules(),
                pds.getRuleSetSize()
        );
    }
}
