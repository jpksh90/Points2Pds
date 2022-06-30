package de.unipotsdam.se.utils;

import com.google.common.collect.Lists;
import com.ibm.wala.sourcepos.Debug;
import de.unipotsdam.se.papds.graphlib.Edge;
import de.unipotsdam.se.papds.graphlib.Graph;
import de.unipotsdam.se.papds.graphlib.Node;
import de.unipotsdam.se.papds.pds.PAutomaton;
import de.unipotsdam.se.papds.pds.StackSymbol;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    public static void printGraph(Graph g) {
        g.getEdgesAndLabels().forEachRemaining(q -> Debug.debug(q.toString()));
    }


    public static <T> String iteratorToString(Iterator<T> input) {
        if (input != null) {
            List<T> list = Lists.newArrayList(input);
            String s = "";
            s = list.stream().map(Object::toString).collect(Collectors.joining(","));
            return s;
        } else {
            return "";
        }
    }


    public static void drawGraphImage(Graph graph, String outputFileName) throws IOException {

        FileWriter writer = new FileWriter(new File(outputFileName + ".dot"));
        writer.write("digraph {\n");
        for (Node n : graph.getNodes()) {
            writer.write(n.hashCode() + "[label=\"" + n.toString() + "\"];\n");
        }

        Iterator<Edge> edges = graph.getEdgesAndLabels();
        while (edges.hasNext()) {
            Edge e = edges.next();
            String label = e.getLabel().label.toString() + (e.getLabel().context == "" ? "" : e.getLabel().context);
            writer.write(e.getU().hashCode() + "->" + e.getV().hashCode() + "[label=\"" + label + "\"];\n");
        }
        writer.write("}");
        writer.close();
        Process p = Runtime.getRuntime().exec(String.format("%s -Tpdf -Gsize=9,15\\! %s -o %s", "dot", outputFileName + ".dot", outputFileName + ".pdf"));
        p.destroy();
    }


    public static void drawPAutomaton(PAutomaton pa, String outputFileName) throws IOException {
        FileWriter writer = new FileWriter(new File(outputFileName + ".dot"));
        writer.write("digraph {\n");


        for (PAutomaton.State state : pa.getStates()) {
            if (!state.isFinal()) {
                writer.write(state.hashCode() + "[shape=oval,width=.5,fixedsize=false,label=\"" + state.getState() + "\"];\n");
            } else {
                writer.write(state.hashCode() + "[peripheries=2,shape=oval,width=.5,fixedsize=false,label=\"" + state.getState() + "\"];\n");
            }
        }

        int r, g, b;
        r = g = b = 0;

        int c = 1;
        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t : pa.getTransitions()) {
            String color = String.format("#%02X", r) + String.format("%02X", g) + String.format("%02X", b);
            writer.write(t.getX().hashCode() + "->" + t.getZ().hashCode() + "[constraint=true, color=\"" + color + "\",label=\"" + t.getY().toString() + "\"];\n");
            if (c % 3 == 0) {
                r = (r + 50) % 250;
            }
            if (c % 2 == 0) {
                g = (g + 50) % 250;
            }

            b = (b + 50) % 250;
            ++c;
        }
        writer.write("}");
        writer.close();
        Process p = Runtime.getRuntime().exec(String.format("%s -Tpdf  %s -o %s", "dot", outputFileName + ".dot", outputFileName + ".pdf"));
        p.destroy();

    }

    public static void writePAutomatonToFile(String fileName, PAutomaton pAutomaton) throws IOException {
        File file = new File(fileName + ".pautomaton");
        file.createNewFile();
        FileWriter writer = new FileWriter(file);

        writer.write("States\n");
        for (PAutomaton.State s : pAutomaton.getStates()) {
            writer.write(s.toString() + "\n");
        }
        writer.write("Transitions:\n");
        for (Triple<PAutomaton.State, StackSymbol, PAutomaton.State> t : pAutomaton.getTransitions()) {
            writer.write(t.toString() + "\n");
        }
        writer.close();
    }
}
