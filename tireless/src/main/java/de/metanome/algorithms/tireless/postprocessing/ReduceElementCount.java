package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionCharacterClass;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class ReduceElementCount {

    private final RegularExpressionConjunction expression;
    private final AlgorithmConfiguration configuration;
    private final Alphabet alphabet;

    public ReduceElementCount(RegularExpressionConjunction expression, AlgorithmConfiguration configuration,
                              Alphabet alphabet) {
        this.expression = expression;
        this.configuration = configuration;
        this.alphabet = alphabet;
    }

    public void mergeNeighbours() {
        List<Double> metrics = getMergeMetrics();
        while (expression.getElementCount(configuration, alphabet) > configuration.MAXIMUM_ELEMENT_COUNT
        && expression.getLength() > 1) {
            int mergePair = getMergePair(metrics);
            mergeExpressions(mergePair);
            metrics.remove(mergePair);
            int oldLength = expression.getLength();
            new CombinedPostprocessor(expression, alphabet, configuration);
            if(oldLength != expression.getLength())
                metrics = getMergeMetrics();
            else {
                if (mergePair > 0) metrics.set(mergePair - 1, getMetric(mergePair));
                if (mergePair < expression.getLength() - 1) metrics.set(mergePair, getMetric(mergePair + 1));
            }
        }
    }

    private void mergeExpressions(int mergePair) {
        BitSet newRepresentation = new BitSet();
        newRepresentation.or(expression.getChild(mergePair).getRepresentation());
        newRepresentation.or(expression.getChild(mergePair + 1).getRepresentation());
        RegularExpressionCharacterClass newChild = new RegularExpressionCharacterClass(newRepresentation);
        newChild.setMinCount(expression.getChild(mergePair).getRepresentationMinCount()
                + expression.getChild(mergePair + 1).getRepresentationMinCount());
        newChild.setMaxCount(expression.getChild(mergePair).getRepresentationMaxCount()
                + expression.getChild(mergePair + 1).getRepresentationMaxCount());
        expression.getChildren().remove(mergePair);
        expression.getChildren().remove(mergePair);
        expression.addChild(newChild, mergePair);
    }

    protected List<Double> getMergeMetrics() {
        List<Double> result = new ArrayList<>();
        for (int i = 1; i < expression.getLength(); i++) {
            result.add(getMetric(i));
        }
        return result;
    }

    private double getMetric(int i) {
        double metric = 1;
        metric *= expression.getChild(i).getMinCount() == 0 ? 1 : 0.5;
        metric *= expression.getChild(i - 1).getMinCount() == 0 ? 1 : 0.5;
        metric *= (expression.getChild(i).getElementCount(configuration, alphabet) +
                expression.getChild(i - 1).getElementCount(configuration, alphabet));
        metric *= getJaccardIndex(expression.getChild(i).getRepresentation(),
                expression.getChild(i - 1).getRepresentation());
        return metric;
    }

    private double getJaccardIndex(BitSet left, BitSet right) {
        BitSet intersection = (BitSet) left.clone();
        intersection.and(right);
        return Math.max((double) intersection.cardinality() / (left.cardinality() +
                right.cardinality() - intersection.cardinality()), 0.01);
    }

    private int getMergePair(List<Double> metrics) {
        new ArrayList<>();
        List<Integer> mergeCandidates =  getMergeCandidates(metrics);
        int middleIndex = 0;
        for(int value: mergeCandidates)
            if(Math.abs(metrics.size()/2 - value) < Math.abs(metrics.size()/2 - middleIndex))
                middleIndex = value;
        return middleIndex;
    }

    private List<Integer> getMergeCandidates(List<Double> metrics) {
        double maximumValue = 0;
        List<Integer> result = new ArrayList<>();
        for(int i = 0; i < metrics.size(); i++) {
            double value = metrics.get(i);
            if (value > maximumValue) {
                maximumValue = value;
                result.clear();
                result.add(i);
            } else if (value == maximumValue)
                result.add(i);
        }
        return result;
    }
}
