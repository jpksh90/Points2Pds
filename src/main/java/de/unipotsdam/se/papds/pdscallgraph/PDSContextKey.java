package de.unipotsdam.se.papds.pdscallgraph;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.ContextKey;

public class PDSContextKey implements ContextKey {

    private IMethod method;
    private int lineno;

    public PDSContextKey(IMethod method, int lineno) {
        this.method = method;
        this.lineno = lineno;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PDSContextKey that = (PDSContextKey) o;

        if (lineno != that.lineno) return false;
        return method != null ? method.equals(that.method) : that.method == null;
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + lineno;
        return result;
    }

    @Override
    public String toString() {
        return "PDSContextKey{" +
                "method=" + method +
                ", lineno=" + lineno +
                '}';
    }
}
