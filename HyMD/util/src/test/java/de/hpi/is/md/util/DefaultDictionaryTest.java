package de.hpi.is.md.util;

public class DefaultDictionaryTest extends DictionaryTest {

	@Override
	protected <T> Dictionary<T> createDictionary() {
		return new DefaultDictionary<>();
	}

}
