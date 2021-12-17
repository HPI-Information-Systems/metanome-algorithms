package de.metanome.algorithms.tireless.preprocessing.alphabet;

import de.metanome.algorithms.tireless.preprocessing.CharClasses;

import java.util.BitSet;

public class DefaultAlphabet {

    public static AlphabetNode getDefaultAlphabet(BitSet excludedSpecials, CharClasses charClasses) {
        AlphabetNode alphabet = new AlphabetNode(".") {{
            addSubclass(new AlphabetNode("[:alnum:]") {{
                addSubclass(new AlphabetLeaf(charClasses.getDigitClass(), "[:digit:]"));
                addSubclass(new AlphabetNode("[:alpha:]") {{
                    addSubclass(new AlphabetLeaf(charClasses.getLowerCaseClass(), "[:lower:]"));
                    addSubclass(new AlphabetLeaf(charClasses.getUpperCaseClass(), "[:upper:]"));
                    addSubclass(new AlphabetLeaf(charClasses.getOtherLetters(), ""));
                }});
            }});
        }};
        addSpecialChars(alphabet, excludedSpecials, charClasses);
        alphabet.resetLevel(0);
        return alphabet;
    }

    private static void addSpecialChars(AlphabetNode alphabet, BitSet excluded, CharClasses charClasses) {
        BitSet specialChars = charClasses.getSpecialCharClass();
        for (int i = specialChars.nextSetBit(0); i >= 0; i = specialChars.nextSetBit(i + 1)) {
            if (excluded.get(i)) continue;
            final int finalI = i;
            alphabet.addSubclass(new AlphabetLeaf(new BitSet() {{
                set(finalI);
            }}, ""));
        }
    }
}
