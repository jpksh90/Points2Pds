package de.unipotsdam.se.papds.pointerstructs;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.collections.Pair;

import java.util.Iterator;

public class Instance implements InstanceKey {

    int lineno;
    private TypeName typeName;

    public Instance(TypeName typeName, int lineno) {
        this.typeName = typeName;
        this.lineno = lineno;
    }


    @Override
    public IClass getConcreteType() {
        return null;
    }

    @Override
    public Iterator<Pair<CGNode, NewSiteReference>> getCreationSites(CallGraph CG) {
        return null;
    }
}
