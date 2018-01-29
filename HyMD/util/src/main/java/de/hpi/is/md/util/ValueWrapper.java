package de.hpi.is.md.util;

import lombok.Data;

@Data
public class ValueWrapper<T> {

	private final T value;
}
