package de.metanome.algorithms.dvhyperloglog;



/** Implementation of HyperLogLog
 ** Reference:
 *  Flajolet, P., Fusy, É., Gandouet, O., & Meunier, F. (2008). Hyperloglog: the analysis of a near-optimal cardinality estimation algorithm. DMTCS Proceedings, (1).
 * * @author Hazar.Harmouch
 * *  source with modification: https://github.com/addthis/stream-lib
 */
public class HyperLogLog {
 
  private final RegisterSet registerSet;
  private final int log2m;// b in the paper
  private final double alphaMM;// the multiplication of m and alpha m in the cardinality relation

  
  
  public HyperLogLog(double error) {
    int m =PowerOf2((int) Math.pow(1.04/error, 2)); // get the number of registers according to the standard error
    this.log2m  =(int) (Math.log(m)/Math.log(2)); // size of the portion of the hash used to determine the register 
    validateLog2m(log2m); // [4,16] for 32 bit i make it 32 as half of 64
    this.registerSet =new RegisterSet(1 << log2m);
    int n = 1 << this.log2m;
    alphaMM = getAlphaMM(log2m, n);
    
  }
  

private static void validateLog2m(int log2m) {
    if (log2m < 4 || log2m > 32) {
        throw new IllegalArgumentException("log2m argument is "
                                           + log2m + " and is outside the range [4, 32]");
    }
}

public boolean offer(Object o) {
  boolean affected=false;
  if(o!=null){
    final long x = MurmurHash.hash64(o);
    // j becomes the binary address determined by the first b log2m of x
    // j will be between 0 and 2^log2m
    final int j = (int) (x >>> (Long.SIZE - log2m));
    final int r = Long.numberOfLeadingZeros((x << this.log2m) | (1 << (this.log2m - 1)) + 1) + 1;
    affected= registerSet.updateIfGreater(j, r);
}
  return affected;
}

public long cardinality() {
    double registerSum = 0;
    int count = registerSet.count;
    double zeros = 0.0;
    for (int j = 0; j < registerSet.count; j++) {
        int val = registerSet.get(j);
        registerSum += 1.0 / (1 << val);
        if (val == 0) {
            zeros++;
        }
    }

    double estimate = alphaMM * (1 / registerSum);

    if (estimate <= (5.0 / 2.0) * count) {
        // Small Range Estimate
        return Math.round(linearCounting(count, zeros));
    } else if (estimate > (1 / 30) * Math.pow(2, 64))
      //large range correction
    { return Math.round( -1*Math.pow(2, 64)* Math.log(1-estimate/Math.pow(2, 64)) );}
    else  {
      //intermediate range no correction
      return Math.round(estimate);
  }
}

protected static double getAlphaMM(final int p, final int m) {
    // See the paper.
    switch (p) {
        case 4:
            return 0.673 * m * m;
        case 5:
            return 0.697 * m * m;
        case 6:
            return 0.709 * m * m;
        default:
            return (0.7213 / (1 + 1.079 / m)) * m * m;
    }
}

protected static double linearCounting(int m, double V) {
    return m * Math.log(m / V);
}

public static int PowerOf2(final int intnum) {
  int b = 1;
  while (b < intnum) {
    b = b << 1;
  }
  return b/2;
}
}