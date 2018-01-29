package de.hpi.is.md.hybrid.impl;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@EqualsAndHashCode
public class Selector {

	private final int[] values;

	int get(int i) {
		return values[i];
	}
}
