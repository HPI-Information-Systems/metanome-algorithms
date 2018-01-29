package de.hpi.is.md.util;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

	public static String toLowerCase(String s) {
		return Optional.ofNullable(s)
			.map(String::toLowerCase)
			.orElse(null);
	}

	public static String join(CharSequence delimiter, Iterable<?> elements) {
		return StreamUtils.seq(elements).toString(delimiter);
	}
}
