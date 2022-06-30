/*
 * Copyright (c) 2017.  Jyoti Prakash
 *
 */

package general;

import de.unipotsdam.se.papds.pointerstructs.Instance;
import de.unipotsdam.se.papds.pointerstructs.VariableKey;

import java.util.Set;

public class PointsToSet {

    private VariableKey variableKey;
    private Set<Instance> instances;

    public PointsToSet(VariableKey variableKey, Set<Instance> instances) {
        this.variableKey = variableKey;
        this.instances = instances;
    }

    public void addInstance(Instance o) {
        instances.add(o);
    }
}
