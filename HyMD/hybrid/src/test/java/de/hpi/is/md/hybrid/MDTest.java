package de.hpi.is.md.hybrid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.Iterables;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;

public abstract class MDTest {

	@Test
	public void testCardinality() {
		MDSite site = createMDSite(4)
			.set(0, 0.4)
			.set(1, 0.5);
		assertThat(site.cardinality()).isEqualTo(2);
		site.set(0, 0.5);
		assertThat(site.cardinality()).isEqualTo(2);
		site.clear(1);
		assertThat(site.cardinality()).isEqualTo(1);
		site.clear(1);
		assertThat(site.cardinality()).isEqualTo(1);
	}

	@Test
	public void testClone() {
		MDSite site = createMDSite(4)
			.set(0, 0.4)
			.set(1, 0.5);
		MDSite clone = site.clone();
		assertThat(clone).isEqualTo(site);
		assertThat(clone).isNotSameAs(site);
		site.clear(0);
		site.clear(1);
		assertThat(site.cardinality()).isEqualTo(0);
		assertThat(clone.get(0).boxed()).hasValue(Double.valueOf(0.4));
		assertThat(clone.get(1).boxed()).hasValue(Double.valueOf(0.5));
		assertThat(clone.get(2).boxed()).isEmpty();
		assertThat(clone.get(3).boxed()).isEmpty();
		assertThat(clone.get(4).boxed()).isEmpty();
	}

	@Test
	public void testGet() {
		MDSite site = createMDSite(4)
			.set(0, 0.4)
			.set(1, 0.5);
		assertThat(site.get(0).boxed()).hasValue(Double.valueOf(0.4));
		assertThat(site.get(1).boxed()).hasValue(Double.valueOf(0.5));
		assertThat(site.get(2).boxed()).isEmpty();
		assertThat(site.get(3).boxed()).isEmpty();
		assertThat(site.get(4).boxed()).isEmpty();
	}

	@Test
	public void testIsInLhs() {
		MDSite lhs = createMDSite(4)
			.set(0, 0.4)
			.set(1, 0.5);
		MDElement rhs = createMDElement(3, 0.5);
		MD md = createMD(lhs, rhs);
		assertThat(md.isInLhs(0)).isTrue();
		assertThat(md.isInLhs(1)).isTrue();
		assertThat(md.isInLhs(2)).isFalse();
		assertThat(md.isInLhs(3)).isFalse();
		assertThat(md.isInLhs(4)).isFalse();
	}

	@Test
	public void testIsRhs() {
		MDSite lhs = createMDSite(4)
			.set(0, 0.4)
			.set(1, 0.5);
		MDElement rhs = createMDElement(3, 0.5);
		MD md = createMD(lhs, rhs);
		assertThat(md.isRhs(0)).isFalse();
		assertThat(md.isRhs(1)).isFalse();
		assertThat(md.isRhs(2)).isFalse();
		assertThat(md.isRhs(3)).isTrue();
		assertThat(md.isRhs(4)).isFalse();
	}

	@Test
	public void testIteration() {
		MDSite site = createMDSite(5)
			.set(1, 0.4)
			.set(3, 0.5);
		assertThat(Iterables.size(site)).isEqualTo(2);
		assertThat(site).contains(createMDElement(1, 0.4));
		assertThat(site).contains(createMDElement(3, 0.5));
	}

	@Test(expected = NoSuchElementException.class)
	public void testNoSuchElement() {
		MDSite site = createMDSite(5)
			.set(1, 0.4)
			.set(3, 0.5);
		Iterator<MDElement> it = site.iterator();
		for (int i = 0; i < 3; i++) {
			it.next();
		}
		fail();
	}

	protected abstract MD createMD(MDSite lhs, MDElement rhs);

	protected abstract MDElement createMDElement(int attr, double threshold);

	protected abstract MDSite createMDSite(int columnPairs);
}
