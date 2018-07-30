package de.hpi.is.md.util;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MathUtils {

	public static double divide(double dividend, double divisor) {
		return divisor == 0.0 ? 0.0 : dividend / divisor;
	}

	public static long increment(long l) {
		return l + 1;
	}

	public static double[] max(double[] d1, double[] d2) {
		checkArgument(d1.length == d2.length, "Arrays must have same size");
		double[] result = new double[d1.length];
		Arrays.setAll(result, getMax(d1, d2));
		return result;
	}

	public static long multiply(long factor1, long factor2) {
		return factor1 * factor2;
	}

	public static int roundToInt(double d) {
		return (int) Math.round(d);
	}

	private static IntToDoubleFunction getMax(double[] d1, double[] d2) {
		return i -> Math.max(d1[i], d2[i]);
	}
}
