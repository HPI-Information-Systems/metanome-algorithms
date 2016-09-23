package de.hpi.mpss2015n.approxind.utils.HLL;

import de.hpi.mpss2015n.approxind.datastructures.HyperLogLog;

import java.util.ArrayList;
import java.util.List;

/**
 * This data class stores a HyperLogLog structure plus some more metadata for a column combination.
 */
public class HLLData {

  private HyperLogLog hll;

  /**
   * Tells whether the described column combination is big, i.e., it is not covered by the inverted index.
   */
  private boolean big;

  public HLLData(){
    big = false;
  }


  public HyperLogLog getHll() {
    return hll;
  }

  public void setHll(HyperLogLog hll) {
    this.hll = hll;
  }

  public void setBig(boolean big) {
    this.big = big;
  }

  public boolean isBig() {
    return big;
  }

}
