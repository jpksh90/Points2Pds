/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 */

package de.unipotsdam.se.papds.main;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.sourcepos.Debug;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.debug.Assertions;
import de.unipotsdam.se.papds.analysis.FunctionBasedHeapModel;
import de.unipotsdam.se.papds.analysis.coreStructures.Instance;
import de.unipotsdam.se.papds.analysis.coreStructures.StaticFieldKey;
import de.unipotsdam.se.papds.analysis.coreStructures.VariableKey;
import de.unipotsdam.se.papds.graphlib.*;
import de.unipotsdam.se.papds.pdscallgraph.PDSCGNode;

import java.util.ArrayList;
import java.util.Set;

public class SSAVisitorImpl implements SSAInstruction.IVisitor {

    private Graph g;
    private IMethod m;
    private IR ir;
    private IClassHierarchy cha;
    private PDSCGNode pdscgNode;
    private CallGraph callGraph;
    private CGNode ctxtInsensCallee;

    public SSAVisitorImpl(Graph g, IMethod method, IR ir, IClassHierarchy cha, PDSCGNode node, CallGraph callGraph, CGNode contextInsensOrg) {
        this.g = g;
        this.m = method;
        this.ir = ir;
        this.cha = cha;
        this.pdscgNode = node;
        this.callGraph = callGraph;
        this.ctxtInsensCallee = contextInsensOrg;

    }

    private void mapExceptions() {

    }

    void processInstructions() {
        if (ir == null) {
            Debug.debug("No IR instructions" + m);
        } else {
            for (SSAInstruction ssa : ir.getInstructions()) {
                if (ssa instanceof SSAGotoInstruction) {
                    visitGoto((SSAGotoInstruction) ssa);
                } else if (ssa instanceof SSAArrayLoadInstruction) {
                    visitArrayLoad((SSAArrayLoadInstruction) ssa);
                } else if (ssa instanceof SSAArrayStoreInstruction) {
                    visitArrayStore((SSAArrayStoreInstruction) ssa);
                } else if (ssa instanceof SSABinaryOpInstruction) {
                    visitBinaryOp((SSABinaryOpInstruction) ssa);
                } else if (ssa instanceof SSAUnaryOpInstruction) {
                    visitUnaryOp((SSAUnaryOpInstruction) ssa);
                } else if (ssa instanceof SSAConversionInstruction) {
                    visitConversion((SSAConversionInstruction) ssa);
                } else if (ssa instanceof SSAComparisonInstruction) {
                    visitComparison((SSAComparisonInstruction) ssa);
                } else if (ssa instanceof SSAConditionalBranchInstruction) {
                    visitConditionalBranch((SSAConditionalBranchInstruction) ssa);
                } else if (ssa instanceof SSASwitchInstruction) {
                    visitSwitch((SSASwitchInstruction) ssa);
                } else if (ssa instanceof SSAReturnInstruction) {
                    visitReturn((SSAReturnInstruction) ssa);
                } else if (ssa instanceof SSAGetInstruction) {
                    visitGet((SSAGetInstruction) ssa);
                } else if (ssa instanceof SSAPutInstruction) {
                    visitPut((SSAPutInstruction) ssa);
                } else if (ssa instanceof SSAInvokeInstruction) {
                    visitInvoke((SSAInvokeInstruction) ssa);
                } else if (ssa instanceof SSANewInstruction) {
                    visitNew((SSANewInstruction) ssa);
                } else if (ssa instanceof SSAArrayLengthInstruction) {
                    visitArrayLength((SSAArrayLengthInstruction) ssa);
                } else if (ssa instanceof SSAThrowInstruction) {
                    visitThrow((SSAThrowInstruction) ssa);
                } else if (ssa instanceof SSAMonitorInstruction) {
                    visitMonitor((SSAMonitorInstruction) ssa);
                } else if (ssa instanceof SSACheckCastInstruction) {
                    visitCheckCast((SSACheckCastInstruction) ssa);
                } else if (ssa instanceof SSAInstanceofInstruction) { //ssa is an instance of InstanceOf :-)
                    visitInstanceof((SSAInstanceofInstruction) ssa);
                } else if (ssa instanceof SSAPhiInstruction) {
                    visitPhi((SSAPhiInstruction) ssa);
                } else if (ssa instanceof SSAPiInstruction) {
                    visitPi((SSAPiInstruction) ssa);
                } else if (ssa instanceof SSAGetCaughtExceptionInstruction) {
                    visitGetCaughtException((SSAGetCaughtExceptionInstruction) ssa);
                } else if (ssa instanceof SSALoadMetadataInstruction) {
                    visitLoadMetadata((SSALoadMetadataInstruction) ssa);
                }
            }
        }
    }


    @Override
    public void visitGoto(SSAGotoInstruction instruction) {
        //goto only alters the flow => we are not aiming for flow sensitivity


    }

