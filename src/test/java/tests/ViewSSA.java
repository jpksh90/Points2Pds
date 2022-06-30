package tests;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.sourcepos.Debug;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import de.unipotsdam.se.utils.OtherAnalysis;

import java.io.IOException;

public class ViewSSA {

    public static void main(String args[]) throws CancelException, IOException {
        String scopeFileName;
        String mainClass;
        String exFile;
        Debug.setMinLogLevel(Debug.LogLevel.INFO);

        scopeFileName = "scopeFiles/Scope_Inheritance.txt";
        mainClass = "LSample";
        exFile = "exclude.txt";

        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFileName, new FileProvider().getFile(exFile), OtherAnalysis.class.getClassLoader());

        ClassHierarchy cha = null;

        try {
            cha = ClassHierarchyFactory.make(scope);

        } catch (ClassHierarchyException e) {
            System.out.println(e);
        }

        AnalysisOptions options = new AnalysisOptions();
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.FULL);

        SSAPropagationCallGraphBuilder builder = Util.makeZeroOneCFABuilder(options, new AnalysisCacheImpl(), cha, scope);
        CallGraph cg = builder.makeCallGraph(options);


        Iterable<Entrypoint> entrypoints = Util.makeMainEntrypoints(scope, cha, mainClass);

        for (int i = 0; i < cg.getMaxNumber(); ++i) {
            CGNode t = cg.getNode(i);
            System.out.println(t.getIR());
        }
    }
}
