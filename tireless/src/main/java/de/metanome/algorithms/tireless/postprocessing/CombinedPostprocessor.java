package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;

public class CombinedPostprocessor {

    public CombinedPostprocessor (RegularExpressionConjunction expression, Alphabet alphabet,
                                  AlgorithmConfiguration configuration) {
        new GeneralizeCharClasses(expression, alphabet, configuration).generalizeCharacterClasses();
        new CombineSimilarCharClasses(expression).combineClasses();
    }
}
