package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;

public class GeneralizeQuantifiers {

    private final AlgorithmConfiguration configuration;

    public GeneralizeQuantifiers(AlgorithmConfiguration configuration) {
        this.configuration = configuration;
    }

    public String getQuantifier(RegularExpression expression) {
        int threshold = configuration.QUANTIFIER_GENERALIZATION_THRESHOLD;
        int min = expression.getMinCount();
        int max = expression.getMaxCount();
        if (min == max) return min == 1 ? "" : String.format("{%d}", min);
        else if (min == 0 && max == 1) return "?";
        else if (max - min < threshold) return String.format("{%d,%d}", min, max);
        else if (min > threshold) return String.format("{%d,}", min);
        else if (min > 0) return "+";
        else return "*";
    }
}
