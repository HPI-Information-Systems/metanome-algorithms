package de.hpi.naumann.dc.input;

import org.junit.Assert;
import org.junit.Test;

public class ColumnTest {

	@Test
	public void testToString() {
		Column c = new Column("relation", "test");
		Assert.assertEquals("relation.test", c.toString());
	}
}
