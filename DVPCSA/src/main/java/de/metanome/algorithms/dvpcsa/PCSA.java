package de.metanome.algorithms.dvpcsa;

import java.util.BitSet;



/**
 * * Implementation of Probabilistic Counting with Stochastic Averaging algorithm. 
 * * Reference:
 * Flajolet, Philippe, and G. Nigel Martin. "Probabilistic counting algorithms for data base
 * applications." Journal of computer and system sciences 31.2 (1985): 182-209. 
 * * @author  Hazar.Harmouch
 */
public class PCSA {
  /**
   * correction factor
   */
  private static final double PHI = 0.77351D;
  /**
   * Number of vectors
   */
  private int numvectors = 64;
  /**
   * Size of the map in bits
   */
  private int bitmapSize = 64;// we use 64 bit hash function

  /**
   * Each Bitmap represents whether we have seen a hash function value whose binary representation
   * ends in 0*i1 one for each hash function
   */
  private BitSet[] bitmaps;

  public PCSA(double error) {
    // standard error= 0.78/sqrt(m) => m=(0.78/error)^2
    this.numvectors =  PowerOf2((int)Math.pow(0.78 / error, 2));
    bitmaps = new BitSet[numvectors];
    for (int i = 0; i < numvectors; i++)
      bitmaps[i] = new BitSet(bitmapSize);
  }

  public boolean offer(Object o) {
    boolean affected = false;
    if (o != null) {
     
      // hash the data value to get unsigned value
      long v = MurmurHash.hash64(o);
    
      // get the first k bit to determine the bucket
      int j = (int) Long.remainderUnsigned(v, numvectors);

      // calculating rho(bk+1,bk+2 ....)
      int r = Long.numberOfTrailingZeros(Long.divideUnsigned(v, numvectors));
      
      //update the map
      if (bitmaps[j].get(r) == false) {
        bitmaps[j].set(r, true);
        affected = true;
      }

    }

    return affected;
  }

  public long cardinality() {
    double sumR = 0;
    for (int j = 0; j < numvectors; j++) {
      int R = bitmaps[j].nextClearBit(0); 
      sumR += R;
    }
    return (long) Math.floor(numvectors / PHI * Math.pow(2, sumR / numvectors));
  }


  /**
   * @return the next power of 2 larger than the input number.
   **/
  public static int PowerOf2(final int intnum) {
    int b = 1;
    while (b < intnum) {
      b = b << 1;
    }
    return b/2;
  }

}
