package de.metanome.algorithms.tireless.algorithm;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;

public abstract class Alignment {
    protected RegularExpressionConjunction left;
    protected RegularExpressionConjunction right;

    public Alignment(RegularExpressionConjunction left, RegularExpressionConjunction right) {
        this.left = left;
        this.right = right;
    }

    public abstract RegularExpressionConjunction mergeExpressions();
}
