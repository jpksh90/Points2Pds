/*
 * Copyright (c) 2017.  Jyoti Prakash
 * Test File for PDS Testing
 *
 * Example taken from https://drona.csa.iisc.ernet.in/~deepakd/atc-2011/pushdown-reachability.pdf (last accessed on 11.12.2017 12:45 HRS)
 */

package tests;

import com.ibm.wala.sourcepos.Debug;
import de.unipotsdam.se.papds.pds.*;
import de.unipotsdam.se.utils.Triple;

import java.util.Set;

public class TestPDS {

    public static boolean DEBUG = true;

    public static void main(String args[]) {
        //Debug.setLogFile("debugfile");

        if (DEBUG) {
            Debug.setMinLogLevel(Debug.LogLevel.DEBUG);
        } else {
            Debug.setMinLogLevel(Debug.LogLevel.INFO);
        }

        PDS pds = new PDS();

        Location p0 = pds.addLocation(new Location("p0"));
        Location p1 = pds.addLocation(new Location("p1"));
        Location p2 = pds.addLocation(new Location("p2"));


        StackSymbol a = pds.addStackSymbols("a");
        StackSymbol b = pds.addStackSymbols("b");
        StackSymbol c = pds.addStackSymbols("c");


        //Add rules to PDS
        /*
            (p0,a) ~~> (p1,b;a)
            (p1,b) ~~> (p2,c;a)
            (p2,c) ~~> (p0,b)
            (p0,b) ~~> (p0,ε)
         */
        pds.makeAndAddRule(new PConfig(p0, PDS.makeStack(a)), new PConfig(p1, PDS.makeStack(b, a)));
        pds.makeAndAddRule(new PConfig(p1, PDS.makeStack(b)), new PConfig(p2, PDS.makeStack(c, a)));
        pds.makeAndAddRule(new PConfig(p2, PDS.makeStack(c)), new PConfig(p0, PDS.makeStack(b)));
        pds.makeAndAddRule(new PConfig(p0, PDS.makeStack(b)), new PConfig(p0, PDS.makeStack()));

        PAutomaton initAutomatonPrestar = new PAutomaton();
        PAutomaton.State s_p0 = initAutomatonPrestar.addState(new PAutomaton.State(p0.toString()));
        PAutomaton.State s_p1 = initAutomatonPrestar.addState(new PAutomaton.State(p1.toString()));
        PAutomaton.State s_p2 = initAutomatonPrestar.addState(new PAutomaton.State(p2.toString()));

        PAutomaton.State s_q0 = initAutomatonPrestar.addState(new PAutomaton.State("s_q0"));
        PAutomaton.State s_q1 = initAutomatonPrestar.addState(new PAutomaton.State("s_q1"));
        s_q1.setFinal(true);

        //Add initial transition (s_p0) -a-> (s_q0) -a-> ((s_q1)) , where s_q1 is final state
        initAutomatonPrestar.addState(new PAutomaton.State(p1.toString()));
        initAutomatonPrestar.addState(new PAutomaton.State(p2.toString()));
        initAutomatonPrestar.addTransition(s_p0, a, s_q0);
        initAutomatonPrestar.addTransition(s_q0, a, s_q1);

        pds.initalizeLocationAndStates(p0, s_p0);
        pds.initalizeLocationAndStates(p1, s_p1);
        pds.initalizeLocationAndStates(p2, s_p2);
        pds.initalizeLocationAndStates(new Location("dummy1"), s_q0);
        pds.initalizeLocationAndStates(new Location("dummy2"), s_q1);
        Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> rel = pds.prestar(initAutomatonPrestar);
        //Debug.info("============ Resultant Transitions =============");
        //rel.forEach(q -> Debug.info(q.toString()));


        Debug.info("PDS= " + pds.toString());

        PAutomaton postStar = pds.postStar(initAutomatonPrestar);
        Debug.info("PostStar automaton" + postStar.toString());
    }
}
