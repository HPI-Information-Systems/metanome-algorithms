package de.hpi.mpss2015n.approxind;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import de.hpi.mpss2015n.approxind.mocks.RelationalInputBuilder;
import de.hpi.mpss2015n.approxind.sampler.IdentityRowSampler;
import de.hpi.mpss2015n.approxind.utils.Arity;
import de.hpi.mpss2015n.approxind.utils.SimpleColumnCombination;
import de.hpi.mpss2015n.approxind.utils.SimpleInd;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;

//@RunWith(Parameterized.class)
public class FAIDATest {

    private static final int TABLE = 0;
    FAIDA algo;
    private static String level;

    private InclusionTester t = new HLLInclusionTester(0.001);
    private int sampleGoal = 2;
    //private InclusionTester t = new BloomFilterInclusionTester();

    // data() Parameters map to these constructor parameter
//    public FAIDATest(InclusionTester t) {
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
        algo = new FAIDA(Arity.UNARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("testTable")
                .setHeader("c0", "c1", "c2")
                .addRow("b", "b", "3")
                .build();

        List<SimpleInd> inds = algo.executeInternal(inputs);

        assertThat(inds, containsInAnyOrder(
                new SimpleInd(SimpleColumnCombination.create(0, 1), SimpleColumnCombination.create(0, 0)),
                new SimpleInd(SimpleColumnCombination.create(0, 0), SimpleColumnCombination.create(0, 1))));
    }

    @Ignore
    @Test
    public void testExecute() throws Exception {
        algo = new FAIDA(Arity.UNARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("testTable")
                .setHeader("c0", "c1", "c2")
                .addRow("a", "b", "1")
                .addRow("b", "b", "3")
                .build();

        List<SimpleInd> inds = algo.executeInternal(inputs);

        assertThat(inds, contains(new SimpleInd(SimpleColumnCombination.create(0, 1),
                SimpleColumnCombination.create(0, 0))));
    }

    @Test
    public void testExecuteBinary() throws Exception {
        algo = new FAIDA(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("testTable")
                .setHeader("c0", "c1", "c2", "c3")
                .addRow("a", "1", "b", "2")
                .addRow("b", "2", "b", "2")
                .addRow("c", "3", "a", "1")
                .build();

        List<SimpleInd> inds = algo.executeInternal(inputs);

        assertThat(inds, contains(
                SimpleInd.left(TABLE, 2).right(TABLE, 0),
                SimpleInd.left(TABLE, 3).right(TABLE, 1),
                SimpleInd.left(TABLE, 2, 3).right(TABLE, 0, 1)

        ));
    }

    @Test
    public void testExecuteTrinary() throws Exception {
        algo = new FAIDA(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("testTable")
                .setHeader("c0", "c1", "c2", "c3", "c4", "c5")
                .addRow("a", "1", "x", "b", "2", "y")
                .addRow("b", "2", "y", "b", "2", "y")
                .addRow("c", "3", "y", "a", "1", "x")
                .build();

        List<SimpleInd> inds = algo.executeInternal(inputs);

        assertThat(inds, hasItem(new SimpleInd(
                        SimpleColumnCombination.create(TABLE, 3, 4, 5),
                        SimpleColumnCombination.create(TABLE, 0, 1, 2))

        ));
    }


    @Test
    public void testExecuteWdcPlanets() throws Exception {
        algo = new FAIDA(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("planets")
                .setHeader("Domicile", "Detriment", "Exaltation",   "Fall")
                .addRow("Mars",	    "Venus",	"Sun",	            "Saturn")
                .addRow("Venus",	"Pluto",	"Moon",             "Uranus")
                .addRow("Mercury",	"Jupiter",	"N/A",              "N/A")
                .addRow("Moon",	    "Saturn",	"Jupiter",         	"Mars")
                .addRow("Sun",	    "Uranus",	"Neptune",	        "Mercury")
                .addRow("Mercury",	"Neptune",	"Pluto, Mercury",	"Venus")
                .addRow("Venus",    "Mars",     "Saturn",           "Sun")
                .addRow("Pluto",	"Venus",	"Uranus",	        "Moon")
                .addRow("Jupiter",  "Mercury",   "N/A",             "N/A")
                .addRow("Saturn",   "Moon",     "Mars",             "Jupiter")
                .addRow("Uranus",	"Sun",	    "Mercury",	        "Neptune")
                .addRow("Neptune",	"Mercury",	"Venus",	        "Pluto, Mercury")
                .build();


        List<SimpleInd> inds = algo.executeInternal(inputs);

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
        algo = new FAIDA(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("planets_2col")
                .setHeader("Exaltation",   "Fall")
                .addRow("Sun",	            "Saturn")
                .addRow("Moon",             "Uranus")
                .addRow("N/A",              "N/A")
                .addRow("Jupiter",         	"Mars")
                .addRow("Neptune",	        "Mercury")
                .addRow("Pluto, Mercury",	"Venus")
                .addRow("Saturn",           "Sun")
                .addRow("Uranus",	        "Moon")
                .addRow("N/A",             "N/A")
                .addRow("Mars",	            "Jupiter")
                .addRow("Mercury",	        "Neptune")
                .addRow("Venus", "Pluto, Mercury").build();

        List<SimpleInd> inds = algo.executeInternal(inputs);

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
        algo = new FAIDA(Arity.N_ARY, new IdentityRowSampler(), t, sampleGoal);
        RelationalInputGenerator[] inputs = new RelationalInputGenerator[1];
        inputs[0] = new RelationalInputBuilder("planets_4row")
                .setHeader("Domicile", "Detriment", "Exaltation",   "Fall")
                .addRow("Mars",     "Venus",    "Sun",  "Moon")
                .addRow("Venus",	"Mars",	    "Moon", "Sun")
                .addRow("Jupiter",  "Mercury",  "Pluto",  "N/A")
                .addRow("Mercury",  "Mercury",  "N/A",  "N/A")
                .build();


        List<SimpleInd> inds = algo.executeInternal(inputs);

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