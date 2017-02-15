package de.hpi.mpss2015n.approxind;

import de.hpi.mpss2015n.approxind.inclusiontester.HLLInclusionTester;
import de.hpi.mpss2015n.approxind.mocks.RelationalInputBuilder;
import de.hpi.mpss2015n.approxind.sampler.IdentityRowSampler;
import de.hpi.mpss2015n.approxind.utils.Arity;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import de.hpi.mpss2015n.approxind.utils.SimpleInd;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.backend.result_receiver.ResultCache;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

//@RunWith(Parameterized.class)
public class FAIDACoreTest {

    private static final int TABLE = 0;
    FAIDACore algo;
    private static String level;

    private InclusionTester t = new HLLInclusionTester(0.001);
    private int sampleGoal = 2;
    //private InclusionTester t = new BloomFilterInclusionTester();

    // data() Parameters map to these constructor parameter
//    public FAIDACoreTest(InclusionTester t) {
//        this.t = t;
//    }
//
//
//    @Parameterized.Parameters(name = "{index}: inclusionTester: {0}")
//    public static Iterable<Object> data() {
//        return Arrays.asList(new Object[][]{
//                {new HashSetInclusionTester()},
//                {new HLLInclusionTester(0.01)},
//                //  {new BloomFilterInclusionTester()}
//        });
//    }

