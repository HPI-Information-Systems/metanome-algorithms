package de.hpi.is.md.relational;

public interface HasName {

	String getName();

	default boolean hasName(String name) {
		return getName().equalsIgnoreCase(name);
	}
}
