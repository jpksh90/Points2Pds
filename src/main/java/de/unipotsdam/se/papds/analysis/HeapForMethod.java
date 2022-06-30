package de.unipotsdam.se.papds.analysis;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.util.collections.ArraySet;
import de.unipotsdam.se.papds.analysis.coreStructures.Instance;
import de.unipotsdam.se.papds.analysis.coreStructures.VariableKey;

import java.util.Collection;
import java.util.Iterator;

public class HeapForMethod {
    private IMethod method;
    private Collection<NewSiteReference> allocationSites; //map all allocation sites to this
    private Collection<VariableKey> variableKeys; //map all the variables to this variable key
    private Collection<Instance> instances;

    //TODO: Add field variables


    /**
     * This function initialized a heap for method
     *
     * @param method
     */
    public HeapForMethod(IMethod method) {
        this.method = method;
        this.allocationSites = new ArraySet<>();
        this.variableKeys = new ArraySet<>();
        this.instances = new ArraySet<>();
    }

    /**
     * @param nsr
     */
    public void addAllocationSite(NewSiteReference nsr) {
        allocationSites.add(nsr);
        //Get the instance type and add Instance
        Instance i = new Instance(nsr.getDeclaredType().getName(), nsr.getProgramCounter());
        instances.add(i);
    }

    public void addVariableKey(VariableKey v) {
        variableKeys.add(v);
    }

    public Iterator<Instance> getInstances() {
        return instances.iterator();
    }

    public Iterator<VariableKey> getVariables() {
        return variableKeys.iterator();
    }
}
