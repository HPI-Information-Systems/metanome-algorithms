package de.hpi.naumann.dc.helpers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParserHelperTest {
	@Test
	public void testIsDouble() {
		assertTrue(ParserHelper.isDouble("1.0"));
		assertFalse(ParserHelper.isDouble("Z2"));
	}
}
