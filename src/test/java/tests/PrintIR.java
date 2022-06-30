/*
 * Copyright (c) 2018.  Jyoti Prakash
 *
 */

package tests;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.FifoQueue;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;
import de.unipotsdam.se.papds.main.Main;

import java.io.IOException;
import java.util.Iterator;

public class PrintIR {

    private static boolean debugmode = true;


    //Set to true to print use-def sets
    private static boolean verbose = true;

    public static void main(String args[]) throws IOException, CancelException {

        String scopeFileName = (debugmode) ? "scopeFiles/Scope_This.txt" : args[1];
        String mainClass = (debugmode) ? "LMain" : args[2];
        String exFile = "exclude.txt";

        AnalysisScope scope = AnalysisScopeReader.readJavaScope(scopeFileName, new FileProvider().getFile(exFile), Main.class.getClassLoader());
        AnalysisCache cache = new AnalysisCacheImpl();
        AnalysisOptions options = new AnalysisOptions();
        options.setAnalysisScope(scope);


        ClassHierarchy cha = null;
        try {
            cha = ClassHierarchyFactory.make(scope);

        } catch (ClassHierarchyException e) {
            System.out.println(e);
        }

        Iterable<Entrypoint> entryPoints;
        entryPoints = Util.makeMainEntrypoints(scope, cha, mainClass);
        options.setAnalysisScope(scope);
        options.setEntrypoints(entryPoints);
        SSAPropagationCallGraphBuilder zeroOneContainer = Util.makeZeroOneContainerCFABuilder(options, cache, cha, scope);
        CallGraph cg = zeroOneContainer.makeCallGraph(options);


        FifoQueue<CGNode> queue = new FifoQueue<>();
        queue.push(cg.getEntrypointNodes().iterator());


        while (!queue.isEmpty()) {
            CGNode n = queue.pop();
            System.out.println("================ Node " + n + " =======================");
            IR ir = n.getIR();
            if (ir != null) {
                Iterator<SSAInstruction> inst = ir.iterateAllInstructions();
                while (inst.hasNext()) {
                    SSAInstruction i = inst.next();
                    if (i instanceof SSAConditionalBranchInstruction) {
                        System.out.println("Inst" + i.iindex + " " + i);
                        if (verbose) {
                            System.out.println("Number of Uses and Defs " + i.getNumberOfUses() + " " + i.getNumberOfDefs());
                            System.out.print("Uses ");
                            for (int c = 0; c < i.getNumberOfUses(); ++c) {
                                System.out.print(i.getUse(c) + " ");
                            }
                            System.out.println("\nDefs " + i.getDef());
                        }
                    }
                }
            } else {
                System.out.println("No IR instruction");
            }
            queue.push(cg.getSuccNodes(n));
        }
    }
}
