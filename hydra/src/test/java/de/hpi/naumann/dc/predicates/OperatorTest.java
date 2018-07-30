package de.hpi.naumann.dc.predicates;

import org.junit.Assert;
import org.junit.Test;

import de.metanome.algorithm_integration.Operator;

/**
 * See Table 2 page 1500
 * 
 * @author tbleifuss
 *
 */
public class OperatorTest {

	@Test
	public void everyOperatorHasAnInverse() {
		// negation closed
		for (Operator op : Operator.values()) {
			Assert.assertNotNull(op.getInverse());
		}
	}

	@Test
	public void everyOperatorImpliesItself() {
		for (Operator op : Operator.values()) {
			boolean found = false;
			for (Operator imp : op.getImplications()) {
				if (imp == op)
					found = true;
			}
			Assert.assertTrue(found);
		}
	}

	@Test
	public void everyOperatorHasShortString() {
		// negation closed
		for (Operator op : Operator.values()) {
			Assert.assertNotNull(op.getShortString());
		}
	}
}
