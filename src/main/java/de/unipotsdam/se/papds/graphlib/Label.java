package de.unipotsdam.se.papds.graphlib;

import java.util.Objects;

/*
    Placeholder class for label
 */
public class Label {

    public lbl label;
    public String context;

    public Label(lbl label) {
        this.label = label;
        this.context = ""; //Empty context for the NEW and ASSIGN edges
    }

    public Label(lbl label, String context) {
        switch (label) {
            case NEW:
            case ASSIGN:
                throw new IllegalArgumentException("Contexts cannot be applied to NEW and ASSIGN EDGES");
            case CALL:
            case RETURN:
            case GETFIELD:
            case PUTFIELD:
            case STATICGETFIELD:
            case STATICPUTFIELD:
                this.label = label;
                this.context = context; //Field Context
                break;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label1 = (Label) o;
        return label == label1.label;
    }

    @Override
    public int hashCode() {

        return Objects.hash(label);
    }

    @Override
    public String toString() {
        return "Label{" +
                "label=" + label + "," + context +
                '}';
    }
}
