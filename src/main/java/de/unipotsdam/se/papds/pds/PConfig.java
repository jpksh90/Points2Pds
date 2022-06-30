package de.unipotsdam.se.papds.pds;

public class PConfig {

    private Location l;
    private StackSymbol[] st;

    public PConfig(Location l, StackSymbol[] st) {
        if (st.length > 2) {
            throw new IllegalArgumentException("Stack size greater than length 2 is not allowed");
        }
        this.l = l;
        this.st = st;
    }


    public Location getL() {
        return l;
    }

    public StackSymbol[] getSt() {
        return st;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PConfig pConfig = (PConfig) o;

        if (l != null ? !l.equals(pConfig.l) : pConfig.l != null) return false;
        return st != null ? st.equals(pConfig.st) : pConfig.st == null;
    }

    @Override
    public int hashCode() {
        int result = l != null ? l.hashCode() : 0;
        result = 31 * result + (st != null ? st.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PConfig{" +
                "l=" + l +
                ", st=" + st +
                '}';
    }
}
