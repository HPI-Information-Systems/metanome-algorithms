package de.metanome.algorithms.tireless.regularexpression;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;

import java.util.Comparator;

public class RegularExpressionComparator implements Comparator<RegularExpression> {
    @Override
    public int compare(RegularExpression o1, RegularExpression o2) {
        return Integer.compare(o1.getLength(), o2.getLength());
    }
}
