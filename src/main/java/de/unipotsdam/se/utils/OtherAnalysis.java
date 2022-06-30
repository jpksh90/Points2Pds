/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 */

package de.unipotsdam.se.utils;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.demandpa.alg.DemandRefinementPointsTo;
import com.ibm.wala.demandpa.alg.statemachine.DummyStateMachine;
import com.ibm.wala.demandpa.flowgraph.IFlowLabel;
import com.ibm.wala.demandpa.flowgraph.SimpleDemandPointerFlowGraph;
import com.ibm.wala.demandpa.util.MemoryAccessMap;
import com.ibm.wala.demandpa.util.SimpleMemoryAccessMap;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.sourcepos.Debug;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.StringStuff;
import de.unipotsdam.se.papds.dfg.PTAwithDFG;

import java.io.IOException;
import java.util.*;

public class OtherAnalysis {
    public static boolean debugmode = true;

    public static void printCG(CallGraph cg) {
        Queue<CGNode> queue = new LinkedList<>();
        queue.addAll(cg.getEntrypointNodes());
        while (!queue.isEmpty()) {
            CGNode t = queue.remove();
            Debug.info(t.toString());

            String s = "";
            for (Iterator<CGNode> it = cg.getSuccNodes(t); it.hasNext(); ) {
                CGNode m = it.next();
                s += m.toString() + "\n\t\t";
            }
            Debug.info(s);
            cg.getSuccNodes(t).forEachRemaining(queue::add);
        }
    }

    private static void printIR(CallGraph cg) {
        for (int i = 0; i < cg.getMaxNumber(); ++i) {
            CGNode t = cg.getNode(i);
            System.out.println(t);
            if (!t.getIR().isEmptyIR())
                t.getIR().iterateAllInstructions().forEachRemaining(q -> {
                    if (q != null) {
                        System.out.println(q.toString());
                    }
                });
            System.out.println();
        }
    }

    public static void zeroOneContextSensitive(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) throws CancelException {
        SSAPropagationCallGraphBuilder builder = Util.makeVanillaZeroOneContainerCFABuilder(options, cache, cha, scope);
        CallGraph cg = builder.makeCallGraph(options);
        HeapModel hm = builder.getPointerAnalysis().getHeapModel();
        HeapGraph g = builder.getPointerAnalysis().getHeapGraph();

        printCG(cg);
        PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();

//        for (int i = 0; i < pa.getHeapGraph().getMaxNumber(); ++i) {
//            System.out.println(pa.getHeapGraph().getNode(i));
//        }
//        for (PointerKey pk : pa.getPointerKeys()) {
//            System.out.println( pk + " " + pa.getPointsToSet(pk));
//        }
        for (int i = 0; i < cg.getMaxNumber(); ++i) {
            System.out.println(cg.getNode(i));
            CGNode node = cg.getNode(i);
            for (CGNode t : Iterator2Collection.toSet(cg.getSuccNodes(node))) {
                System.out.println("\t" + t);
            }
        }
    }


    public static void rtaGraph(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) throws CallGraphBuilderCancelException {
        CallGraphBuilder<InstanceKey> callGraphBuilder = Util.makeRTABuilder(options, cache, cha, scope);
        CallGraph cg = callGraphBuilder.makeCallGraph(options, null);
        //printCG(cg);
        for (int i = 0; i < cg.getMaxNumber(); ++i) {
            System.out.println(cg.getNode(i));
            CGNode node = cg.getNode(i);
            for (CGNode t : Iterator2Collection.toSet(cg.getSuccNodes(node))) {
                System.out.println("\t" + t);
            }
        }
    }

    public static void demandRefinement(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) throws CallGraphBuilderCancelException {
        final SSAPropagationCallGraphBuilder builder = Util
                .makeZeroOneCFABuilder(options, cache, cha, scope);
        final CallGraph cg = builder.makeCallGraph(options, null);
        final PointerAnalysis<InstanceKey> pa = builder
                .getPointerAnalysis();
        MemoryAccessMap fam = new SimpleMemoryAccessMap(cg, builder
                .getPointerAnalysis().getHeapModel(), false);
        DemandRefinementPointsTo fullDemandPointsTo = DemandRefinementPointsTo
                .makeWithDefaultFlowGraph(cg, builder, fam, cha, options,
                        new DummyStateMachine.Factory<IFlowLabel>());
        Collection<CGNode> epoints = cg.getEntrypointNodes();
        Iterable<PointerKey> pks = pa.getPointerKeys();
        HeapGraph<InstanceKey> hg = pa.getHeapGraph();

        printCG(cg);


        for (CGNode node : epoints) {
            IR ir = node.getIR();
            for (int i = 1; i < ir.getSymbolTable().getMaxValueNumber(); ++i) {
                PointerKey key = fam.getHeapModel().getPointerKeyForLocal(node, i);
                Collection<InstanceKey> p = fullDemandPointsTo.getPointsTo(key);
                System.out.println(p + "\t{" + p + "}");
            }

        }
    }

    public static void contextInsensitive(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) throws CancelException {
        SSAPropagationCallGraphBuilder builder = Util.makeZeroOneCFABuilder(options, cache, cha, scope);
        CallGraph cg = builder.makeCallGraph(options);
        printCG(cg);
        PointerAnalysis<InstanceKey> analysis = builder.getPointerAnalysis();
        HeapGraph<InstanceKey> heapGraph = analysis.getHeapGraph();

        for (PointerKey pk : analysis.getPointerKeys()) {
            System.out.println(pk + " " + analysis.getPointsToSet(pk));
        }

    }

