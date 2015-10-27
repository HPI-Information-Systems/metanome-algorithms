package de.hpi.mpss2015n.approxind.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ReservoirSampler<T> {

  private final Random rnd = new Random();

  private final List<T> reservoir;
  private final int sampleSize;
  private int numItemsSeen = 0;


  public ReservoirSampler(int sampleSize) {
    this.reservoir = new ArrayList<>();
    this.sampleSize = sampleSize;

  }

  /**
   * Sample an item and store in the reservoir if needed.
   *
   * @param row The item to sample
   */
  public void sample(T row) {
    if (reservoir.size() < sampleSize) {
      reservoir.add(row);
    } else {
      // find a sample to replace
      int index = rnd.nextInt(numItemsSeen + 1);
      if (index < sampleSize) {
        reservoir.set(index, row);
      }
    }
    numItemsSeen++;
  }

  List<T> getSample() {
    return reservoir;
  }
}
