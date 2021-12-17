package de.metanome.algorithms.tireless.postprocessing;

import de.metanome.algorithms.tireless.preprocessing.AlgorithmConfiguration;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpression;
import de.metanome.algorithms.tireless.regularexpression.containerclasses.RegularExpressionCharacterClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GeneralizeQuantifiersTest {

    @Test
    public void testNoQuantifier(){
        GeneralizeQuantifiers generalize = new GeneralizeQuantifiers(getDummyConfiguration());
        assertEquals("", generalize.getQuantifier(getExpression(1,1)));
    }

    @Test
    public void testConstantQuantifier(){
        GeneralizeQuantifiers generalize = new GeneralizeQuantifiers(getDummyConfiguration());
        assertEquals("{99}", generalize.getQuantifier(getExpression(99,99)));
    }

    @Test
    public void testRangeQuantifier(){
        GeneralizeQuantifiers generalize = new GeneralizeQuantifiers(getDummyConfiguration());
        assertEquals("{20,24}", generalize.getQuantifier(getExpression(20,24)));
    }

    @Test
    public void testNoMaximumQuantifier(){
        GeneralizeQuantifiers generalize = new GeneralizeQuantifiers(getDummyConfiguration());
        assertEquals("{20,}", generalize.getQuantifier(getExpression(20,99)));
    }

    @Test
    public void testPlusQuantifier(){
        GeneralizeQuantifiers generalize = new GeneralizeQuantifiers(getDummyConfiguration());
        assertEquals("+", generalize.getQuantifier(getExpression(2,99)));
    }

    @Test
    public void testStarQuantifier(){
        GeneralizeQuantifiers generalize = new GeneralizeQuantifiers(getDummyConfiguration());
        assertEquals("*", generalize.getQuantifier(getExpression(0,99)));
    }

    private RegularExpression getExpression(int min, int max) {
        return new RegularExpressionCharacterClass() {{
            setMinCount(min);
            setMaxCount(max);
        }};
    }

    private AlgorithmConfiguration getDummyConfiguration() {
        return new AlgorithmConfiguration(0,0,0,
                0,0,5, 0);
    }
}
