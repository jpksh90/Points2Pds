package de.unipotsdam.se.papds.pds;

public class StackSymbol {

    private String stacksymbol;

    //Create a  Stack Symbol for the
    public StackSymbol(String stacksymbol) {
        this.stacksymbol = stacksymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StackSymbol that = (StackSymbol) o;

        return stacksymbol != null ? stacksymbol.equals(that.stacksymbol) : that.stacksymbol == null;
    }

    @Override
    public int hashCode() {
        return stacksymbol != null ? stacksymbol.hashCode() : 0;
    }

    @Override
    public String toString() {
        return stacksymbol;
    }
}
