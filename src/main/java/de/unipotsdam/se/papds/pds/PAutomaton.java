package de.unipotsdam.se.papds.pds;

import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;
import de.unipotsdam.se.utils.Triple;

import java.util.*;
import java.util.stream.Collectors;

public class PAutomaton {

    public final boolean DEBUG = true;
    Set<Triple<State, StackSymbol, State>> transitions = HashSetFactory.make();
    private HashSet<State> states;


    public PAutomaton() {
        states = new HashSet<>();
    }

    public State addState(State t) {
        if (!states.contains(t)) {
            states.add(t);
        }
        return t;
    }

    public Triple<State, StackSymbol, State> addTransition(State x, StackSymbol w, State y) {
        Triple<State, StackSymbol, State> trans = new Triple<State, StackSymbol, State>(x, w, y);
        if (!states.contains(x)) {
            throw new IllegalArgumentException("State not found:" + x);
        } else if (!states.contains(y)) {
            throw new IllegalArgumentException("State not found:" + y);
        }
        transitions.add(trans);
        return trans;
    }

    public Optional<List<State>> next(State state, StackSymbol alphabet) {
        List<State> list;
        list = transitions.stream()
                .filter(t -> state.equals(t.getX()) && alphabet.equals(t.getY()))
                .map(t -> t.getZ()).collect(Collectors.toList());
        if (list == null)
            return Optional.empty();
        else
            return Optional.of(list);
    }

    public Vector<Triple<State, StackSymbol, State>> getTransitions() {
        Vector<Triple<State, StackSymbol, State>> tmp = new Vector<>();
        tmp.addAll(transitions);
        return tmp;
    }

    public HashSet<State> getStates() {
        return this.states;
    }

    public void addAll(Set<State> s) {
        states.addAll(s);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("<PAutomaton>").append("States = " + states + "\n").append("Transitions\n");
        transitions.forEach(q -> str.append(q).append('\n'));
        str.append("</PAutomaton>");
        return str.toString();
    }


    public List<Pair<State, StackSymbol>> getOutgoingTransitions(State s) {
        Vector<Pair<State, StackSymbol>> res = new Vector<>();
        return transitions.stream()
                .filter(q -> q.getX().equals(s))
                .map(q -> Pair.make(q.getZ(), q.getY()))
                .collect(Collectors.toList());
    }


    public Set<PAutomaton.State> getNonFinalStates() {
        Set<State> res = new HashSet<>();
        res = states.stream()
                .filter(state -> !state.isFinal())
                .collect(Collectors.toSet());
        return res;
    }

    public Set<PAutomaton.State> getFinalStates() {
        Set<State> res = new HashSet<>();
        res = states.stream()
                .filter(state -> !state.isFinal())
                .collect(Collectors.toSet());
        return res;
    }

    public List<Pair<PAutomaton.State, StackSymbol>> getIncomingTransitions(State s) {
        return transitions.stream()
                .filter(q -> q.getZ().equals(s))
                .map(q -> Pair.make(q.getX(), q.getY())).collect(Collectors.toList());
    }


    public HashMap<State, List<List<Pair<State, StackSymbol>>>> getAllPathsToFinalStates() {
        HashMap<State, List<List<Pair<State, StackSymbol>>>> result = HashMapFactory.make();
        Set<State> initStates = getNonFinalStates();

        //Initialize the list for all nodes
        for (State s : initStates) {
            result.put(s, new ArrayList<>());
        }

        Queue<State> worklist = new LinkedList<>();

        initStates.forEach(worklist::add);

        HashSet<State> visted = new HashSet<>();

        //Initialize the hashMap


        while (!worklist.isEmpty()) {
            State s = worklist.remove();
            if (!s.isFinal()) {
                if (!visted.contains(s)) {
                    visted.add(s);
                    List<List<Pair<State, StackSymbol>>> list = result.get(s);
                    if (list != null) {
                        //Get the incoming transitions and then add it to the list
                        List<Pair<PAutomaton.State, StackSymbol>> inTransitions = getIncomingTransitions(s);
                        for (Pair<PAutomaton.State, StackSymbol> p : inTransitions) {
                            list.stream().map(q -> q.add(p));
                        }
                        if (s.isInitial()) {
                            List l = new ArrayList();
                            l.add(Pair.make(s, PDS.epsilon));
                            list.add(l);
                        }
                        //Add the dependents for worklist if doesn't exists
                        List<Pair<State, StackSymbol>> outgoing = getOutgoingTransitions(s).stream().filter(q -> !worklist.contains(q)).collect(Collectors.toList());
                        worklist.addAll(outgoing.stream().map(q -> q.fst).collect(Collectors.toList()));
                    }
                }
            }
        }

        return result;
    }

    public static class State {
        private String state;
        private boolean isInitial;
        private boolean isFinal;

        public State(String state) {
            this.state = state;
        }

        public String getState() {
            return state;
        }

        public boolean isInitial() {
            return isInitial;
        }

        public void setInitial(boolean initial) {
            isInitial = initial;
        }

        public boolean isFinal() {
            return isFinal;
        }

        public void setFinal(boolean aFinal) {
            isFinal = aFinal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            State state1 = (State) o;

            return state != null ? state.equals(state1.state) : state1.state == null;
        }

        @Override
        public int hashCode() {
            return state != null ? state.hashCode() : 0;
        }

        @Override
        public String toString() {
            return state;
        }
    }


}