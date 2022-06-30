/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 */

package de.unipotsdam.se.papds.pointerstructs;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.collections.ArraySet;
import com.ibm.wala.util.collections.BimodalMap;

import java.util.Collection;
import java.util.Iterator;

/**
 * This calss create a method based heap abstraction for each of the method
 */
public class FunctionBasedHeapModel {

    /*
     * Maps each method to a Heap for each function. This ensures function abstraction
     * TODO: Check how it can be extended to libraries
     */
    private static BimodalMap<IMethod, HeapForFunction> heapForMethod;
    private IClassHierarchy cha;

    FunctionBasedHeapModel(IClassHierarchy cha) {
        this.cha = cha;
        heapForMethod = new BimodalMap<>(1000); //switches to a efficient map representation after cut-off
    }

    public void addVariableKey(IMethod m, VariableKey v) {
        if (!heapForMethod.containsKey(m)) {
            //Create a new heap for the function
            HeapForFunction hfm = new HeapForFunction(m);
            hfm.addVariableKey(v);
        } else {
            heapForMethod.get(m).addVariableKey(v);
        }
    }

    public void addAllocationSite(IMethod m, NewSiteReference nsr) {
        if (!heapForMethod.containsKey(m)) {
            HeapForFunction hfm = new HeapForFunction(m);
            hfm.addAllocationSite(nsr);
        }
    }

    public Iterator<Instance> getInstanceForMethod(IMethod m) {
        if (heapForMethod.containsKey(m)) {
            return heapForMethod.get(m).getInstances();
        } else {
            throw new IllegalArgumentException("Method m not found " + this.getClass().getEnclosingMethod().getName());
        }
    }

    public Iterator<VariableKey> getVariableForMethod(IMethod m) {
        if (heapForMethod.containsKey(m)) {
            return heapForMethod.get(m).getVariables();
        } else {
            throw new IllegalArgumentException("Method m not found " + this.getClass().getEnclosingMethod().getName());
        }
    }

    /**
     * Central Data Structure to store heap objects per function size
     */
    private static class HeapForFunction {
        private IMethod method;
        private Collection<NewSiteReference> allocationSites; //map all allocation sites to this
        private Collection<VariableKey> variableKeys; //map all the variables to this variable key
        private Collection<Instance> instances;

        //TODO: Add field variables

        public HeapForFunction(IMethod method) {
            this.method = method;
            this.allocationSites = new ArraySet<>();
            this.variableKeys = new ArraySet<>();
            this.instances = new ArraySet<>();
        }

        public void addAllocationSite(NewSiteReference nsr) {
            allocationSites.add(nsr);
            //Get the instance type and add Instance
            Instance i = new Instance(nsr.getDeclaredType().getName(), nsr.getProgramCounter());
            instances.add(i);
        }

        public void addVariableKey(VariableKey v) {
            variableKeys.add(v);
        }


        public Iterator<NewSiteReference> newSiteReferenceIterator() {
            return allocationSites.iterator();
        }

        public Iterator<Instance> getInstances() {
            return instances.iterator();
        }

        public Iterator<VariableKey> getVariables() {
            return variableKeys.iterator();
        }
    }


    //TODO: Add for field map
}