    public static void objectSensitive(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) throws CancelException {
        SSAPropagationCallGraphBuilder zeroOneContainer = Util.makeZeroOneContainerCFABuilder(options, cache, cha, scope);
        CallGraph cg = zeroOneContainer.makeCallGraph(options);
        PointerAnalysis<InstanceKey> analysisContainer = zeroOneContainer.getPointerAnalysis();
        for (PointerKey p : analysisContainer.getPointerKeys()) {
            System.out.println(p + "\t{" + analysisContainer.getPointsToSet(p) + "}");
        }
    }

    public static void typeSensitive(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) throws CancelException {
        ContextSelector context = new ReceiverTypeContextSelector();
        DefaultSSAInterpreter ssa = new DefaultSSAInterpreter(options, cache);
        SSAPropagationCallGraphBuilder typeSSA = new nCFABuilder(3, cha, options, cache, context, ssa);
        CallGraph cg = typeSSA.makeCallGraph(options);
        assert (cg != null);

        PointerAnalysis<InstanceKey> pointerAnalysis = typeSSA.getPointerAnalysis();

        assert (pointerAnalysis != null);
        for (Iterator<PointerKey> it = typeSSA.iteratePointerKeys(); it.hasNext(); ) {
            PointerKey p = it.next();
            System.out.println(p + "\t{" + pointerAnalysis.getPointsToSet(p) + "}");
        }
    }

    private static Iterable<Entrypoint> makePublicEntrypoints(IClassHierarchy cha, String entryClass) {
        Collection<Entrypoint> result = new ArrayList<>();
        IClass klass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application,
                StringStuff.deployment2CanonicalTypeString(entryClass)));
        assert (klass != null);
        assert (klass.getDeclaredMethods() != null);
        for (IMethod m : klass.getDeclaredMethods()) {
            if (m.isPublic()) {
                result.add(new DefaultEntrypoint(m, cha));
            }
        }
        return result;
    }

    public static void printDemandPointerFlow(AnalysisOptions options, AnalysisCache cache, IClassHierarchy cha, AnalysisScope scope) throws CancelException {
        final SSAPropagationCallGraphBuilder builder = Util.makeZeroOneCFABuilder(options, cache, cha, scope);
        final CallGraph cg = builder.makeCallGraph(options, null);
        final PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();
        MemoryAccessMap fam = new SimpleMemoryAccessMap(cg, builder.getPointerAnalysis().getHeapModel(), false);

        PTAwithDFG pta = new PTAwithDFG(cg, fam, cha, builder.getPointerAnalysis().getHeapModel());
        SimpleDemandPointerFlowGraph dfg = new SimpleDemandPointerFlowGraph(cg, fam.getHeapModel(), fam, cha);
        for (int i = 0; i < dfg.getMaxNumber(); ++i) {
            System.out.println(dfg.getNode(i));
        }
    }

    public static void main(String args[]) throws IOException, CancelException {
        String scopeFileName;
        String mainClass;
        String exFile;
        Debug.setMinLogLevel(Debug.LogLevel.DEBUG);

        if (debugmode) {
            scopeFileName = "scopeFiles/Scope_avrora.txt";
            mainClass = "Ljintgen/Main";
            exFile = "exclude.txt";
        } else {
            scopeFileName = args[1];
            mainClass = args[2];
            exFile = args[3];
        }
        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFileName, new FileProvider().getFile(exFile), OtherAnalysis.class.getClassLoader());

        ClassHierarchy cha = null;

        try {
            cha = ClassHierarchyFactory.make(scope);

        } catch (ClassHierarchyException e) {
            System.out.println(e);
        }


        Iterable<Entrypoint> entryPoints;
        entryPoints = Util.makeMainEntrypoints(scope, cha, mainClass);
        AnalysisCache cache = new AnalysisCacheImpl();

        AnalysisOptions options = new AnalysisOptions();
        options.setEntrypoints(entryPoints);

        IMethod main = null;
        for (IClass c : cha) {
            for (IMethod m : c.getDeclaredMethods()) {
                if (m.getName().toString().equals("main") && scope.isApplicationLoader(c.getClassLoader())) {
                    main = m;
                }
            }
        }
        //OtherAnalysis.zeroOneContextSensitive(options, cache, cha, scope);
        //OtherAnalysis.contextInsensitive(options, cache, cha, scope);
        //OtherAnalysis.printDemandPointerFlow(options, cache, cha, scope);
        //OtherAnalysis.rtaGraph(options,cache,cha,scope);
        //OtherAnalysis.contextInsensitive(options,cache,cha, scope);
        //OtherAnalysis.objectSensitive(options,cache,cha,scope);
        //OtherAnalysis.zeroOneContextSensitive(options,cache,cha,scope);
        //OtherAnalysis.objectSensitive(options, cache, cha, scope);
        OtherAnalysis.zeroOneContextSensitive(options, cache, cha, scope);

        /*
        CallGraphBuilder cgb = Util.makeRTABuilder(options, cache, cha, scope);
        CallGraph cg = cgb.makeCallGraph(options, null);
        Debug.info("Number of CallGraph nodes " + cg.getNumberOfNodes());
        //printCG(cg);
        FifoQueue<CGNode> queue = new FifoQueue<>();
        queue.push(cg.getEntrypointNodes().iterator());

        while (!queue.isEmpty()) {
            CGNode t = queue.pop();
            System.out.println(queue.size());
            System.out.println(t.toString());
            cg.getSuccNodes(t).forEachRemaining(queue::push);
        }
        */

        //OtherAnalysis.printDemandPointerFlow(options,cache, cha, scope);


    }
}
