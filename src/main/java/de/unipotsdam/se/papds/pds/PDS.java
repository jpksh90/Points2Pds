
/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 */

package de.unipotsdam.se.papds.pds;

import com.ibm.wala.sourcepos.Debug;
import de.unipotsdam.se.utils.Triple;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

public class PDS {

    public static StackSymbol epsilon = new StackSymbol("\u03B5");
    PAutomaton pa;
    //Map Locations to States
    private HashMap<Location, PAutomaton.State> locationtoStateMap = new HashMap<Location, PAutomaton.State>();

    //Map the state to locations
    //TODO: Instead use a  BiMap
    private HashMap<PAutomaton.State, Location> statetoLocationMap = new HashMap<>();
    private Set<Rule> ruleSet = new HashSet<>(); // $\Delta$
    private Set<Location> locationSet = new HashSet<>(); // $P$
    private Set<StackSymbol> stackSymbols = new HashSet<>(); // $\Gamma$


    public PDS() {
    }

    //make an empty stack, with no elements
    public static StackSymbol[] makeStack() {
        return new StackSymbol[0];
    }

    //make a stack with 1 element
    public static StackSymbol[] makeStack(StackSymbol s) {
        StackSymbol[] tmp = {s};
        return tmp;
    }

    //make a stack with 2 elements
    public static StackSymbol[] makeStack(StackSymbol s, StackSymbol s1) {
        StackSymbol[] tmp = {s, s1};
        return tmp;
    }


    public Rule makeAndAddRule(PConfig p, PConfig l) {
        Rule rule = new Rule(p, l);
        ruleSet.add(rule);
        return rule;
    }

    public Location addLocation(Location l) {
        locationSet.add(l);
        return l;
    }

    public StackSymbol addStackSymbols(String s) {
        StackSymbol st = new StackSymbol(s);
        stackSymbols.add(st);
        return st;
    }