    /**
     * use[0] of an array load instruction maps to the array variable and use[1] map to an index. Our analysis
     * merges all the array contents to a single variable and doesn't
     * and doesn't differentiate between each element of an array
     *
     * @param instruction
     */
    @Override
    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {


        VariableKey v1 = FunctionBasedHeapModel.getVariableKey(m, instruction.getDef());
        VariableKey v2 = FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(0));
        g.addEdge(new VariableNode(v1), new VariableNode(v2), new Label(lbl.ASSIGN));
    }

    /**
     * use[0] defines the target of arraystore instruction and use[2] determines the source.  The analysis merges all the arrays indexes to
     * a single index 0.
     *
     * @param instruction
     */
    @Override
    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
        VariableKey v1 = FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(0));
        VariableKey v2 = FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(2));
        g.addEdge(new VariableNode(v1), new VariableNode(v2), new Label(lbl.ASSIGN));
    }

    /**
     * Binary operations are applied to primitive data types which is not handled by Pointer analysis
     *
     * @param instruction
     */
    @Override
    public void visitBinaryOp(SSABinaryOpInstruction instruction) {

    }

    /**
     * Behaviour same as unary operation
     *
     * @param instruction
     */
    @Override
    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {

    }

    @Override
    public void visitConversion(SSAConversionInstruction instruction) {

    }

    @Override
    public void visitComparison(SSAComparisonInstruction instruction) {
        //Only applies for primitive data types

    }

    @Override
    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
        //Flow insensitive analysis

    }

    @Override
    public void visitSwitch(SSASwitchInstruction instruction) {

    }

    @Override
    public void visitReturn(SSAReturnInstruction instruction) {
        //Get the predecessor of the CGNode and map it to

        //TODO: Take a return key for each node and map it to the variable
        if (instruction.getUse(0) != -1) {
            VariableKey v1 = FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(0));
            VariableNode vnode = new VariableNode(v1.getVn(), m);
            g.addEdge(vnode, new VariableNode(pdscgNode.getReturnKey()), new Label(lbl.ASSIGN));
        }

    }


    //SSA GET Instruction x = y.f
    @Override
    public void visitGet(SSAGetInstruction instruction) {
        //If the static or non static get the assignment
        VariableKey var = FunctionBasedHeapModel.getVariableKey(m, instruction.getDef());
        VariableNode vnode = new VariableNode(var.getVn(), m);
        IField field = cha.resolveField(instruction.getDeclaredField());
        if (field != null) {
            IClass klass = field.getDeclaringClass();


            if (instruction.isStatic()) {
                //Get the class class reference and field name
                StaticFieldKey fk = FunctionBasedHeapModel.getStaticFieldKey(klass, field);
                StaticFieldNode sfn = new StaticFieldNode(fk.getKlass(), fk.getField());
                g.addEdge(sfn, vnode, new Label(lbl.STATICGETFIELD, field.toString()));
            } else {
                //If its not a static field, then get the variable node for the the varaiable
                VariableKey var2 = FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(0));
                VariableNode var2node = new VariableNode(var2.getVn(), m);
                g.addEdge(var2node, vnode, new Label(lbl.GETFIELD, field.toString()));
            }
        }

    }

    /*
        SSA PutInstructions : x.f = y
     */
    @Override
    public void visitPut(SSAPutInstruction instruction) {
        VariableKey var = FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(0));
        VariableNode varNode = new VariableNode(var.getVn(), m);

        IField fld = cha.resolveField(instruction.getDeclaredField());
        if (fld != null) {
            if (instruction.isStatic()) {
                //Data flow from Variable to Static Field
                StaticFieldKey sfk = FunctionBasedHeapModel.getStaticFieldKey(fld.getDeclaringClass(), fld);
                StaticFieldNode sfn = new StaticFieldNode(sfk.getKlass(), sfk.getField());
                g.addEdge(varNode, sfn, new Label(lbl.STATICPUTFIELD, fld.toString()));
            } else {
                VariableKey varSrc = FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(1));
                VariableNode varSrcnode = new VariableNode(varSrc.getVn(), m);
                g.addEdge(varSrcnode, varNode, new Label(lbl.PUTFIELD, fld.toString()));
            }
        }

    }

    @Override
    public void visitInvoke(SSAInvokeInstruction instruction) {

        /*
         * UGLY HACK
         *
         *
         */

        //TODO: This hack doesn;t work if a invokeInstruction invokes an interface. Somehow, here one needs to find the possible targets and invoke the fixpoint-solver for resolving the targets


        //Get the target method
        Set<CGNode> targets = callGraph.getPossibleTargets(this.ctxtInsensCallee, instruction.getCallSite());
        Debug.debug(String.format("Targets for %s : [%s]", instruction, targets.toString()));


        for (CGNode target : targets) {
            IMethod tm = target.getMethod();

            //Idea: Get the reciever of the virtual call and then
            if (instruction.getNumberOfUses() > 0 && instruction.getNumberOfParameters() > 0) {
                ArrayList<VariableKey> variableKeys = new ArrayList<>();
                for (int i = 0; i < instruction.getNumberOfUses(); i++) {
                    variableKeys.add(FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(i)));
                }

                ArrayList<VariableKey> targetParams = new ArrayList<>();
                for (int i = 0; i < instruction.getNumberOfParameters(); i++) {
                    targetParams.add(FunctionBasedHeapModel.getVariableKey(tm, i + 1));
                }

                assert (variableKeys.size() == targetParams.size());

                VariableNode src = new VariableNode(variableKeys.get(0));
                VariableNode tgt = new VariableNode(targetParams.get(0));
                g.addEdge(src, tgt, new Label(lbl.CALL, src.getMethod().getName().toString() + String.valueOf(instruction.iindex)));
                g.addEdge(tgt, src, new Label(lbl.RETURN, src.getMethod().getName().toString() + String.valueOf(instruction.iindex)));


                for (int i = 1; i < variableKeys.size(); ++i) {
                    src = new VariableNode(variableKeys.get(i));
                    tgt = new VariableNode(targetParams.get(i));
                    g.addEdge(src, tgt, new Label(lbl.CALL, src.getMethod().getName().toString() + String.valueOf(instruction.iindex)));
                }


                //Map the return variable
                //Map the value with the variable defined in the instruction
                if (instruction.getDef() != -1) {
                    VariableKey returnVal = FunctionBasedHeapModel.getVariableKey(m, instruction.getDef());
                    VariableKey returnKey = pdscgNode.getReturnKey();

                    VariableNode rtnVal = new VariableNode(returnVal);
                    VariableNode rtnFun = new VariableNode(returnKey);
                    g.addEdge(rtnFun, rtnVal, new Label(lbl.RETURN, src.getMethod().getName().toString() + String.valueOf(instruction.iindex)));
                }
            }
        }
    }

    public void visitPiNodes(SSAPiInstruction[] instructions) {
        Assertions.UNREACHABLE();
    }

    public void visitPhiNodes(SSAPhiInstruction[] instructions) {
        for (SSAInstruction instruction : instructions) {
            //Add "assign edge between  use and def of PhiNode
            VariableKey v1 = FunctionBasedHeapModel.getVariableKey(m, instruction.getDef());
            ArrayList<VariableKey> variableKeyArray = new ArrayList<>(instruction.getNumberOfUses());
            for (int i = 0; i < instruction.getNumberOfUses(); ++i) {
                if (instruction.getUse(i) != -1) {
                    VariableKey v = FunctionBasedHeapModel.getVariableKey(m, instruction.getUse(i));
                    variableKeyArray.add(v);
                }
            }
            variableKeyArray.forEach(q -> g.addEdge(new VariableNode(v1.getVn(), v1.getM()), new VariableNode(v1.getVn(), v1.getM()), new Label(lbl.ASSIGN)));
            variableKeyArray.forEach(v -> FunctionBasedHeapModel.getVariableKey(m, v.getVn()));
        }
    }

    @Override
    public void visitNew(SSANewInstruction instruction) {
        if (instruction == null) {
            throw new IllegalArgumentException("instruction cannot be null " + this.getClass().getEnclosingMethod().getName());
        }
        //Get the instruction name and add it a points to graph. Store the type information
        Instance instance = new Instance(instruction.getConcreteType().getName(), instruction.iindex);
        VariableKey variable = new VariableKey(instruction.getDef(0), m);

        Driver.getPointerAnalysis().

        ObjectNode n = new ObjectNode(instance.getTypeName(), m, instruction.iindex);
        VariableNode vn = new VariableNode(variable.getVn(), m);
        g.addEdge(n, vn, new Label(lbl.NEW));
    }

    @Override
    public void visitArrayLength(SSAArrayLengthInstruction instruction) {
        //Do  nothing here

    }


    @Override
    public void visitThrow(SSAThrowInstruction instruction) {
        //TODO: Decide whether to map exceptions to a string or exception object
        //Map of the exceptions to string
    }

    @Override
    public void visitMonitor(SSAMonitorInstruction instruction) {
        //A monitor instruction has no effect on pointer analysis


    }

    @Override
    public void visitCheckCast(SSACheckCastInstruction instruction) {
        VariableKey v1 = new VariableKey(instruction.getResult(), m);
        VariableKey v2 = new VariableKey(instruction.getVal(), m);
        g.addEdge(new VariableNode(v1), new VariableNode(v2), new Label(lbl.ASSIGN));
    }

    @Override
    public void visitInstanceof(SSAInstanceofInstruction instruction) {
        //InstanceOf instruction has no effect on heap

    }

    @Override
    public void visitPhi(SSAPhiInstruction instruction) {
        //Add the edges for Phi Instruction
        VariableKey v1 = new VariableKey(instruction.getDef(), m);

        for (int i = 0; i < instruction.getNumberOfUses(); ++i) {
            VariableKey vn = new VariableKey(instruction.getUse(i), m);
            g.addEdge(new VariableNode(v1), new VariableNode(vn), new Label(lbl.ASSIGN));
        }

    }

    @Override
    public void visitPi(SSAPiInstruction instruction) {
        //TODO: Finish the

    }

    @Override
    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {

    }

    @Override
    public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {

    }
}
