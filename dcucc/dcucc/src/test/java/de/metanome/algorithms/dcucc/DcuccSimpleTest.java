package de.metanome.algorithms.dcucc;

import de.metanome.algorithm_helper.data_structures.PLIBuilder;
import de.metanome.algorithm_helper.data_structures.PositionListIndex;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithms.test_helper.fixtures.AbaloneFixture;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Jens Ehrlich
 */
public class DcuccSimpleTest {

  Dcucc algorithm;

  @Before
  public void setUp() throws Exception {
    algorithm = new Dcucc();
    algorithm.conditionLatticeTraverser = new SimpleConditionTraverser(algorithm);
  }

  @Test
  public void pliHashmapTest()
          throws CouldNotReceiveResultException, UnsupportedEncodingException, FileNotFoundException,
          InputGenerationException, InputIterationException, AlgorithmConfigurationException {
    //Setup
    AbaloneFixture fixture = new AbaloneFixture();
    RelationalInput input = fixture.getInputGenerator().generateNewCopy();
    PLIBuilder builder = new PLIBuilder(input);
    List<PositionListIndex> pliList = builder.getPLIList();

    List<PositionListIndex> pliListCopy = new ArrayList<>();
    for (int i = 0; i < pliList.size(); i++) {
      pliListCopy.add(i, new PositionListIndex(pliList.get(i).getClusters()));
    }

    for (int i = 0; i < pliList.size(); i++) {
      PositionListIndex pli = pliList.get(i);
      PositionListIndex pliCopy = pliListCopy.get(i);

      Long2LongOpenHashMap pliHash = pli.asHashMap();
      Long2LongOpenHashMap pliCopyHash = pliCopy.asHashMap();

      assertEquals(pliHash, pliCopyHash);
      assertEquals(pliHash.keySet(), pliCopyHash.keySet());

      for (long row : pliHash.keySet()) {
        assertEquals(pliHash.get(row), pliCopyHash.get(row));
      }
    }
  }

  @Test
  public void pliHashmapTest2()
          throws CouldNotReceiveResultException, UnsupportedEncodingException, FileNotFoundException,
          InputGenerationException, InputIterationException, AlgorithmConfigurationException {
    //Setup
    AbaloneFixture fixture = new AbaloneFixture();
    RelationalInput input = fixture.getInputGenerator().generateNewCopy();
    PLIBuilder builder = new PLIBuilder(input);
    List<PositionListIndex> pliList = builder.getPLIList();

    List<Long2LongOpenHashMap> pliListCopy = new ArrayList<>();
    for (int i = 0; i < pliList.size(); i++) {
      pliListCopy.add(i, pliList.get(i).asHashMap());
    }

    for (int i = 0; i < pliList.size(); i++) {
      PositionListIndex pli = pliList.get(i);

      Long2LongOpenHashMap pliHash = pli.asHashMap();
      Long2LongOpenHashMap pliCopyHash = pliListCopy.get(i);

      assertEquals(pliHash, pliCopyHash);
      assertEquals(pliHash.keySet(), pliCopyHash.keySet());

      for (long row : pliHash.keySet()) {
        assertEquals(pliHash.get(row), pliCopyHash.get(row));
      }
    }
  }
}
