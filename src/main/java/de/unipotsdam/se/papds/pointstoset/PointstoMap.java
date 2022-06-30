package de.unipotsdam.se.papds.pointstoset;

import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.Pair;
import de.unipotsdam.se.papds.pointerstructs.Instance;
import de.unipotsdam.se.papds.pointerstructs.VariableKey;

import java.util.HashMap;
import java.util.Set;

public class PointstoMap {

    private HashMap<VariableKey, Set<Instance>> varPointsTo;
    private HashMap<Instance, Set<Pair<VariableKey, Set<Instance>>>> heapPointsTo;

    PointstoMap() {
        varPointsTo = HashMapFactory.make();
        heapPointsTo = HashMapFactory.make();
    }

}
