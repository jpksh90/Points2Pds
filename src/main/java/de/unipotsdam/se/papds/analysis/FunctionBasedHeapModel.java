/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 */

package de.unipotsdam.se.papds.analysis;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.FilteredPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import de.unipotsdam.se.papds.analysis.coreStructures.FieldKey;
import de.unipotsdam.se.papds.analysis.coreStructures.Instance;
import de.unipotsdam.se.papds.analysis.coreStructures.StaticFieldKey;
import de.unipotsdam.se.papds.analysis.coreStructures.VariableKey;

import java.util.Iterator;
import java.util.List;


//TODO: Refactor this class
//Define every thing as a factory methods

/**
 * This class create a method based heap abstraction for each of the method
 */
public class FunctionBasedHeapModel implements HeapModel {


    //List of Instance Keys for each method
    private static SetMultimap<IMethod, Instance> instances = MultimapBuilder.hashKeys().hashSetValues().build();

    //List of Variable Key for each method
    private static SetMultimap<IMethod, VariableKey> variables = MultimapBuilder.hashKeys().hashSetValues().build();

    //List of static field keys for each class
    private static Table<IClass, IField, StaticFieldKey> staticFieldKeys = HashBasedTable.create();

    //List of Instance Field Keys for each class
    private static SetMultimap<Pair<IMethod, VariableKey>, FieldKey> instanceFieldKeys = MultimapBuilder.hashKeys().hashSetValues().build();

    /*
        List of Static variables which only have a class scope
     */
    private static Table<IClass, IField, List<Instance>> staticFieldsPointsTo = HashBasedTable.create();


    //Maps a Variable to an Instance
    private static SetMultimap<VariableKey, Instance> varPointsTo = MultimapBuilder.hashKeys().hashSetValues().build();


    //Maps a static field to an Instance
    private static SetMultimap<Pair<IClass, IField>, Instance> staticFieldPointsTo = MultimapBuilder.hashKeys().hashSetValues().build();


    //Maps a instance field to a points-to set
    private static Table<VariableKey, IField, Instance> instanceFieldPointsTo = HashBasedTable.create();


    /**
     * For each variable, create a new variable key otherwise add it
     * TODO: Canocalize variables names
     */


    public static VariableKey getVariableKey(IMethod method, int vn) {

        VariableKey var = new VariableKey(vn, method);
        if (variables.get(method).contains(var)) {
            return var;
        } else {
            variables.get(method).add(var);
            return var;
        }
    }

    /*
        For each instance method, create a new instance
     */
    public static Instance getInstance(IMethod method, TypeName tn, int lineno) {
        Instance in = new Instance(tn, lineno);
        if (instances.get(method).contains(in)) {
            return in;
        } else {
            instances.get(method).add(in);
            return in;
        }
    }


    /*
        For each class, create a new static field key
     */
    public static StaticFieldKey getStaticFieldKey(IClass klass, IField field) {
        StaticFieldKey fieldKey = new StaticFieldKey(klass, field);
        if (staticFieldKeys.contains(klass, field)) {
            return staticFieldKeys.get(klass, field);
        } else {
            staticFieldKeys.put(klass, field, fieldKey);
            return fieldKey;
        }
    }


    public static void addPointsTo(VariableKey v, Instance i) {
        varPointsTo.put(v, i);
    }


    public static void addStaticPointsTo(IClass k, IField f, Instance i) {
        Pair<IClass, IField> p = Pair.make(k, f);
        staticFieldPointsTo.put(p, i);
    }

    public static void addinstanceFieldPointsTo(VariableKey v, IField f, Instance i) {
        instanceFieldPointsTo.put(v, f, i);
    }

    @Override
    public Iterator<PointerKey> iteratePointerKeys() {
        return null;
    }

    @Override
    public IClassHierarchy getClassHierarchy() {
        return null;
    }

    @Override
    public InstanceKey getInstanceKeyForAllocation(CGNode cgNode, NewSiteReference newSiteReference) {
        return null;
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
        return null;
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
