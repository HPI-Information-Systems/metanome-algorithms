package de.metanome.algorithms.tireless.algorithm;

import de.metanome.algorithms.tireless.preprocessing.alphabet.Alphabet;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.ExpressionType;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionConjunction;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionDisjunctionOfTokens;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class OutlierDetection {

    RegularExpressionDisjunctionOfTokens preliminaryExpression;
    RegularExpressionConjunction updatedExpression;
    int minimalOccurrenceThreshold;

    public OutlierDetection(RegularExpressionDisjunctionOfTokens preliminaryExpression,
                            RegularExpressionConjunction updatedExpression,
                            int minimalOccurrenceThreshold) {
        this.preliminaryExpression = preliminaryExpression;
        this.updatedExpression = updatedExpression;
        this.minimalOccurrenceThreshold = minimalOccurrenceThreshold;
    }

    public void detectAndRemoveOutliers() {
        removeOutlierComponents();
        removeOutlierCharClasses();
    }

    private void removeOutlierComponents() {
        for (int i = 0; i < updatedExpression.getLength(); i++) {
            RegularExpression child = updatedExpression.getChild(i);
            if ((child.getExpressionType() == ExpressionType.DISJUNCTION_OF_TOKENS
                    || child.getExpressionType() == ExpressionType.TOKEN)
                    && child.getMinCount() == 0) {
                if (updatedExpression.getChild(i).getAppearanceCount() <= minimalOccurrenceThreshold)
                    updatedExpression.getChildren().remove(i--);
                else if (preliminaryExpression.getMinCount() == 1 &&
                        updatedExpression.getChild(i).getAppearanceCount()
                                >= preliminaryExpression.getAppearanceCount() - minimalOccurrenceThreshold)
                    updatedExpression.getChild(i).setMinCount(1);
            }
        }
    }

    private void removeOutlierCharClasses() {
        for (int i = 0; i < updatedExpression.getLength(); i++) {
            if (updatedExpression.getChild(i).getExpressionType() != ExpressionType.DISJUNCTION_OF_TOKENS) continue;
            RegularExpressionDisjunctionOfTokens child =
                    (RegularExpressionDisjunctionOfTokens) updatedExpression.getChild(i);
            Set<String> removeCollector = new HashSet<>();
            checkForSpecialCharacters(child, removeCollector);
            checkForAlphabets(child, removeCollector);
            for (String token : removeCollector) {
                int count = child.getChildren().remove(token);
                child.setAppearanceCount(child.getAppearanceCount() - count);
            }
            if(child.getLength() == 0)
                updatedExpression.getChildren().remove(i--);
        }
    }

    private void checkForAlphabets(RegularExpressionDisjunctionOfTokens child, Set<String> removeCollector) {
        for (Alphabet alphabet : child.getMainAlphabets().keySet()) {
            if (alphabet != null && child.getMainAlphabets().get(alphabet) <= minimalOccurrenceThreshold) {
                BitSet representation = alphabet.getRepresentingBitset();
                for (String token : child.getChildren().keySet())
                    for (Character character : token.toCharArray()) {
                        if (child.getNonSpecialCharacters().containsKey(character)) continue;
                        if (representation.get((int) character)) removeCollector.add(token);
                        break;
                    }
                child.removeFromRepresentation(representation);
            }
        }
    }

    private void checkForSpecialCharacters(RegularExpressionDisjunctionOfTokens child, Set<String> removeCollector) {
        for (Character character : child.getNonSpecialCharacters().keySet()) {
            if (child.getNonSpecialCharacters().get(character) <= minimalOccurrenceThreshold) {
                for (String token : child.getChildren().keySet()) {
                    if (token.indexOf(character) >= 0) {
                        removeCollector.add(token);
                    }
                }
                child.removeFromRepresentation(character);
            }
        }
    }
}
