package de.hpi.is.md.util;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BigDecimalUtils {

	private static final BigDecimal[] NUMBERS = new BigDecimal[500];

	static {
		for (int i = 0; i < NUMBERS.length; i++) {
			NUMBERS[i] = BigDecimal.valueOf(i);
		}
	}

	public static BigDecimal valueOf(int number) {
		return 0 <= number && number < NUMBERS.length ? NUMBERS[number]
			: BigDecimal.valueOf(number);
	}

}