    @Ignore
    @Test
    public void testExecuteSingleLine() throws Exception {
        algo = new FAIDACore(Arity.UNARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("testTable")
                .setHeader("c0", "c1", "c2")
                .addRow("b", "b", "3")
                .build();

        ResultCache resultCache = new ResultCache("FAIDA", Collections.emptyList());
        List<SimpleInd> inds = algo.execute(inputs, resultCache);

        assertThat(inds, containsInAnyOrder(
                new SimpleInd(SimpleColumnCombination.create(0, 1), SimpleColumnCombination.create(0, 0)),
                new SimpleInd(SimpleColumnCombination.create(0, 0), SimpleColumnCombination.create(0, 1))));
    }

    @Ignore
    @Test
    public void testExecute() throws Exception {
        algo = new FAIDACore(Arity.UNARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("testTable")
                .setHeader("c0", "c1", "c2")
                .addRow("a", "b", "1")
                .addRow("b", "b", "3")
                .build();

        ResultCache resultCache = new ResultCache("FAIDA", Collections.emptyList());
        List<SimpleInd> inds = algo.execute(inputs, resultCache);

        assertThat(inds, contains(new SimpleInd(SimpleColumnCombination.create(0, 1),
                SimpleColumnCombination.create(0, 0))));
    }

    @Test
    public void testExecuteBinary() throws Exception {
        algo = new FAIDACore(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("testTable")
                .setHeader("c0", "c1", "c2", "c3")
                .addRow("a", "1", "b", "2")
                .addRow("b", "2", "b", "2")
                .addRow("c", "3", "a", "1")
                .build();

        ResultCache resultCache = new ResultCache("FAIDA", Collections.emptyList());
        List<SimpleInd> inds = algo.execute(inputs, resultCache);

        assertThat(inds, contains(
                SimpleInd.left(TABLE, 2).right(TABLE, 0),
                SimpleInd.left(TABLE, 3).right(TABLE, 1),
                SimpleInd.left(TABLE, 2, 3).right(TABLE, 0, 1)

        ));
    }

    @Test
    public void testExecuteTrinary() throws Exception {
        algo = new FAIDACore(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("testTable")
                .setHeader("c0", "c1", "c2", "c3", "c4", "c5")
                .addRow("a", "1", "x", "b", "2", "y")
                .addRow("b", "2", "y", "b", "2", "y")
                .addRow("c", "3", "y", "a", "1", "x")
                .build();

        ResultCache resultCache = new ResultCache("FAIDA", Collections.emptyList());
        List<SimpleInd> inds = algo.execute(inputs, resultCache);

        assertThat(inds, hasItem(new SimpleInd(
                SimpleColumnCombination.create(TABLE, 3, 4, 5),
                SimpleColumnCombination.create(TABLE, 0, 1, 2))

        ));
    }


    @Test
    public void testExecuteWdcPlanets() throws Exception {
        algo = new FAIDACore(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("planets")
                .setHeader("Domicile", "Detriment", "Exaltation", "Fall")
                .addRow("Mars", "Venus", "Sun", "Saturn")
                .addRow("Venus", "Pluto", "Moon", "Uranus")
                .addRow("Mercury", "Jupiter", "N/A", "N/A")
                .addRow("Moon", "Saturn", "Jupiter", "Mars")
                .addRow("Sun", "Uranus", "Neptune", "Mercury")
                .addRow("Mercury", "Neptune", "Pluto, Mercury", "Venus")
                .addRow("Venus", "Mars", "Saturn", "Sun")
                .addRow("Pluto", "Venus", "Uranus", "Moon")
                .addRow("Jupiter", "Mercury", "N/A", "N/A")
                .addRow("Saturn", "Moon", "Mars", "Jupiter")
                .addRow("Uranus", "Sun", "Mercury", "Neptune")
                .addRow("Neptune", "Mercury", "Venus", "Pluto, Mercury")
                .build();


        ResultCache resultCache = new ResultCache("FAIDA", Collections.emptyList());
        List<SimpleInd> inds = algo.execute(inputs, resultCache);

        assertThat(inds, containsInAnyOrder(
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 0),
                        SimpleColumnCombination.create(TABLE, 1)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 1),
                        SimpleColumnCombination.create(TABLE, 0)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 2),
                        SimpleColumnCombination.create(TABLE, 3)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 3),
                        SimpleColumnCombination.create(TABLE, 2)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 1, 3),
                        SimpleColumnCombination.create(TABLE, 0, 2)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 0, 2),
                        SimpleColumnCombination.create(TABLE, 1, 3)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 0, 3),
                        SimpleColumnCombination.create(TABLE, 1, 2)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 1, 2),
                        SimpleColumnCombination.create(TABLE, 0, 3))

        ));
    }

    @Test
    public void testExecuteWdcPlanets2Col() throws Exception {
        algo = new FAIDACore(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("planets_2col")
                .setHeader("Exaltation", "Fall")
                .addRow("Sun", "Saturn")
                .addRow("Moon", "Uranus")
                .addRow("N/A", "N/A")
                .addRow("Jupiter", "Mars")
                .addRow("Neptune", "Mercury")
                .addRow("Pluto, Mercury", "Venus")
                .addRow("Saturn", "Sun")
                .addRow("Uranus", "Moon")
                .addRow("N/A", "N/A")
                .addRow("Mars", "Jupiter")
                .addRow("Mercury", "Neptune")
                .addRow("Venus", "Pluto, Mercury").build();

        ResultCache resultCache = new ResultCache("FAIDA", Collections.emptyList());
        List<SimpleInd> inds = algo.execute(inputs, resultCache);

        assertThat(inds, containsInAnyOrder(
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 0),
                        SimpleColumnCombination.create(TABLE, 1)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 1),
                        SimpleColumnCombination.create(TABLE, 0))
        ));
    }


    @Test
    public void testExecuteWdcPlanetsSmall() throws Exception {
        algo = new FAIDACore(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("planets_4row")
                .setHeader("Domicile", "Detriment", "Exaltation", "Fall")
                .addRow("Mars", "Venus", "Sun", "Moon")
                .addRow("Venus", "Mars", "Moon", "Sun")
                .addRow("Jupiter", "Mercury", "Pluto", "N/A")
                .addRow("Mercury", "Mercury", "N/A", "N/A")
                .build();


        ResultCache resultCache = new ResultCache("FAIDA", Collections.emptyList());
        List<SimpleInd> inds = algo.execute(inputs, resultCache);

        assertThat(inds, containsInAnyOrder(
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 1),
                        SimpleColumnCombination.create(TABLE, 0)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 3),
                        SimpleColumnCombination.create(TABLE, 2)),
                new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 1, 3),
                        SimpleColumnCombination.create(TABLE, 0, 2))
        ));
    }


}