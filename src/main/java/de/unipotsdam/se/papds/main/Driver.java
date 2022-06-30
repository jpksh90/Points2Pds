package de.unipotsdam.se.papds.main;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.sourcepos.Debug;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.FifoQueue;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import de.unipotsdam.se.papds.analysis.PDSPointerAnalysis;
import de.unipotsdam.se.papds.analysis.coreStructures.Instance;
import de.unipotsdam.se.papds.analysis.demand.DemandDrivenAnalysis;
import de.unipotsdam.se.papds.analysis.demand.Query;
import de.unipotsdam.se.papds.analysis.demand.QueryBuilder;
import de.unipotsdam.se.papds.analysis.full.FullPointerAnalysis;
import de.unipotsdam.se.papds.graphlib.Graph;
import de.unipotsdam.se.papds.graphlib.Label;
import de.unipotsdam.se.papds.graphlib.VariableNode;
import de.unipotsdam.se.papds.graphlib.lbl;
import de.unipotsdam.se.papds.pdscallgraph.PDSCGNode;
import de.unipotsdam.se.papds.pdscallgraph.PDSCallGraphBuilder;
import de.unipotsdam.se.utils.OtherAnalysis;
import de.unipotsdam.se.utils.Utils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Driver {

    private static String mainClass;

    private static String exFile;

    private static String scopeFile;

    private static String classPath;

    private static Graph ptg = new Graph();

    private static AnalysisScope scope;

    private static IClassHierarchy cha = null;

    private static AnalysisCache cache;

    private static Iterable<Entrypoint> entryPoints;

    private static AnalysisOptions options;

    private static CallGraph callGraph;

    private static boolean fullAnalysisMode = false;

    private static Set<CGNode> entryNodes = new HashSet<>();

    private static PointerAnalysis<Instance> pointerAnalysis;


    public static void setUpFromScopefile(String mc, String ex, String sf, String reflectionOption) throws IOException {
        mainClass = mc;
        exFile = ex;
        scopeFile = sf;
        scope = AnalysisScopeReader.readJavaScope(scopeFile, new FileProvider().getFile(exFile), Main.class.getClassLoader());
        _setUp(reflectionOption);
    }

    public static void setUpFromClasspath(String mc, String ex, String cp, String reflectionOption) throws IOException {
        mainClass = mc;
        exFile = ex;
        classPath = cp;
        _setUp(reflectionOption);
    }

    private static void _setUp(String reflectionOption) {
        try {
            cha = ClassHierarchyFactory.make(scope);
            Debug.info("Number of Classes:" + cha.getNumberOfClasses());
        } catch (ClassHierarchyException e) {
            System.out.println(e);
        }
        entryPoints = Util.makeMainEntrypoints(scope, cha, mainClass);
        cache = new AnalysisCacheImpl();
        options = new AnalysisOptions();
        options.setEntrypoints(entryPoints);
        options.setAnalysisScope(scope);

        if (reflectionOption.equals("full")) {
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL); //Set for a NoReflection Option
        } else if (reflectionOption.equals("no-flow-casts")) {
            options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NO_FLOW_TO_CASTS);
        }
        pointerAnalysis = new PDSPointerAnalysis(cha);
    }


    /**
     * @throws IOException
     * @throws CallGraphBuilderCancelException
     */
    public static void buildPAG() throws IOException, CallGraphBuilderCancelException {
        //Driver.fullAnalysisMode = fullAnalysis;

        //WorkList approach for CGNodes Queue
        FifoQueue<CGNode> queue = new FifoQueue<>();
        ptg = new Graph();

        //CallGraphBuilder directory
        CallGraphBuilder<Instance> callgraphbuilder = new PDSCallGraphBuilder(cha, null);
        callGraph = callgraphbuilder.makeCallGraph(options, null);


        CallGraphBuilder zeroOneCallGraph = Util.makeVanillaZeroOneCFABuilder(options, cache, cha, scope);
        CallGraph contextInsensitive = zeroOneCallGraph.makeCallGraph(options, null);
        Debug.info(getCallGraphStats(contextInsensitive, "0-1ContextSensitive"));


        //Convert an iterable to string
        Collection<CGNode> entrypoints = contextInsensitive.getEntrypointNodes();
        entryNodes.addAll(entrypoints);


        Debug.debug("New Entrypoints " + entryPoints.toString());

//        for (Entrypoint e : entryPoints) {
//            CGNode node = new PDSCGNode(e.getMethod());
//            queue.push(node);
//            callGraph.addNode(node);
//        }

        entrypoints.forEach(q -> queue.push(q));

        Debug.debug("Initial EntryPoints " + queue);

        long begin = System.currentTimeMillis();

        Debug.info("Building Points-To-Graph ......");

        Set<CGNode> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            //Get the node from the call graph builder
            CGNode n = queue.pop();
            callGraph.addNode(n);
            PDSCGNode pdsNewNode = new PDSCGNode(n.getMethod());

            Debug.debug("Removed " + n + " from queue");
            if (!visited.contains(n)) {
                visited.add(n);

//                System.out.println(n.getIR());
                //Pass to SSA visitor
                SSAVisitorImpl irVisitor = new SSAVisitorImpl(ptg, n.getMethod(), n.getIR(), cha, pdsNewNode, contextInsensitive, n);
                irVisitor.processInstructions();
                Iterator<CallSiteReference> callsites = n.iterateCallSites();
                //If the target method is resolved, then add the target method to callgraph

                while (callsites.hasNext()) {
                    //Add the Call-Site functions to the queue
                    CallSiteReference call = callsites.next();
                    Set<CGNode> resolvedTargets = contextInsensitive.getPossibleTargets(n, call);

                    for (CGNode resolvedTarget : resolvedTargets) {
                        CGNode target = new PDSCGNode(resolvedTarget.getMethod());

                        Debug.debug("IR for method:" + target.getMethod().toString());


                        //If the method is unique, then add that method to call graph, otherwise request
                        callGraph.addNode(target);
                        callGraph.addEdge(n, target);
                        Debug.debug("Added Target Node " + target.getMethod().toString());
                        assert target != null;
                        Debug.debug("Pushed node " + target.getMethod().getSignature() + " to queue");
                        queue.push(resolvedTarget);
                        addReturnNode(pdsNewNode, (PDSCGNode) target, call.getProgramCounter());
                    }
                }
            }
        }

        long end = System.currentTimeMillis();
        Debug.info("End Buidling Points-To-Graph : Time " + (end - begin) / 1000.0 + "ms");

        //Call Construct PDS
        if (Main.debug) {
            Utils.printGraph(ptg);
        }
        Debug.info(getCallGraphStats(callGraph, "PDS-CallGraph"));
    }

    public static void addReturnNode(PDSCGNode caller, PDSCGNode callee, int lineno) {
        ptg.addEdge(new VariableNode(callee.getReturnKey()),
                new VariableNode(caller.getReturnKey()),
                new Label(lbl.RETURN, caller.getReturnKey().getM().getName().toString() + String.valueOf(lineno)));
    }

    public static void fullAnalysis() {
        FullPointerAnalysis pointerAnalysis = new FullPointerAnalysis();
        pointerAnalysis.doAnalysis(ptg);
    }


    public static void demandDrivenAnalysis(Query q) {
        DemandDrivenAnalysis analysis = new DemandDrivenAnalysis();
        analysis.addQuery(q);
        analysis.doAnalysis(ptg);
    }

    public static QueryBuilder queryBuilderInitializer() {
        return new QueryBuilder(entryNodes);
    }

    public static void printIR(CGNode n) {
        SSAInstruction[] instructions = n.getIR().getInstructions();

        for (int i = 0; i < instructions.length; i++) {
            if (instructions[i] != null)
                Debug.debug("\t" + instructions[i].toString());
        }
    }

    public static String getCallGraphStats(CallGraph cg, String msg) {
        return String.format("CALLGRAPH STATISTICS:%s\n\tNumber of Nodes=%d\n\tNumber of Edges=", msg, cg.getNumberOfNodes());
    }


    public static void doAndersenAnalysis() throws CancelException {
        OtherAnalysis.zeroOneContextSensitive(options, cache, cha, scope);
    }

    public static PointerAnalysis<Instance> getPointerAnalysis() {
        return pointerAnalysis;
    }
}
