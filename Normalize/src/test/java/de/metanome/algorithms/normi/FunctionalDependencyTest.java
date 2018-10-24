package de.metanome.algorithms.normi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.BitSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.metanome.algorithms.normalize.structures.FunctionalDependency;
import de.metanome.algorithms.normalize.structures.Schema;

public class FunctionalDependencyTest {

	private int numAttributes;
	private Schema schema;
	
	@Before
	public void setUp() throws Exception {
		this.numAttributes = 10;
		
		BitSet attributes = new BitSet();
		attributes.set(0, this.numAttributes);
		
		this.schema = Mockito.mock(Schema.class);
		Mockito.when(this.schema.getAttributes()).thenReturn(attributes);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void keyLengthScoreShouldBeCorrect() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method method = FunctionalDependency.class.getDeclaredMethod("keyLengthScore");
		method.setAccessible(true);
		
		BitSet lhs = new BitSet(this.numAttributes);
		BitSet rhs = new BitSet(this.numAttributes);
		
		FunctionalDependency fd = new FunctionalDependency(lhs, rhs, this.schema);
		assertEquals("Length score for empty lhs should be 0", Float.valueOf(0), method.invoke(fd));
		
		lhs.set(2);
		assertEquals("Length score for |lhs|=1 should be 1", Float.valueOf(1), method.invoke(fd));
		
		lhs.set(7);
		assertTrue("Length score for |lhs|=2 should smaller than 1", 1 > ((Float) method.invoke(fd)).floatValue());
	}
}
