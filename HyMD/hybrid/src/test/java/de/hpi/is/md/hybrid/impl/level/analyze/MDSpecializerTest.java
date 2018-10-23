package de.hpi.is.md.hybrid.impl.level.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.hpi.is.md.ThresholdProvider;
import de.hpi.is.md.hybrid.impl.infer.FullLhsSpecializer;
import de.hpi.is.md.hybrid.impl.infer.FullSpecializer;
import de.hpi.is.md.hybrid.impl.infer.LhsSpecializer;
import de.hpi.is.md.hybrid.impl.infer.SpecializationFilter;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.OptionalDouble;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MDSpecializerTest {

	@Mock
	private ThresholdProvider provider;
	@Mock
	private SpecializationFilter specializationFilter;

	@Test
	public void test() {
		doReturn(OptionalDouble.of(0.8)).when(provider).getNext(0, 0.7);
		doReturn(OptionalDouble.empty()).when(provider).getNext(1, 1.0);
		doReturn(OptionalDouble.of(1.0)).when(provider).getNext(2, 0.0);
		doReturn(OptionalDouble.of(0.6)).when(provider).getNext(3, 0.0);
		doReturn(OptionalDouble.empty()).when(provider).getNext(4, 0.0);
		when(Boolean.valueOf(specializationFilter.filter(any(), any()))).thenReturn(Boolean.TRUE);
		int columnPairs = 5;
		MDSite lhs = new MDSiteImpl(columnPairs)
			.set(0, 0.7)
			.set(1, 1.0);
		MDElement rhs = new MDElementImpl(3, 0.8);
		Collection<MD> result = buildSpecializer()
			.specialize(new MDImpl(lhs, rhs));
		assertThat(result).hasSize(3);
		assertThat(result).contains(new MDImpl(lhs.clone().set(0, 0.8), new MDElementImpl(3, 0.8)));
		assertThat(result).contains(new MDImpl(lhs.clone().set(2, 1.0), new MDElementImpl(3, 0.8)));
		assertThat(result).contains(new MDImpl(lhs.clone().set(3, 0.6), new MDElementImpl(3, 0.8)));
	}

	private MDSpecializer buildSpecializer() {
		LhsSpecializer lhsSpecializer = new LhsSpecializer(provider);
		FullLhsSpecializer fullLhsSpecializer = new FullLhsSpecializer(lhsSpecializer);
		FullSpecializer specializer = new FullSpecializer(fullLhsSpecializer, specializationFilter);
		return new MDSpecializer(specializer);
	}

}