package de.metanome.algorithms.dvakmv;


import java.util.TreeSet; 

/**
 * * Implementation of AKMV
 *  @author Hazar.Harmouch
 */

public class AKMV { 
  private TreeSet<Integer> kMin;
  //the order of the used minimum
  private int k=4096;//for eps=0.01

  
  /**
   * @param
   * eps theoretical standard error
   */
  public  AKMV(double eps) {
      this.kMin = new TreeSet<Integer>();
      this.k = PowerOf2( (int) (2+(2/(Math.PI*eps*eps))));
  }
  
  /**
   * @param
   * key a new element from the dataset 
   */
  public boolean offer(Object key) { 
  int idx =MurmurHash.hash(key)& Integer.MAX_VALUE; 
      if (kMin.size() < k) {
          if (!kMin.contains(idx)) {
              kMin.add(idx);
              return true;
          }
      } else {
          if (idx < kMin.last())
              if (!kMin.contains(idx)) {
                  kMin.pollLast();
                  kMin.add(idx);
                  return true;
              }
      }
      
      return false;
  }
  
  /**
   * @return the cardinality estimation.
   **/
  public long cardinality() {
      if (kMin.size() < k)
          return kMin.size(); //exact 
      else
      { 
        return     (long) ((k - 1.0) * Integer.MAX_VALUE) / (kMin.last());}
  }
 
  
  
  /**
   * @return the next power of 2 larger than the input number.
   **/
 int PowerOf2(final int intnum) {
    int b = 1;
    while (b < intnum) {
      b = b << 1;
    }
    return b/2;
  }  
}