package de.hpi.is.md.util;

public class Int2ObjectHashMultimapTest extends Int2ObjectMultimapTest {

	@Override
	protected Int2ObjectMultimap<String> create() {
		return new Int2ObjectHashMultimap<>();
	}

}