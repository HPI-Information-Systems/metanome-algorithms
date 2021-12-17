package de.metanome.algorithms.tireless.preprocessing;

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

public class CharClasses {

    private final BitSet lowerCaseLetters = new BitSet();
    private final BitSet upperCaseLetters = new BitSet();
    private final BitSet otherLetters = new BitSet();
    private final BitSet specialCharacters = new BitSet();
    private final BitSet digits = new BitSet();

    public CharClasses(Collection<String> values) {
        computeCharClasses(values);
    }

    private void computeCharClasses(Collection<String> values) {

        for (String value : values) {
            if (value == null) continue;
            for (int i : value.toCharArray()) {
                processChar(i);
            }
        }

        postprocessClasses();
    }

    private void processChar(int i) {
        if (!Character.isDefined(i)) return;
        specialCharacters.set(i);
        if (Character.getType(i) >= 9 && Character.getType(i) <= 11) digits.set(i);
        if (Character.isLowerCase(i)) lowerCaseLetters.set(i);
        if (Character.isUpperCase(i)) upperCaseLetters.set(i);
        if (Character.isLetter(i)) otherLetters.set(i);
    }

    private void postprocessClasses() {
        specialCharacters.andNot(otherLetters);
        specialCharacters.andNot(digits);

        otherLetters.andNot(lowerCaseLetters);
        otherLetters.andNot(upperCaseLetters);
        lowerCaseLetters.or(new BitSet() {{set('a', 'z' + 1);}});
        upperCaseLetters.or(new BitSet() {{set('A', 'Z' + 1);}});
        digits.or(new BitSet() {{set('0', '9' + 1);}});
    }

    public BitSet getDigitClass() {
        return digits;
    }

    public BitSet getLowerCaseClass() {
        return lowerCaseLetters;
    }

    public BitSet getUpperCaseClass() {
        return upperCaseLetters;
    }

    public BitSet getOtherLetters() {
        return otherLetters;
    }

    public BitSet getSpecialCharClass() {
        return specialCharacters;
    }
}
