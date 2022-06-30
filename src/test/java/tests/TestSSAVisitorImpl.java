package tests;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.FifoQueue;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import de.unipotsdam.se.papds.graphlib.Graph;
import de.unipotsdam.se.utils.OtherAnalysis;

import java.io.IOException;

public class TestSSAVisitorImpl {

    public static boolean debugmode = true;

    public static void main(String args[]) throws IOException, CancelException {
        String scopeFileName;
        String mainClass;
        String exFile;

        if (debugmode) {
            scopeFileName = "Scope_Assign.txt";
            mainClass = "LAssign";
            exFile = "exclude.txt";
        } else {
            scopeFileName = args[1];
            mainClass = args[2];
            exFile = args[3];
        }
        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFileName, new FileProvider().getFile(exFile), OtherAnalysis.class.getClassLoader());
        AnalysisCache cache = new AnalysisCacheImpl();
        ClassHierarchy cha = null;
        try {
            cha = ClassHierarchyFactory.make(scope);

        } catch (ClassHierarchyException e) {
            System.out.println(e);
        }
        System.out.print("Number of Classes = " + cha.getNumberOfClasses());
        AnalysisOptions options = new AnalysisOptions();
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);
        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha, mainClass);
        options.setEntrypoints(entrypoints);

        SSAPropagationCallGraphBuilder callGraphBuilder = Util.makeZeroOneCFABuilder(options, cache, cha, scope);
        CallGraph cg = callGraphBuilder.makeCallGraph(options);

        FifoQueue<CGNode> queue = new FifoQueue<>();
        queue.push(cg.getEntrypointNodes().iterator());
        Graph ptg = new Graph();
        while (!queue.isEmpty()) {
//            SSAVisitorImpl t = new SSAVisitorImpl();
        }
    }
}
