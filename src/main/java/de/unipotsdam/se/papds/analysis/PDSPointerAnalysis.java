package de.unipotsdam.se.papds.analysis;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.intset.MutableMapping;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.OrdinalSetMapping;
import de.unipotsdam.se.papds.analysis.coreStructures.Instance;
import de.unipotsdam.se.papds.analysis.coreStructures.VariableKey;

import java.util.Collection;
import java.util.Map;

public class PDSPointerAnalysis implements PointerAnalysis {


    IClassHierarchy cha;
    HeapGraph hg;
    HeapModel hm;

    /*
        Maps a pointer key to a list of pointsto set
     */
    private Map<PointerKey, OrdinalSet<InstanceKey>> pointerKeyMap = HashMapFactory.make();


    /*
        Map all instance key to a mutable mapping, used in OrdinalSet Mapping
     */
    MutableMapping<Instance> instances = MutableMapping.make();

    public PDSPointerAnalysis(IClassHierarchy cha) {
        this.cha = cha;
        this.hm = new PDSHeapModel(cha);
    }

    @Override
    public OrdinalSet getPointsToSet(PointerKey pointerKey) {
        return pointerKeyMap.get(pointerKey);
    }

    @Override
    public HeapModel getHeapModel() {
        return this.hm;
    }

    @Override
    public HeapGraph getHeapGraph() {
        return this.
    }

    @Override
    public OrdinalSetMapping getInstanceKeyMapping() {
        return null;
    }

    @Override
    public Iterable<PointerKey> getPointerKeys() {
        return null;
    }

    @Override
    public Collection getInstanceKeys() {
        return pointerKeyMap.values();
    }

    @Override
    public boolean isFiltered(PointerKey pointerKey) {
        return false;
    }

    @Override
    public IClassHierarchy getClassHierarchy() {
        return this.cha;
    }


    public void addPointsTo(VariableKey variableKey, Instance instance) {
        map.put(variableKey
    }

}
