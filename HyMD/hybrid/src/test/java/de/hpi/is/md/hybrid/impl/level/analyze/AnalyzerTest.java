package de.hpi.is.md.hybrid.impl.level.analyze;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hpi.is.md.hybrid.Analyzer;
import de.hpi.is.md.hybrid.Lattice.LatticeMD;
import de.hpi.is.md.hybrid.SupportedMD;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.infer.ThresholdLowerer;
import de.hpi.is.md.hybrid.impl.lattice.FullLattice;
import de.hpi.is.md.hybrid.impl.level.AnalyzeTask;
import de.hpi.is.md.hybrid.impl.level.Statistics;
import de.hpi.is.md.hybrid.impl.md.MDElementImpl;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.impl.md.MDSiteImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.jooq.lambda.Seq;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class AnalyzerTest {

	@Mock
	private MDSpecializer specializer;
	@Mock
	private ThresholdLowerer lowerer;
	@Mock
	private FullLattice fullLattice;

	@Test
	public void testInfer() {
		testInfer(true);
	}

	@Test
	public void testInferGeneralization() {
		Collection<SupportedMD> found = new ArrayList<>();
		int columnPairs = 4;
		MDSite lhs1 = new MDSiteImpl(columnPairs).set(0, 0.5);
		MDSite lhs2 = new MDSiteImpl(columnPairs).set(0, 0.6);
		MDElement rhs = new MDElementImpl(1, 1.0);
		MD inferred1 = new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.6), rhs);
		doReturn(Optional.of(Mockito.mock(LatticeMD.class))).when(fullLattice)
			.addIfMinimalAndSupported(inferred1);
		MD inferred2 = new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.7), rhs);
		doReturn(Optional.empty()).when(fullLattice).addIfMinimalAndSupported(inferred2);
		Analyzer analyzer = createAnalyzer(found);
		RhsResult rhsResult1 = RhsResult.builder()
			.rhsAttr(1)
			.threshold(1.0)
			.from(1.0)
			.violations(Collections.emptyList())
			.validAndMinimal(false)
			.build();
		RhsResult rhsResult2 = RhsResult.builder()
			.rhsAttr(1)
			.threshold(1.0)
			.from(1.0)
			.violations(Collections.emptyList())
			.validAndMinimal(false)
			.build();
		doReturn(Collections.singletonList(inferred1)).when(specializer).specialize(new MDImpl(lhs1, rhs));
		doReturn(Collections.singletonList(inferred2)).when(specializer).specialize(new MDImpl(lhs2, rhs));
		analyzer.analyze(Seq.of(
			new ValidationResult(new LhsResult(lhs1, 2), Collections.singletonList(rhsResult1)),
			new ValidationResult(new LhsResult(lhs2, 2), Collections.singletonList(rhsResult2)))
			.map(this::toTask)
			.toList());
		InOrder inOrder = Mockito.inOrder(specializer);
		inOrder.verify(specializer).specialize(new MDImpl(lhs1, rhs));
		inOrder.verify(specializer).specialize(new MDImpl(lhs2, rhs));
	}

	@Test
	public void testInferNotMinimal() {
		testInfer(false);
	}

	@Test
	public void testInferNotSupported() {
		Analyzer analyzer = createAnalyzer(new ArrayList<>());
		RhsResult rhsResult1 = RhsResult.builder()
			.rhsAttr(1)
			.threshold(0.8)
			.violations(Collections.emptyList())
			.validAndMinimal(true)
			.build();
		RhsResult rhsResult2 = RhsResult.builder()
			.rhsAttr(2)
			.threshold(0.8)
			.violations(Collections.emptyList())
			.validAndMinimal(true)
			.build();
		Statistics statistics = analyzer
			.analyze(Seq.of(new ValidationResult(new LhsResult(new MDSiteImpl(4).set(0, 0.5), 1),
				Arrays.asList(rhsResult1, rhsResult2)))
				.map(this::toTask)
				.toList());
		assertThat(statistics.getNewDeduced()).isEqualTo(0);
	}

	@Test
	public void testNotSupported() {
		Collection<SupportedMD> found = new ArrayList<>();
		Analyzer analyzer = createAnalyzer(found);
		int columnPairs = 4;
		MDSite lhs = new MDSiteImpl(columnPairs);
		MDElement rhs = new MDElementImpl(1, 0.8);
		RhsResult rhsResult = RhsResult.builder()
			.rhsAttr(1)
			.threshold(0.8)
			.violations(Collections.emptyList())
			.validAndMinimal(true)
			.build();
		analyzer.analyze(Seq.of(
			new ValidationResult(new LhsResult(lhs, 1), Collections.singletonList(rhsResult)))
			.map(this::toTask)
			.toList());
		assertThat(found).isEmpty();
		verify(fullLattice).markNotSupported(lhs);
		verify(specializer, never()).specialize(any());
	}

	@Test
	public void testNotValidAndMinimal() {
		Collection<SupportedMD> found = new ArrayList<>();
		Analyzer analyzer = createAnalyzer(found);
		int columnPairs = 4;
		MDSite lhs = new MDSiteImpl(columnPairs);
		MDElement rhs = new MDElementImpl(1, 0.8);
		when(specializer.specialize(new MDImpl(lhs, rhs))).thenReturn(Collections.emptyList());
		RhsResult rhsResult = RhsResult.builder()
			.rhsAttr(1)
			.threshold(0.6)
			.from(0.8)
			.violations(Collections.emptyList())
			.validAndMinimal(false)
			.build();
		analyzer.analyze(Seq.of(
			new ValidationResult(new LhsResult(lhs, 2), Collections.singletonList(rhsResult)))
			.map(this::toTask)
			.toList());
		assertThat(found).isEmpty();
	}

	@Test
	public void testSupported() {
		Collection<SupportedMD> found = new ArrayList<>();
		Analyzer analyzer = createAnalyzer(found);
		int columnPairs = 4;
		MDSite lhs = new MDSiteImpl(columnPairs);
		MDElement rhs1 = new MDElementImpl(1, 0.8);
		int support = 2;
		RhsResult rhsResult1 = RhsResult.builder()
			.rhsAttr(1)
			.threshold(0.6)
			.from(0.8)
			.violations(Collections.emptyList())
			.validAndMinimal(true)
			.build();
		MDElement rhs2 = new MDElementImpl(2, 0.8);
		RhsResult rhsResult2 = RhsResult.builder()
			.rhsAttr(2)
			.threshold(0.6)
			.from(0.8)
			.violations(Collections.emptyList())
			.validAndMinimal(false)
			.build();
		doReturn(Collections.emptyList()).when(specializer).specialize(new MDImpl(lhs, rhs1));
		doReturn(Collections.emptyList()).when(specializer).specialize(new MDImpl(lhs, rhs2));
		analyzer.analyze(Seq.of(new ValidationResult(new LhsResult(lhs, support),
			Arrays.asList(rhsResult1, rhsResult2)))
			.map(this::toTask)
			.toList());
		assertThat(found).hasSize(1);
		assertThat(found)
			.contains(new SupportedMD(new MDImpl(lhs, new MDElementImpl(1, 0.6)), support));
	}

	private Analyzer createAnalyzer(Collection<SupportedMD> found) {
		InferHandler inferHandler = InferHandler.builder()
			.specializer(specializer)
			.fullLattice(fullLattice)
			.build();
		return AnalyzerImpl.builder()
			.minSupport(2)
			.inferHandler(inferHandler)
			.fullLattice(fullLattice)
			.consumer(found::add)
			.build();
	}

	private void testInfer(boolean validAndMinimal) {
		Collection<SupportedMD> found = new ArrayList<>();
		int columnPairs = 4;
		MDSite lhs = new MDSiteImpl(columnPairs).set(0, 0.5);
		MD inferred1 = new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.6),
			new MDElementImpl(1, 1.0));
		MD inferred2 = new MDImpl(new MDSiteImpl(columnPairs).set(1, 0.6),
			new MDElementImpl(1, 1.0));
		MD inferred3 = new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.6),
			new MDElementImpl(2, 1.0));
		MD inferred4 = new MDImpl(new MDSiteImpl(columnPairs).set(0, 0.6),
			new MDElementImpl(3, 1.0));
		doReturn(Optional.of(Mockito.mock(LatticeMD.class))).when(fullLattice)
			.addIfMinimalAndSupported(inferred2);
		doReturn(Optional.of(Mockito.mock(LatticeMD.class))).when(fullLattice)
			.addIfMinimalAndSupported(inferred1);
		doReturn(Optional.of(Mockito.mock(LatticeMD.class))).when(fullLattice)
			.addIfMinimalAndSupported(inferred3);
		doReturn(Optional.empty()).when(fullLattice).addIfMinimalAndSupported(inferred4);
		Analyzer analyzer = createAnalyzer(found);
		MDElement rhs1 = new MDElementImpl(1, 0.8);
		RhsResult rhsResult1 = RhsResult.builder()
			.rhsAttr(1)
			.threshold(0.6)
			.from(0.8)
			.violations(Collections.emptyList())
			.validAndMinimal(validAndMinimal)
			.build();
		MDElement rhs2 = new MDElementImpl(2, 0.8);
		RhsResult rhsResult2 = RhsResult.builder()
			.rhsAttr(2)
			.threshold(0.6)
			.from(0.8)
			.violations(Collections.emptyList())
			.validAndMinimal(validAndMinimal)
			.build();
		doReturn(Arrays.asList(inferred1, inferred2)).when(specializer).specialize(new MDImpl(lhs, rhs1));
		doReturn(Arrays.asList(inferred3, inferred4)).when(specializer).specialize(new MDImpl(lhs, rhs2));
		Statistics statistics = analyzer.analyze(Seq.of(
			new ValidationResult(new LhsResult(lhs, 2), Arrays.asList(rhsResult1, rhsResult2)))
			.map(this::toTask)
			.toList());
		assertThat(statistics.getNewDeduced()).isEqualTo(3);
	}

	private AnalyzeTask toTask(ValidationResult result) {
		return new AnalyzeTask(result, lowerer);
	}

}