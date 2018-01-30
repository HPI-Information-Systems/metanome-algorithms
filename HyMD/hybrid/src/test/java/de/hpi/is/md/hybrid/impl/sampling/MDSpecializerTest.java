package de.hpi.is.md.hybrid.impl.sampling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.SimilaritySet;
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
import java.util.Collection;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class MDSpecializerTest {

	@Mock
	private LhsSpecializer lhsSpecializer;
	@Mock
	private SpecializationFilter specializationFilter;

	@Test
	public void test() {
		MDSpecializer specializer = buildSpecializer();
		int columnPairs = 4;
		MDSite lhs = new MDSiteImpl(columnPairs).set(1, 0.7);
		SimilaritySet similaritySet = new SimilaritySet(new double[]{0.7, 0.8, 0.6, 0.5});
		doReturn(Optional.of(new MDSiteImpl(columnPairs).set(0, 0.8))).when(lhsSpecializer)
			.specialize(lhs, 0, similaritySet.get(0));
		doReturn(Optional.empty()).when(lhsSpecializer)
			.specialize(lhs, 1, similaritySet.get(1));
		doReturn(Optional.of(new MDSiteImpl(columnPairs).set(2, 0.7))).when(lhsSpecializer)
			.specialize(lhs, 2, similaritySet.get(2));
		doReturn(Optional.of(new MDSiteImpl(columnPairs).set(3, 0.9))).when(lhsSpecializer)
			.specialize(lhs, 3, similaritySet.get(3));
		when(specializationFilter.filter(any(), any())).thenReturn(true);
		int rhsAttr = 3;
		MDElement rhs = new MDElementImpl(rhsAttr, 0.8);
		Collection<MD> specialized = specializer.specialize(lhs, rhs, similaritySet);
		assertThat(specialized).hasSize(3);
		assertThat(specialized).contains(new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.8),
			new MDElementImpl(rhsAttr, 0.8)));
		assertThat(specialized).contains(new MDImpl(new MDSiteImpl(columnPairs).set(2, 0.7),
			new MDElementImpl(rhsAttr, 0.8)));
		assertThat(specialized).contains(new MDImpl(new MDSiteImpl(columnPairs).set(3, 0.9),
			new MDElementImpl(rhsAttr, 0.8)));
	}

	private MDSpecializer buildSpecializer() {
		FullLhsSpecializer fullLhsSpecializer = new FullLhsSpecializer(lhsSpecializer);
		FullSpecializer specializer = new FullSpecializer(fullLhsSpecializer, specializationFilter);
		return new MDSpecializer(specializer);
	}

}