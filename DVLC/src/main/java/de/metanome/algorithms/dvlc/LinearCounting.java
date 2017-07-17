package de.metanome.algorithms.dvlc;

/**
 * * Implementation of Linear Counting algorithm.
 * * Reference: Whang, K. Y., Vander-Zanden, B. T., & Taylor, H. M. (1990). A linear-time probabilistic counting algorithm for database applications.
 *   ACM Transactions on Database Systems (TODS), 15(2), 208-229 
 * * @author Hazar.Harmouch
 * * * source with modification: https://github.com/addthis/stream-lib
 */


import static java.lang.Math.exp;
import static java.lang.Math.max;
import static java.lang.Math.pow;

/**
 * See <i>A Linear-Time Probabilistic Counting Algorithm for Database Applications</i>
 * by Whang, Vander-Zanden, Taylor
 */
public class LinearCounting   {

    /**
     * Bitmap
     * Hashed stream elements are mapped to bits in this array
     */
    protected byte[] map;

    /**
     * Size of the map in bits
     */
    protected final int length;


    /**
     * Number of bits left unset in the map
     */
    protected int count;
    
    /**
     * Taken from Table II of Whang et al.
     */
    protected final static int[] onePercentErrorLength =
            {
                    5034, 5067, 5100, 5133, 5166, 5199, 5231, 5264, 5296,                    // 100 - 900
                    5329, 5647, 5957, 6260, 6556, 6847, 7132, 7412, 7688,                    // 1000 - 9000
                    7960, 10506, 12839, 15036, 17134, 19156, 21117, 23029, 24897,            // 10000 - 90000
                    26729, 43710, 59264, 73999, 88175, 101932, 115359, 128514, 141441,       // 100000 - 900000
                    154171, 274328, 386798, 494794, 599692, 702246, 802931, 902069, 999894,  // 1000000 - 9000000
                    1096582                                                                  // 10000000
            };
    /**
     * Returns a LinearCounting estimator which keeps estimates below 1% error on average and has
     * a low likelihood of saturation (0.7%) for any stream with
     * cardinality less than maxCardinality
     *
     * @param maxCardinality
     */
    public LinearCounting(int maxCardinality) {
      int size=onePercentError(maxCardinality);
      this.length = 8 * size;
      this.count = this.length;
      map = new byte[size];
  }

    /**
     * Builds Linear Counter with arbitrary standard error and maximum expected cardinality.
     * <p/>
     *
     * @param eps            standard error as a fraction (e.g. {@code 0.01} for 1%)
     * @param maxCardinality maximum expected cardinality
     */
    public LinearCounting(double eps, int maxCardinality) {
        int sz = computeRequiredBitMaskLength(maxCardinality, eps);
        int size=(int) Math.ceil(sz / 8D);
        this.length = 8 * size;
        this.count = this.length;
        map = new byte[size];
    }

    
    public long cardinality() {
        return (long) (Math.round(length * Math.log(length / ((double) count))));
    }

    public byte[] getBytes() {
        return map;
    }

   
    public boolean offerHashed(long hashedLong) {
        throw new UnsupportedOperationException();
    }


    public boolean offerHashed(int hashedInt) {
        throw new UnsupportedOperationException();
    }


    public boolean offer(Object o) {
      
        boolean modified = false;
if(o!=null){
        long hash = (long) MurmurHash.hash64(o);
        int bit = (int) ((hash & 0xFFFFFFFFL) % (long) length);
        int i = bit / 8;
        byte b = map[i];
        byte mask = (byte) (1 << (bit % 8));
        if ((mask & b) == 0) {
            map[i] = (byte) (b | mask);
            count--;
            modified = true;
        }
}
        return modified;
    }

  
    public int sizeof() {
        return map.length;
    }

    protected int computeCount() {
        int c = 0;
        for (byte b : map) {
            c += Integer.bitCount(b & 0xFF);
        }

        return length - c;
    }

    /**
     * @return (# set bits) / (total # of bits)
     */
    public double getUtilization() {
        return (length - count) / (double) length;
    }

