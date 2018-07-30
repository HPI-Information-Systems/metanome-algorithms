package de.hpi.is.md.util;

public interface Hashable {

	default void hash(Hasher hasher) {
		Class<?> clazz = this.getClass();
		hasher.putClass(clazz);
	}

}
