package de.unipotsdam.se.papds.analysis.demand;

import com.ibm.wala.classLoader.IMethod;

/**
 *  Class specicfies a query for each demand driven analysis
 */
public class Query {

    private IMethod method; //method m
    private int vn; //variable number

    public Query(IMethod m, int vn) {
        this.method = m;
        this.vn = vn;
    }

    public IMethod getMethod() {
        return method;
    }


    public int getVn() {
        return vn;
    }


    @Override
    public String toString() {
        return "Query{" +
                "method=" + method +
                ", vn=" + vn +
                '}';
    }
}