    public int getCount() {
        return count;
    }

    public boolean isSaturated() {
        return (count == 0);
    }

    /**
     * For debug purposes
     *
     * @return
     */
    protected String mapAsBitString() {
        StringBuilder sb = new StringBuilder();
        for (byte b : map) {
            String bits = Integer.toBinaryString(b);
            for (int i = 0; i < 8 - bits.length(); i++) {
                sb.append('0');
            }
            sb.append(bits);
        }
        return sb.toString();
    }

         
      
        /**
         * Runs binary search to find minimum bit mask length that holds precision inequality.
         *
         * @param n   expected cardinality
         * @param eps desired standard error
         * @return minimal required bit mask length
         */
        private static int computeRequiredBitMaskLength(double n, double eps) {
            if (eps >= 1 || eps <= 0) {
                throw new IllegalArgumentException("Epsilon should be in (0, 1) range");
            }
            if (n <= 0) {
                throw new IllegalArgumentException("Cardinality should be positive");
            }
            int fromM = 1;
            int toM = 100000000;
            int m;
            double eq;
            do {
                m = (toM + fromM) / 2;
                eq = precisionInequalityRV(n / m, eps);
                if (m > eq) {
                    toM = m;
                } else {
                    fromM = m + 1;
                }
            } while (toM > fromM);
            return m > eq ? m : m + 1;
        }

        /**
         * @param t   load factor for linear counter
         * @param eps desired standard error
         */
        private static double precisionInequalityRV(double t, double eps) {
            return max(1.0 / pow(eps * t, 2), 5) * (exp(t) - t - 1);
        }

        /**
         * Returns a LinearCounting estimator which keeps estimates below 1% error on average and has
         * a low likelihood of saturation (0.7%) for any stream with
         * cardinality less than maxCardinality
         *
         * @param maxCardinality
         * @return
         * @throws IllegalArgumentException if maxCardinality is not a positive integer
         */
        public static int onePercentError(int maxCardinality) {
            if (maxCardinality <= 0) {
                throw new IllegalArgumentException("maxCardinality (" + maxCardinality + ") must be a positive integer");
            }

            int length = -1;
            if (maxCardinality < 100) {
                length = onePercentErrorLength[0];
            } else if (maxCardinality < 10000000) {
                int logscale = (int) Math.log10(maxCardinality);
                int scaleValue = (int) Math.pow(10, logscale);
                int scaleIndex = maxCardinality / scaleValue;
                int index = 9 * (logscale - 2) + (scaleIndex - 1);
                int lowerBound = scaleValue * scaleIndex;
                length = lerp(lowerBound, onePercentErrorLength[index], lowerBound + scaleValue, onePercentErrorLength[index + 1], maxCardinality);

                //System.out.println(String.format("Lower bound: %9d, Max cardinality: %9d, Upper bound: %9d", lowerBound, maxCardinality, lowerBound+scaleValue));
                //System.out.println(String.format("Lower bound: %9d, Interpolated   : %9d, Upper bound: %9d", onePercentErrorLength[index], length, onePercentErrorLength[index+1]));
            } else if (maxCardinality < 50000000) {
                length = lerp(10000000, 1096582, 50000000, 4584297, maxCardinality);
            } else if (maxCardinality < 100000000) {
                length = lerp(50000000, 4584297, 100000000, 8571013, maxCardinality);
            } else if (maxCardinality <= 120000000) {
                length = lerp(100000000, 8571013, 120000000, 10112529, maxCardinality);
            } else {
                length = maxCardinality / 12;
            }

            return (int) Math.ceil(length / 8D);

         
        }

       
               /**
         * @param x0
         * @param y0
         * @param x1
         * @param y1
         * @param x
         * @return linear interpolation
         */
        protected static int lerp(int x0, int y0, int x1, int y1, int x) {
            return (int) Math.ceil(y0 + (x - x0) * (double) (y1 - y0) / (x1 - x0));
        }
   
    }
