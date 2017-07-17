package de.metanome.algorithms.dvsuperloglog;

import java.util.Arrays;

/** Implementation of SuperLogLog
 ** Reference:
 *   Durand, M., & Flajolet, P. (2003). Loglog counting of large cardinalities. In Algorithms-ESA 2003 (pp. 605-617). Springer Berlin Heidelberg.
 * * @author Hazar.Harmouch
 */
public class SuperLogLog {
 
  /**
   * the number of buckets (m) {m=2^Range[0, 31]}
   */
  private int numofbucket=16;
  /**
   * the number of buckets (m0)=floor(m*0.7) according to the truncation rule
   */
  private int truncatednumofbucket=11;
  /**
   * the number of bits used to determine the bucket (k)=log2(m) {k=Range[0, 31]}
   */
  private int Numbits=4;
  
  /**
   * The maximum bit set
   */
  private byte[] M;
  
  /**
   * sum of maxs
   */
  private int Rsum = 0;
  
  /**The maximum cardinality*/
  private double Nmax=Math.pow(10, 19);
  
  /**
   * Restriction Rule**/
  private double B;
  private double Ca=1.09295;
 
  public SuperLogLog(double error) {  
       this.numofbucket =PowerOf2((int) Math.pow(1.05/error, 2));
       this.Numbits=(int) (Math.log(numofbucket)/Math.log(2));
       this.M = new byte[numofbucket];
       //truncation rule parameters
       this.truncatednumofbucket=(int)Math.floor(0.7*numofbucket);
    
       //restriction Rule parameters
       B=Math.ceil(Math.log(Nmax/numofbucket)/Math.log(2)+3);
  }

  public boolean offer(Object o) {
    boolean affected = false;    
    if(o!=null){
               //hash the data value to get unsigned value
                long v=MurmurHash.hash64(o);
                // get the first k bit to determine the bucket 
             // get the first k bit to determine the bucket 
                int j =(int)(v >>> (Long.SIZE - Numbits));
                // calculating rho(bk+1,bk+2 ....)
                byte r = (byte) (Long.numberOfLeadingZeros((v << Numbits) | (1 << (Numbits - 1))) + 1);
                // get the max rho
               if (M[j] < r) {
                    M[j] = r;
                    affected = true;
                }           
    }

        return affected;
    }

  public long cardinality() {
  
  //take into account just the smallest m0 value and discard the rest
    Arrays.sort(M);
    for (int j = 0; j < truncatednumofbucket; j++)
      //use register values that are in the interval [0...ceil(log2(nmax/m)+3)]
       if(M[j]>0 && M[j]<B)
        Rsum+=M[j];// the trancated sum
    double Ravg = Rsum / (double) truncatednumofbucket;
    return (long) Math.floor(Ca*truncatednumofbucket* Math.pow(2, Ravg));
}
  
  public static int PowerOf2(final int intnum) {
    int b = 1;
    while (b < intnum) {
      b = b << 1;
    }
    return b/2;
  }
  }