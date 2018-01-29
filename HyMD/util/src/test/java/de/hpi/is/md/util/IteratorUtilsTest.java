package de.hpi.is.md.util;

import static org.assertj.core.api.Assertions.assertThat;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator.OfDouble;
import org.junit.Test;

public class IteratorUtilsTest {

	@Test
	public void testNext() {
		List<Integer> list = Collections.singletonList(1);
		Iterator<Integer> it = list.iterator();
		assertThat(IteratorUtils.next(it)).hasValue(1);
		assertThat(IteratorUtils.next(it)).isEmpty();
	}

	@Test
	public void testNextDouble() {
		DoubleList list = DoubleLists.singleton(1.0);
		OfDouble it = list.iterator();
		assertThat(IteratorUtils.next(it).boxed()).hasValue(1.0);
		assertThat(IteratorUtils.next(it).boxed()).isEmpty();
	}

}