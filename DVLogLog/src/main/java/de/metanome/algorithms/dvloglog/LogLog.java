package de.metanome.algorithms.dvloglog;



/** Implementation of LogLog
 ** Reference:
 *   Durand, M., & Flajolet, P. (2003). Loglog counting of large cardinalities. In Algorithms-ESA 2003 (pp. 605-617). Springer Berlin Heidelberg.
 * * @author Hazar.Harmouch
 * *  source with modification: https://github.com/addthis/stream-lib
 */
public class LogLog {
 
  /**
   * the number of buckets (m) {m=2^Range[0, 31]}
   */
  private int numofbucket=16;
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
  
  
  /**
   * correction factors
   * page 5 of the original paper: Alpha=(Gamma[-1/m]*(1 - 2^(1/m))/Log[2])^-m 
   * mAlpha=m* Alpha
   * E= mAlpha * 2^(average of M for all the buckets)
   * Gamma function computed using Mathematical AccountingForm
   * [ N
   *    [With
   *      [{m = 2^Range[0, 31]},
   *       m (Gamma[-1/m]*(1 - 2^(1/m))/Log[2])^-m
   *       ], 
   *    14
   *    ]
   * ]
   */
  protected static final double[] mAlpha = {
          0,
          0.44567926005415,
          1.2480639342271,
          2.8391255240079,
          6.0165231584809,
          12.369319965552,
          25.073991603111,
          50.482891762408,
          101.30047482584,
          202.93553338100,
          406.20559696699,
          812.74569744189,
          1625.8258850594,
          3251.9862536323,
          6504.3069874480,
          13008.948453415,
          26018.231384516,
          52036.797246302,
          104073.92896967,
          208148.19241629,
          416296.71930949,
          832593.77309585,
          1665187.8806686,
          3330376.0958140,
          6660752.5261049,
          13321505.386687,
          26643011.107850,
          53286022.550177,
          106572045.43483,
          213144091.20414,
          426288182.74275,
          852576365.81999
  };
  private double Ca;
  


  public LogLog(double error) {  
      
       this.numofbucket =PowerOf2((int) Math.pow(1.30/error, 2));
      
       this.Numbits=(int) (Math.log(numofbucket)/Math.log(2));
       
       if (Numbits >= (mAlpha.length - 1)) {
         throw new IllegalArgumentException(String.format("Max k (%d) exceeded: k=%d", mAlpha.length - 1, Numbits));
     }
       this.Ca = mAlpha[Numbits];
       this.M = new byte[numofbucket];
       
  }

  public boolean offer(Object o) {
    boolean affected = false;    
    if(o!=null){
               //hash the data value to get unsigned value
                long v=MurmurHash.hash64(o);
                // get the first k bit to determine the bucket 
                int j =(int)(v >>> (Long.SIZE - Numbits));
                // calculating rho(bk+1,bk+2 ....)
                byte r = (byte) (Long.numberOfLeadingZeros((v << Numbits) | (1 << (Numbits - 1))) + 1);
                // get the max rho
                if (M[j] < r) {
                    Rsum += r - M[j];
                    M[j] = r;
                    affected = true;
                }           
    }

        return affected;
    }

  public long cardinality() {
    double Ravg = Rsum / (double) numofbucket;
    return (long) (Ca * Math.pow(2, Ravg));
}
  
  public static int PowerOf2(final int intnum) {
    int b = 1;
    while (b < intnum) {
      b = b << 1;
    }
    return b/2;
  }
  }