package de.hpi.mpss2015n.approxind.utils;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class CandidateGeneratorTest {

    private static final int TABLE = 0;
    CandidateGenerator gen = new CandidateGenerator();

    @Test
    public void testCreateCombinedCandidatesBinary() throws Exception {
        List<SimpleInd> candidates = gen.createCombinedCandidates(Arrays.asList(
                SimpleInd.left(TABLE, 0).right(TABLE, 3),
                SimpleInd.left(TABLE, 1).right(TABLE, 4)
        ));

        assertThat(candidates, contains(SimpleInd.left(TABLE, 0, 1).right(TABLE, 3, 4)));

    }

    @Test
    public void testCreateCombinedCandidatesTrinary() throws Exception {
        List<SimpleInd> inds = Arrays.asList(
                SimpleInd.left(TABLE, 0, 1).right(TABLE, 3, 4),
                SimpleInd.left(TABLE, 1, 2).right(TABLE, 4, 5),
                SimpleInd.left(TABLE, 0, 2).right(TABLE, 3, 5)
        );
        List<SimpleInd> candidates = gen.createCombinedCandidates(inds);

        assertThat(candidates, contains(SimpleInd.left(TABLE, 0, 1, 2).right(TABLE, 3, 4, 5)));
    }

}