package de.metanome.algorithms.dvmincount;



/**
 * Implementation of MinCount algorithm 
 * "F. Giroire. Order statistics and estimating cardinalities of massive data sets. Discrete Applied Mathematics, 157(2):406-427, 2009"
 * 
 * @author Hazar.Harmouch
 *
 */
public class MinCount {

  private int[][] kMin; // the minimums
  private double[][] kMin01; // the minimums in [0,1]
  private int k = 3; // the order of the used minimum k>=2
  private int numberOfBuckets = 1024; // number of buckets m>=1 to 2^31
  private int Numbits = 10;



  // alpha[log2(numberOfBuckets)]=numberOfBuckets*pow(exp(lgamma((double)3. -
  // 1./((double)numberOfBuckets)))/2.,-numberOfBuckets)
  protected static final double[] Alpha = {2.000000, 4.527074, 9.564176, 19.631417, 39.762721,
      80.023805, 160.545229, 321.587709, 643.672484, 1287.841943, 2576.180816, 5152.858538,
      10306.213972, 20612.924833, 41226.346553, 82453.189992, 164906.876868, 329814.250621,
      659628.998109, 1319258.493138, 2638517.483002, 5277035.463036, 10554071.422489,
      21108143.356138, 42216287.213608, 84432574.928546, 168865149.414817, 337730300.274572,
      675460599.477796, 1350921197.884245, 2701842394.697141};


  public MinCount(double eps) {


    // Calculate numberOfBuckets from the desired error error=1/sqrt(M) M=K*m float numbers
    numberOfBuckets = PowerOf2((int) (1 / (eps * eps * k)));// m=2^b
    Numbits = (int) (Math.log(numberOfBuckets) / Math.log(2));
    if (Numbits >= (Alpha.length - 1)) {
      throw new IllegalArgumentException(
          String.format("Max number of buckets (%d) exceeded: m=%d", Alpha.length - 1, Numbits));
    }
    this.kMin01 = new double[numberOfBuckets][k];
    this.kMin = new int[numberOfBuckets][k];


    /* initialize the tables of minima. */
    for (int i = 0; i < numberOfBuckets; i++)
      for (int j = 0; j < k; j++) {
        kMin01[i][j] = 0;
        kMin[i][j] = Integer.MAX_VALUE;
      }

  }

  /* Handle the minima with the new hashed value. */
  public boolean offer(Object key) {

    // hashing
    long hashed = Integer.toUnsignedLong(MurmurHash.hash(key));

    // get bucket number
    // get the first b bit to determine the bucket

    int bucket = (int) Long.remainderUnsigned(hashed, numberOfBuckets);

    // truncate the b bits
    int hashedValue = Integer.divideUnsigned((int) hashed, numberOfBuckets);
    /* handle the problem of (hashed << 32) >> 32 when m=1 */
    bucket = (bucket < numberOfBuckets) ? bucket : 0;

    // update the corresponding bucket
    int[] minBucket = kMin[bucket];

    /*
     * - The new hashed value is bigger than the 3d minimum: nothing to do. - It is the most common
     * case. We save a lot of time of doing this test first.
     */
    if (minBucket[2] <= hashedValue)
      return false;


    if (minBucket[1] <= hashedValue) {

      /* The word has already been seen. */
      if (hashedValue == minBucket[1])
        return false;

      minBucket[2] = hashedValue;
      return true;
    }

    if (minBucket[0] <= hashedValue) {

      /* The word has already been seen. */
      if (hashedValue == minBucket[0])
        return false;

      minBucket[2] = minBucket[1];
      minBucket[1] = hashedValue;
      return true;
    }


    minBucket[2] = minBucket[1];
    minBucket[1] = minBucket[0];
    minBucket[0] = hashedValue;


    return true;
  }



  public long cardinality() {


    double R = 0;

    int mthsize = Integer.divideUnsigned(Integer.MAX_VALUE + 1, numberOfBuckets);



    /* Compute the Ke minimum in [0,1] */
    /* If the minimum is 0 put it to 1/mthsize. */
    for (int i = 0; i < numberOfBuckets; i++)
      for (int j = 0; j < k; j++) {
        kMin01[i][j] = ((double) kMin[i][j]) / ((double) mthsize);


        if (kMin01[i][j] == 0)
          kMin01[i][j] = ((double) 1.) / ((double) mthsize);
      }


    // calculate R=-1/m(sum ln(Mki))
    for (int i = 0; i < numberOfBuckets; i++) {
      R -= Math.log(kMin01[i][k - 1]);

    }


    // f_0
    return (long) (Alpha[Numbits] * Math.exp(R / numberOfBuckets));

  }


  /**
   * @return the next power of 2 larger than the input number.
   **/
  int PowerOf2(final int intnum) {
    int b = 1;
    while (b < intnum) {
      b = b << 1;
    }
    return b;
  }

}
