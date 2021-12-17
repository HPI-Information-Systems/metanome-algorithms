package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.regularexpression.containerclasses.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class CombineSimilarCharClasses {

    private final RegularExpressionConjunction expression;

    public CombineSimilarCharClasses(RegularExpressionConjunction expression) {
        this.expression = expression;
    }

    public void combineClasses() {
        mergeIdenticalClasses();
        if (expression.getLength() >= 2) {
            ArrayList<Boolean> left = isMergeable(true);
            ArrayList<Boolean> right = isMergeable(false);
            merge(left, right, true, true);
            merge(left, right, true, false);
            merge(left, right, false, false);
        }
        mergeIdenticalClasses();
    }

    protected void mergeIdenticalClasses() {
        for (int i = 1; i < expression.getLength(); i++) {
            if (expression.getChild(i).getExpressionType() == ExpressionType.CHARACTER_CLASS
                    && expression.getChild(i - 1).getExpressionType() == ExpressionType.CHARACTER_CLASS
                    && expression.getChild(i).getRepresentation()
                    .equals(expression.getChild(i - 1).getRepresentation())) {

                expression.getChild(i - 1).setMinCount(expression.getChild(i - 1).getMinCount() +
                        expression.getChild(i).getMinCount());
                expression.getChild(i - 1).setMaxCount(expression.getChild(i - 1).getMaxCount() +
                        expression.getChild(i).getMaxCount());
                expression.getChildren().remove(i--);

            } else if (expression.getChild(i).getExpressionType() == ExpressionType.TOKEN
                    && expression.getChild(i - 1).getExpressionType() == ExpressionType.TOKEN
                    && expression.getChild(i).getMinCount()
                    == expression.getChild(i - 1).getMinCount()
                    && expression.getChild(i).getMinCount() <= 1
                    && expression.getChild(i).getMaxCount() == 1
                    && expression.getChild(i - 1).getMaxCount() == 1) {

                RegularExpressionToken removeToken = (RegularExpressionToken) expression.getChildren().remove(i--);
                RegularExpressionToken token = (RegularExpressionToken) expression.getChild(i);
                token.setToken(token.getToken() + removeToken.getToken());
            }
        }
    }

    protected ArrayList<Boolean> isMergeable(boolean left) {
        ArrayList<Boolean> result = new ArrayList<>() {{
            for (int i = 0; i < expression.getLength(); i++)
                add(false);
        }};
        if (left)
            for (int i = 1; i < expression.getLength(); i++)
                checkPosition(result, i, true);
        else
            for (int i = expression.getLength() - 2; i >= 0; i--)
                checkPosition(result, i, false);
        return result;
    }

    private void checkPosition(ArrayList<Boolean> result, int i, boolean left) {
        RegularExpression child = expression.getChild(i);
        if (child.getMinCount() > 0)
            result.set(i, false);
        else {
            int j = i;
            while ((j > 0 || !left) && (j < expression.getLength() - 1 || left) && (result.get(j) || j == i)) {
                j += left ? -1 : 1;
                result.set(i, checkMerge(child, expression.getChild(j)));
                if (result.get(i)) break;
            }
        }
    }

    private boolean checkMerge(RegularExpression candidate, RegularExpression superClass) {
        BitSet copy = (BitSet) candidate.getRepresentation().clone();
        copy.and(superClass.getRepresentation());
        return copy.cardinality() > candidate.getRepresentation().cardinality() / 2;
    }

    protected void merge(ArrayList<Boolean> left, ArrayList<Boolean> right,
                         boolean startRight, boolean onlyDoubleTrues) {
        int i = startRight ? expression.getLength() - 1 : 0;
        AtomicInteger startIndex = new AtomicInteger(i);
        AtomicBoolean isMergeable = new AtomicBoolean(false);
        while (i >= 0 && i < expression.getLength()) {
            if (startRight)
                i = processPosition(left, right, true, onlyDoubleTrues, i, startIndex, isMergeable);
            else
                i = processPosition(right, left, false, onlyDoubleTrues, i, startIndex, isMergeable);
        }
    }

    private int processPosition(ArrayList<Boolean> left, ArrayList<Boolean> right, boolean startRight,
                                boolean onlyDoubleTrues, int i, AtomicInteger startIndex, AtomicBoolean isMergeable) {
        if (!left.get(i) || (onlyDoubleTrues && !right.get(i)))
            if (isMergeable.get()) {
                isMergeable.set(false);
                if (startRight) {
                    mergeRange(i, startIndex.get(), left, right);
                    i++;
                    startIndex.set(onlyDoubleTrues ? i : i - 1);
                } else {
                    mergeRange(startIndex.get(), i, right, left);
                    i -= (i - startIndex.get()) + 1;
                    startIndex.set(onlyDoubleTrues ? i : i + 1);
                }
            } else startIndex.set(onlyDoubleTrues ? i : startRight ? i - 1 : i + 1);
        else if (onlyDoubleTrues || Math.abs(startIndex.get() - i) >= 1) isMergeable.set(true);
        return startRight ? i - 1 : i + 1;
    }

    private void mergeRange(int first, int last, ArrayList<Boolean> left, ArrayList<Boolean> right) {
        BitSet representation = new BitSet();
        int minCount = 0;
        int maxCount = 0;
        for (int i = first; i <= last; i++) {
            RegularExpression child = expression.getChildren().remove(first);
            if (i != first) left.remove(first + 1);
            if (i != last) right.remove(first);
            representation.or(child.getRepresentation());
            minCount += child.getMinCount();
            maxCount += child.getMaxCount();
        }
        appendNewChild(first, left, right, representation, minCount, maxCount);
    }

    private void appendNewChild(int first, ArrayList<Boolean> left, ArrayList<Boolean> right, BitSet representation,
                                int minCount, int maxCount) {
        RegularExpressionCharacterClass newChild = new RegularExpressionCharacterClass(representation);
        newChild.setMinCount(minCount);
        newChild.setMaxCount(maxCount);
        expression.addChild(newChild, first);
    }
}
