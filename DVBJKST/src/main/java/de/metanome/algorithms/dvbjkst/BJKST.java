package de.metanome.algorithms.dvbjkst;


import java.util.*;
import java.util.Map.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
/**
 * BJKST algorithm for distinct counting.
 * 
 * Reference: Bar-Yossef, Ziv, et al. "Counting distinct elements in a data stream." Randomization
 * and Approximation Techniques in Computer Science. Springer Berlin Heidelberg, 2002. 1-10.
 * @author Hazar.Harmouch
 */
public class BJKST {


  // Data structures
  private Object2IntRBTreeMap<String> buffer;

  // constants
  private Integer Zlevel = 0;// the index of the current level
  private int C = 576;// the parameter C based on the desired guarantees on the algorithms estimate
                      // for determinant factor <=1/3
  private double error = 0.01; //
  private int maxbufferSize;//Theta 576/error^2
                             // the size of the bucket B consisting of all tokens j with
                            // trailzero(h(j))>= z the compression factor

private int hseed=9001;
private int gseed=8000; 
 
 
/**
* @param 
* epsilon: the desired error limit
*/
  public BJKST(double epsilon) {
    this.error = epsilon;
    this.maxbufferSize = (int) ((this.C) / Math.pow(this.error, 2.0));
    this.buffer = new Object2IntRBTreeMap<String>();

  }



  public void offer(Object o) {
if(o!=null){
    int zereosP  = Long.numberOfTrailingZeros(MurmurHash.hash64(o,hseed));
    if (zereosP >= this.Zlevel) {
     buffer.put(Long.toBinaryString(MurmurHash.hash64(o,gseed)), zereosP);
      while (buffer.size() >= maxbufferSize) {
        this.Zlevel = this.Zlevel + 1;
        for (Iterator<Entry<String, Integer>> itr = buffer.entrySet().iterator(); itr.hasNext();) {
          Entry<String, Integer> element = itr.next();
          if (element.getValue() < Zlevel) {
            itr.remove();
          }
        }
      }
    }
}
  }

  /**
   * @return the cardinality estimation.
   **/
  public long cardinality() {
    int finalEstimate = (int) (Math.pow(2.0,this.Zlevel) * buffer.size());
    return finalEstimate;
  }
}
