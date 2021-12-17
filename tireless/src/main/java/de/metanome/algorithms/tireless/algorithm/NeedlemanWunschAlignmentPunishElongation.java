package de.metanome.algorithms.tireless.algorithm;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;

public class NeedlemanWunschAlignmentPunishElongation extends NeedlemanWunschAlignment {

    public NeedlemanWunschAlignmentPunishElongation(RegularExpressionConjunction left,
                                                    RegularExpressionConjunction right) {
        super(left, right);
        if (left.getLength() > right.getLength()) {
            this.left = right;
            this.right = left;
        }
    }

    @Override
    public RegularExpressionConjunction mergeExpressions() {
        setGapCostRight(-3);
        if (left.getLength() == right.getLength())
            setGapCostLeft(-3);
        return super.mergeExpressions();
    }
}
