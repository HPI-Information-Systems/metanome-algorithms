package de.hpi.is.md.util;

import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectUtils {

	public static boolean bothNull(Object obj1, Object obj2) {
		return obj1 == null && obj2 == null;
	}

	public static boolean eitherNull(Object obj1, Object obj2) {
		return obj1 == null || obj2 == null;
	}

	public static boolean notEquals(Object obj1, Object obj2) {
		return !Objects.equals(obj1, obj2);
	}
}
