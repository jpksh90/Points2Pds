package de.unipotsdam.se.papds.pds;

import java.util.Objects;

public class Rule {

    private PConfig lhs;
    private PConfig rhs;

    Rule(PConfig lhs, PConfig rhs) {
        if (lhs.getSt().length != 1) {
            throw new IllegalArgumentException("Left Hand P-Config stack size is always 1");
        }
        if (lhs.getSt().length == 0) {
            throw new IllegalArgumentException("Left side stack can't be empty");
        }
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public PConfig getLhs() {
        return lhs;
    }

    public PConfig getRhs() {
        return rhs;
    }

    public Location getRightLoc() {
        return rhs.getL();
    }

    public Location getLeftLoc() {
        return lhs.getL();
    }

    public StackSymbol[] getLeftStack() {
        return lhs.getSt();
    }

    public StackSymbol[] getRightStack() {
        return rhs.getSt();
    }

    public boolean rightStackEmpty() {
        return rhs.getSt().length == 0;
    }

    public Rule getRule() {
        return this;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "lhs=" + lhs.toString() +
                ", rhs=" + rhs.toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rule rule = (Rule) o;
        return Objects.equals(lhs, rule.lhs) &&
                Objects.equals(rhs, rule.rhs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(lhs, rhs);
    }
}
