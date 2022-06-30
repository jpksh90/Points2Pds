package de.unipotsdam.se.papds.analysis;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.classLoader.ProgramCounter;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import de.unipotsdam.se.papds.analysis.coreStructures.FieldKey;
import de.unipotsdam.se.papds.analysis.coreStructures.Instance;
import de.unipotsdam.se.papds.analysis.coreStructures.VariableKey;

import java.util.Iterator;

/**
 * Represents a Points-To set heap model
 */
public class PDSHeapModel implements HeapModel {

    private IClassHierarchy cha;

    /*
        Maps a CGNode to a list of InstanceKeys, enables context-sensitive heap abstraction without
        "explicitly" cloning heap
     */
    private SetMultimap<CGNode, InstanceKey> instanceKeysInCGNode = MultimapBuilder.hashKeys().hashSetValues().build();


    /*
        Map every CGNode to a pointer key
     */
    private SetMultimap<CGNode, PointerKey> pointerKeysInCGNode = MultimapBuilder.hashKeys().hashSetValues().build();


    PDSHeapModel(IClassHierarchy cha) {
        this.cha = cha;
    }

    @Override
    public Iterator<PointerKey> iteratePointerKeys() {
        return null;
    }

    @Override
    public IClassHierarchy getClassHierarchy() {
        return this.cha;
    }

    @Override
    public InstanceKey getInstanceKeyForAllocation(CGNode cgNode, NewSiteReference newSiteReference) {
        Instance instance = new Instance(newSiteReference.getDeclaredType().getName(), newSiteReference.getProgramCounter());
        instanceKeysInCGNode.put(cgNode, instance);
        return instance;
    }

    @Override
    public InstanceKey getInstanceKeyForMultiNewArray(CGNode cgNode, NewSiteReference newSiteReference, int i) {
        return null;
    }

    @Override
    public <T> InstanceKey getInstanceKeyForConstant(TypeReference typeReference, T t) {
        return null;
    }

    @Override
    public InstanceKey getInstanceKeyForPEI(CGNode cgNode, ProgramCounter programCounter, TypeReference typeReference) {
        return null;
    }

    @Override
    public InstanceKey getInstanceKeyForMetadataObject(Object o, TypeReference typeReference) {
        return null;
    }

    @Override
    public PointerKey getPointerKeyForLocal(CGNode cgNode, int i) {
        VariableKey var = new VariableKey(i, cgNode.getMethod());
        pointerKeysInCGNode.put(cgNode, var);
        return var;
    }

    @Override
    public FilteredPointerKey getFilteredPointerKeyForLocal(CGNode cgNode, int i, FilteredPointerKey.TypeFilter typeFilter) {
        return null;
    }

    @Override
    public PointerKey getPointerKeyForReturnValue(CGNode cgNode) {
        return null;
    }

    @Override
    public PointerKey getPointerKeyForExceptionalReturnValue(CGNode cgNode) {
        return null;
    }

    @Override
    public PointerKey getPointerKeyForStaticField(IField iField) {
        return null;
    }

    @Override
    public PointerKey getPointerKeyForInstanceField(InstanceKey instanceKey, IField iField) {
        return null;
    }

    @Override
    public PointerKey getPointerKeyForArrayContents(InstanceKey instanceKey) {
        return null;
    }
}
