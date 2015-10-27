package de.hpi.mpss2015n.approxind.utils.HLL;

import de.hpi.mpss2015n.approxind.datastructures.HyperLogLog;

import java.util.ArrayList;
import java.util.List;

public class HLLData {

  private HyperLogLog hll;

  private boolean big;

  private int counter;

  private List<Long> sample;

  public HLLData(){
    big = false;
    sample = new ArrayList<>();
  }

  public List<Long> getSample() { return sample; }

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

  public int getCounter() {
    return counter;
  }

  public void incrementCounter() {
    counter++;
  }
}
