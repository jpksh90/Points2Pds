package de.unipotsdam.se.papds.graphlib;

public enum lbl {
    CALL,
    RETURN,
    ASSIGN,
    NEW,
    GETFIELD,
    PUTFIELD,
    STATICGETFIELD,
    STATICPUTFIELD,
    DUMMY, //Introduced for the sake of type safety of Optional type in edge weight by guava library
    //TODO: Field
}