    private Vector<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> checkInTriple(HashSet<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> vec, PAutomaton.State q) {
        Vector<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> res = new Vector<>();
        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t : vec) {
            if (t.getX().equals(q)) {
                vec.add(t);
            }
        }
        return res;
    }

    public void initalizeLocationAndStates(Location l, PAutomaton.State s) {
        locationtoStateMap.put(l, s);
        statetoLocationMap.put(s, l);
    }

    //TODO: Implement optimizations mentioned in algorithm

    /**
     * @return For a given p automaton, computes the backward reachable states. States are backward reachable if it for some word it reaches final state
     */
    public Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> prestar(PAutomaton pa) {

        Debug.debug("Start PreStar");
        long startTime = System.currentTimeMillis();

        PAutomaton pa_prestar = new PAutomaton();
        Queue<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> trans = new LinkedBlockingQueue<>();
        trans.addAll(pa.getTransitions());
        Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> rel = new HashSet<>();
        Set<Rule> deltaRuleSet = new HashSet<>();

        //Union two sets of Rules
        final BinaryOperator<Set<Rule>> rulesUnion = (q, v) -> {
            Set<Rule> tmp = new HashSet<>();
            tmp.addAll(q);
            tmp.addAll(v);
            return tmp;
        };
        //Select the rules for a Program Locations
        final BiFunction<Set<Rule>, Location, Optional<Set<Rule>>> rulesForLoc = (r, l) -> Optional.of(r.parallelStream().filter(q -> q.getRhs().getL().equals(l)).collect(Collectors.toSet()));
        //Get rules for a specific length
        final BiFunction<Set<Rule>, Integer, Optional<Set<Rule>>> rulesForLengthFilter = (r, l) -> Optional.of(r.parallelStream().filter(q -> q.getRhs().getSt().length == l).collect(Collectors.toSet()));


        // Add rules of the form <p,y1> ~~> <q,e>
        Optional<Set<Rule>> rules = rulesForLengthFilter.apply(ruleSet, 0);
        for (Rule rule : rules.get()) {
            Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t = new Triple<>(locationtoStateMap.get(rule.getLhs().getL()), rule.getLhs().getSt()[0], locationtoStateMap.get(rule.getRhs().getL()));
            trans.add(t);
        }


        while (!trans.isEmpty()) {
            Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t = trans.remove(); // (q, a, q')
            Debug.debug("Removed Transition " + t);
            if (!rel.contains(t)) {
                rel.add(t);

//                Get the rules of form <p,y> ~~> <q,a>
                Set<Rule> rules_union = rulesUnion.apply(ruleSet, deltaRuleSet);
                //Remove the rules that do not follow the pattern <p,w>
                rules_union = rules_union.parallelStream().filter(rule -> ((rule.getRightLoc().equals(statetoLocationMap.get(t.getX()))) && (rule.getRightStack().length == 1) && (rule.getRightStack()[0].equals(t.getY())))).collect(Collectors.toSet());
                Debug.debug("Rules here" + t + " " + rules_union + " " + rules_union.size());
                Debug.debug("Rule Length");

                for (Rule rule : rules_union) {
                    //Get the states for rules
                    PAutomaton.State p = locationtoStateMap.get(rule.getLhs().getL());
                    PAutomaton.State q = t.getZ();
                    Triple<PAutomaton.State, StackSymbol, PAutomaton.State> newTransition = new Triple<>(p, rule.getLhs().getSt()[0], q);
                    trans.add(newTransition);
                    Debug.debug("Line 145: Transition0 " + t + " Rule " + rule + " Transition " + newTransition);
                }


                //Get the rules of the form <p,y> ~~> <q, zw>
                //Split the rules into form (p,y) ~~> (q',w)
                rules = rulesForLengthFilter.apply(rulesForLoc.apply(ruleSet, statetoLocationMap.get(t.getX())).get(), 2);
                rules.ifPresent(q -> q.removeIf(r -> (!r.getRightStack()[0].equals(t.getY()))));
                for (Rule rule : rules.get()) {
                    //Create a new rule for the column
                    Location q1 = statetoLocationMap.get(t.getZ());
                    Rule r = new Rule(new PConfig(rule.getLhs().getL(), makeStack(rule.getRightStack()[0])), new PConfig(q1, makeStack(rule.getRightStack()[1])));
                    deltaRuleSet.add(r);
                    Debug.debug("Added" + r);

                    //Get the transitions for (q1, \gamma, q11)
                    final BiFunction<Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>>, PAutomaton.State, Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>>> transitionsFrom = (l, q) -> l.parallelStream().filter(l1 -> l1.getX().equals(q)).collect(Collectors.toSet());

                    Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> deltaHelp = transitionsFrom.apply(rel, t.getZ());
                    for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> d : deltaHelp) {
                        if (d.getY().equals(rule.getRhs().getSt()[1])) {
                            Triple<PAutomaton.State, StackSymbol, PAutomaton.State> newTransition = new Triple<>(locationtoStateMap.get(rule.getLhs().getL()), rule.getLhs().getSt()[0], d.getZ());
                            trans.add(newTransition);
                            Debug.debug("Line 167: TransitionO " + t + " Rule " + rule + " Transition " + newTransition);
                        }
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        Debug.debug("End PreStar");
        Debug.info("PreStar Time " + (endTime - startTime) + "ms");
        return rel;
    }


    public PAutomaton preStarAutomaton(PAutomaton pa) {
        Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> rel = prestar(pa);
        PAutomaton newPA = new PAutomaton();

        //Get all the states
        Set<PAutomaton.State> states;
        rel.forEach(transition -> {
            newPA.addState(transition.getX());
            newPA.addState(transition.getZ());
        });

        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> r : rel) {
            pa.addTransition(r.getX(),r.getY(),r.getZ());
        }
        return  newPA;
    }



    /**
     * For an automaton
     *
     * @return An automaton, consisting of forward reachable states
     */
    public PAutomaton postStar(PAutomaton pa) {
        Debug.debug("Initial PAutomaton");
        Debug.debug(pa.toString());
        Debug.debug("Start PostStar");
        long startTime = System.currentTimeMillis();


        //Add all states from initial automaton to a new automaton
        PAutomaton resultantAutomaton = new PAutomaton();
        resultantAutomaton.addAll(pa.getStates());


        Queue<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> trans = new LinkedBlockingQueue<>();
        Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> rel = new HashSet<>();
        HashMap<String, PAutomaton.State> stringToStateMap = new HashMap<>(); //Map the states that are not a part of locations  to a string

        //Add the transitions that are part of location set
        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t : pa.getTransitions()) {
            if (locationSet.contains(statetoLocationMap.get(t.getX()))) {
                trans.add(t);
            }
        }

        //Add the transitions from pa.transitions()\trans
        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t : pa.getTransitions()) {
            if (!trans.contains(t)) {
                rel.add(t);
            }
        }
//        pa.getTransitions().forEach(t -> {
//            if (!trans.contains(t)) {
//                rel.add(t);
//            }
//        });

        Debug.debug("Rel " + rel + " length" + rel.size());
        Debug.debug("Initial Transitions " + trans + "\n Trans size " + trans.size());

        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t : rel) {
            resultantAutomaton.addTransition(t.getX(), t.getY(), t.getZ());
        }

        //Add rules for stack size = 2
        Set<Rule> rules2 = ruleSet.parallelStream().filter(q -> q.getRightStack().length == 2).collect(Collectors.toSet());
        for (Rule rule : rules2) {
            //Add the states
            String name = rule.getRightLoc().toString() + rule.getRightStack()[0].toString();
            PAutomaton.State st = new PAutomaton.State("state_" + name);
            resultantAutomaton.addState(st);
            stringToStateMap.put(name, st);
        }


        while (!trans.isEmpty()) {
            Debug.debug(" Transitions " + trans);
            Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t = trans.remove(); //(p,y,q)
            Debug.debug("Removed Transition " + t);
            if (!rel.contains(t)) {
                rel.add(t);
                if (!t.getY().equals(epsilon)) {
                    //Get the rule of form (p,y) ~~-> (p',\epsilon)
                    Set<Rule> rules = ruleSet.parallelStream().filter(rule ->
                            (rule.getLeftStack()[0].equals(t.getY())) &&
                                    (rule.getRightStack().length == 0) &&
                                    (rule.getLeftLoc().equals(statetoLocationMap.get(t.getX()))))
                            .collect(Collectors.toSet());
                    Debug.debug("Rules set" + rules.size());
                    for (Rule r : rules) {
                        Triple<PAutomaton.State, StackSymbol, PAutomaton.State> newtrans = new Triple<>(locationtoStateMap.get(r.getRightLoc()), epsilon, t.getZ());
                        trans.add(newtrans);
                        Debug.debug("Added new Transition (275)" + newtrans);
                    }

                    //Get the rules of form (p,y) ~~-> (p',y1)
                    rules = ruleSet.parallelStream().filter(rule -> (rule.getLeftStack()[0].equals(t.getY())) && (rule.getRightStack().length == 1) && (rule.getLeftLoc().equals(statetoLocationMap.get(t.getX())))).collect(Collectors.toSet());
                    for (Rule r : rules) {
                        Triple<PAutomaton.State, StackSymbol, PAutomaton.State> newtrans = new Triple<>(locationtoStateMap.get(r.getRightLoc()), r.getRightStack()[0], t.getZ());
                        trans.add(newtrans);
                        Debug.debug("Added new transition (283)" + newtrans);
                    }

                    //Get the rules of form (p,y) ~~-> (p', y1;y2)
                    rules = ruleSet.parallelStream().filter(rule -> (rule.getLeftStack()[0].equals(t.getY())) && (rule.getRightStack().length == 2) && (rule.getLeftLoc().equals(statetoLocationMap.get(t.getX())))).collect(Collectors.toSet());
                    for (Rule r : rules) {
                        String name = r.getRightLoc().toString() + r.getRightStack()[0].toString();
                        Triple<PAutomaton.State, StackSymbol, PAutomaton.State> newtrans = new Triple<>(locationtoStateMap.get(r.getRightLoc()), r.getRightStack()[0], stringToStateMap.get(name));
                        trans.add(newtrans);
                        Debug.debug("Added new transition (292)" + newtrans);
                        resultantAutomaton.addTransition(stringToStateMap.get(name), r.getRightStack()[1], t.getZ());
                        rel.add(new Triple<>(stringToStateMap.get(name), r.getRightStack()[1], t.getZ()));
//                        result.getTransitions(stringToStateMap.get(r.getRightStack().toString())).forEach(transition -> rel.add(transition));

                        //Get all the transitions of form (p'', epsilon, q_stack)
                        Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> auxTransitions = rel.parallelStream().filter(q -> q.getY().equals(epsilon) && q.getZ().equals(stringToStateMap.get(name))).collect(Collectors.toSet());
                        Debug.debug("Aux Transitions  " + auxTransitions);
                        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> p : auxTransitions) {
                            Triple<PAutomaton.State, StackSymbol, PAutomaton.State> newTrans = new Triple<>(p.getX(), r.getRightStack()[1], t.getZ());
                            trans.add(newTrans);
                            Debug.debug("Added new transition (303)" + newTrans);
                        }
                    }
                } else {
                    //Get transitions of the form (q,y',q') \in rel and add it to transition
                    //rel.stream().filter(q -> q.getX().equals(t.getZ())).forEach(l -> trans.add(l));
                    Set<Triple<PAutomaton.State, StackSymbol, PAutomaton.State>> rel_temp = rel.stream().filter(q -> q.getX().equals(t.getZ())).collect(Collectors.toSet());
                    if (rel_temp.size() > 0) {
                        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> r : rel_temp) {
                            Triple<PAutomaton.State, StackSymbol, PAutomaton.State> newTrans = new Triple<>(t.getX(), r.getY(), r.getZ());
                            trans.add(newTrans);
                            Debug.debug("Added new transition (314)" + newTrans);
                        }
                    }
                }
            }
        }

        Debug.debug(" Rel = " + rel);
        rel.forEach(q -> resultantAutomaton.addTransition(q.getX(), q.getY(), q.getZ()));
        long endTime = System.currentTimeMillis();
        Debug.info("PostStar Time:" + (endTime - startTime) + "ms");
        Debug.debug("End PostStar");
        Debug.debug(resultantAutomaton.toString());
        return resultantAutomaton;
    }


    public PAutomaton.State getState(Location l) {
        return locationtoStateMap.get(l);
    }

    public Set<Rule> getRules() {
        return this.ruleSet;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Rules\n").append("Ruleset Size " + ruleSet.size() + " Number of Program Locations " + locationSet.size());
        for (Rule r : ruleSet) {
            sb.append(r.toString());
            sb.append("\n");
        }
        return sb.toString();
    }


    public HashMap<Location, PAutomaton.State> getLocationtoStateMap() {
        return locationtoStateMap;
    }

    public Set<Location> getLocationSet() {
        return locationSet;
    }

    public int getLocationSetSize() {
        return locationSet.size();
    }

    public int getRuleSetSize() {
        return ruleSet.size();
    }

    public int getStackAlphabetSize() {
        return stackSymbols.size();
    }

    public long getNumberOfPushRules() {
        return ruleSet.stream().filter(rule -> (rule.getRightStack().length == 2)).count();
    }

    public long getNumberOfPopRules() {
        return ruleSet.stream().filter(rule -> (rule.getRightStack().length == 0)).count();
    }

    public long getNumberOfIntRules() {
        return ruleSet.stream().filter(rule -> rule.getRightStack().length == 1).count();
    }
}
