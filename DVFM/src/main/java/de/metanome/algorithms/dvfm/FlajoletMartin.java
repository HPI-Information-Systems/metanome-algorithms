package de.metanome.algorithms.dvfm;



import java.util.BitSet;
import java.util.Random;


/**
 * * Implementation of Probabilistic counting algorithm or FM Sketch.
 * * Reference:
 *   Flajolet, Philippe, and G. Nigel Martin. "Probabilistic counting algorithms
 *   for data base applications." Journal of computer and system sciences 31.2 
 *   (1985): 182-209. 
 * * @author Hazar.Harmouch.
 */


public class FlajoletMartin {
  /**
   * correction factor
   */
  private static final double PHI = 0.77351D;
  /**
   * Number of hash functions
   */
  private int numHashFunctions=64;//m depends on the error
  /**
   * Size of the map in bits
   */
  private int bitmapSize=64; //L=64 this is the max for the hash functions 
  /**
   * The u=generated hash functions
   */
  private  int[] seeds;

  /**
   * Each Bitmap represents whether we have seen a hash function value whose binary representation ends in 0*i1
   * one for each hash function
   */
  private BitSet[] bitmaps;


  public FlajoletMartin(double error) {
      // standard error= 1/sqrt(m) => m=(1/error)^2  
      this.numHashFunctions=nextPowerOf2((int)Math.pow(1/error, 2));
      bitmaps = new BitSet[numHashFunctions];
      for(int i=0;i<numHashFunctions;i++)
        bitmaps[i]=new BitSet(bitmapSize);
      seeds = new int[numHashFunctions];
      generateseeds();
  }
 
    


  public boolean offer(Object o) {
    boolean affected = false;    
    if(o!=null){
            for (int j=0; j<numHashFunctions; j++) {
                int s = seeds[j];
                //non-negative hash values
                long v = MurmurHash.hash64(o,s);
                //index := pi(hash(x))
                int index =rho(v);
                //update the corresponding bit in the bitmap
                if (bitmaps[j].get(index)==false) {
                  bitmaps[j].set(index,true);
                    affected = true;
                }   
        }
    }
        return affected;
    }
  
  

  public long cardinality() {
    double sumR=0;
        for (int j=0; j<numHashFunctions; j++) {
            sumR += bitmaps[j].nextClearBit(0);
            
        }
    
    return (long) (Math.pow(2, sumR/numHashFunctions) / PHI);
}

  /**
   * @return the position of the least significant 1-bit in the binary representation of y
   *         rho(O)=0
   */
  private int rho(long y) {
    return Long.numberOfTrailingZeros(y);
}

  /**
   * @return the next power of 2 larger than the input number.
   **/
  public static int nextPowerOf2(final int intnum) {
    int b = 1;
    while (b < intnum) {
      b = b << 1;
    }
    return b/2;
  }

  
  private void generateseeds() {
    Random generator = new Random(9001);
    for (int j=0; j<numHashFunctions; j++) {
      seeds[j] =generator.nextInt();
       }
    
}
}