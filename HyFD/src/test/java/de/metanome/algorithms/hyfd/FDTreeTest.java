package de.metanome.algorithms.hyfd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.BitSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.metanome.algorithms.hyfd.structures.FDTree;

public class FDTreeTest {
	
	private FDTree fdtree;
	
	@Before
	public void setUp() throws Exception {
		this.fdtree = new FDTree(5, -1);
		BitSet lhs = new BitSet();
		lhs.set(0);
		lhs.set(1);
		lhs.set(3);
		this.fdtree.addFunctionalDependency(lhs, 2);
	}

	@After
	public void tearDown() throws Exception {
	}
	
/*	@Test
	public void testContainsSpecialization() {
		BitSet lhs = new BitSet();
		lhs.set(0);
		lhs.set(1);
		assertTrue(this.fdtree.containsFdOrSpecialization(lhs, 2));
	}
*/	
	@Test
	public void testContainsGeneralization() {
		BitSet lhs = new BitSet();
		lhs.set(0);
		lhs.set(1);
		assertFalse(this.fdtree.containsFdOrGeneralization(lhs, 2));
		lhs.set(3);
		lhs.set(4);
		assertTrue(this.fdtree.containsFdOrGeneralization(lhs, 2));
	}
	
/*	@Test
	public void testGetSpecialization() {
		BitSet lhs = new BitSet();
		lhs.set(0);
		lhs.set(1);
		BitSet specLhs = new BitSet();
		assertTrue(this.fdtree.getSpecialization(lhs, 2, 0, specLhs));
		BitSet expResult = new BitSet();
		
		expResult.set(0);
		expResult.set(1);
		expResult.set(3);
		assertEquals(expResult, specLhs);
		
	}
*/	
	@Test 
	public void testGetGeneralizationAndDelete() {
		BitSet lhs = new BitSet();
		lhs.set(0);
		lhs.set(1);
		lhs.set(3);
		lhs.set(4);
		BitSet specLhs = this.fdtree.getFdOrGeneralization(lhs, 2);
		
		BitSet expResult = new BitSet();
		
		expResult.set(0);
		expResult.set(1);
		expResult.set(3);
		assertEquals(expResult, specLhs);
	}
	
/*	@Test
	public void testFilterSpecialization() {
		BitSet lhs = new BitSet();
		lhs.set(0);
		lhs.set(3);
		this.fdtree.addFunctionalDependency(lhs, 2);
		
		this.fdtree.filterSpecializations();
		
		BitSet expResult = new BitSet();
		expResult.set(0);
		expResult.set(1);
		expResult.set(3);
		assertFalse(this.fdtree.containsFdOrGeneralization(lhs, 2));
	}
*/	
	@Test
	public void testDeleteGeneralizations() {
		fdtree = new FDTree(4, -1);
		BitSet lhs = new BitSet();
		lhs.set(0);
		lhs.set(1);
		
		this.fdtree.addFunctionalDependency(lhs, 3);
		lhs.clear(1);
		lhs.set(2);
		this.fdtree.addFunctionalDependency(lhs, 3);
		
		//lhs.set(1);
		//this.fdtree.deleteGeneralizations(lhs, 3, 0);
		//assertTrue(this.fdtree.isEmpty());
	}
	
/*	@Test 
	public void testContainsSpezialization() {
		FDTree fdtree = new FDTree(5);
		BitSet lhs = new BitSet();
		lhs.set(0);
		lhs.set(2);
		lhs.set(4);
		fdtree.addFunctionalDependency(lhs, 3);
		lhs.clear(0);
		lhs.set(1);
		fdtree.addFunctionalDependency(lhs, 3);
		
		lhs.clear(2);
		boolean result = fdtree.containsFdOrSpecialization(lhs, 3);
		assertTrue(result);
	}
*/
}
