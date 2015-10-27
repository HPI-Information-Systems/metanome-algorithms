package de.hpi.mpss2015n.approxind.sampler;

import com.google.common.base.Preconditions;

import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class ReservoirRowSample implements RelationalInput {

  Vector<List<String>> reservoir;
  int numItemsSeen = 0;
  private final Random rnd = new Random();
  Iterator<List<String>> iterator;
  List<String> columnNames;
  String relationName;
  RelationalInput input;


  public ReservoirRowSample(RelationalInput input, int sampleSize) {
    this.reservoir = new Vector<>();
    relationName = input.relationName();
    columnNames = input.columnNames();
    try {
      while (input.hasNext()) {
        List<String> item = input.next();
        sample(item, sampleSize);
      }
    } catch (InputIterationException e) {
      e.printStackTrace();
    }
    iterator = reservoir.iterator();
    this.input = input;
  }

    /**
   * Sample an item and store in the reservoir if needed.
   *
   * @param item The item to sample - may not be null.
   */
  protected void sample(List<String> item, int sampleSize) {
    Preconditions.checkNotNull(item);
    if (reservoir.size() < sampleSize) {
      // reservoir not yet full, just append
      reservoir.add(item);
    } else {
      // find a sample to replace
      int rIndex = rnd.nextInt(numItemsSeen + 1);
      if (rIndex < sampleSize) {
        reservoir.set(rIndex, item);
      }
    }
    numItemsSeen++;
  }

  @Override
  public boolean hasNext() throws InputIterationException {
    return iterator.hasNext();
  }

  @Override
  public List<String> next() throws InputIterationException {
    return iterator.next();
  }

  @Override
  public int numberOfColumns() {
    return columnNames.size();
  }

  @Override
  public String relationName() {
    return relationName;
  }

  @Override
  public List<String> columnNames() {
    return columnNames;
  }

  @Override
  public void close() throws Exception {
    input.close();
  }
}
