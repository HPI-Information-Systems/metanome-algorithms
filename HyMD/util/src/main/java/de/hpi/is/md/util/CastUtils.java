package de.hpi.is.md.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@SuppressWarnings("unchecked")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CastUtils {

	public static <T> T as(Object obj) {
		return (T) obj;
	}
}