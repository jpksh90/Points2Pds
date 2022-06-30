package de.unipotsdam.se.papds.pointerstructs;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import com.ibm.wala.util.collections.Pair;

/**
 * Represents a Points-To set heap model
 */
public class PDSHeapModel {

    private SetMultimap<VariableKey, Instance> varPointsTo;
    private SetMultimap<Pair<Instance, FieldKey>, Instance> heapPointsTo;

    PDSHeapModel() {
        varPointsTo = MultimapBuilder.hashKeys().linkedHashSetValues().build();
        heapPointsTo = MultimapBuilder.hashKeys().linkedHashSetValues().build();
    }


    /**
     * Adds a new VarPointsTo set
     *
     * @param key
     * @param instance
     */
    void addVarPointsTo(VariableKey key, Instance instance) {
        if (varPointsTo.containsKey(key)) {
            varPointsTo.get(key).add(instance);
        } else {
            varPointsTo.put(key, instance);
        }
    }

    /**
     * Adds a new heapPointsTo. Used for GET and PUT operations
     *
     * @param h1
     * @param f
     * @param h2
     */
    void addheapPointsTo(Instance h1, FieldKey f, Instance h2) {
        Pair<Instance, FieldKey> key = Pair.make(h1, f);
        if (heapPointsTo.containsKey(key)) {
            heapPointsTo.get(key).add(h2);
        } else {
            throw new IllegalArgumentException("Heap object doesn't exist");
        }
    }
}
