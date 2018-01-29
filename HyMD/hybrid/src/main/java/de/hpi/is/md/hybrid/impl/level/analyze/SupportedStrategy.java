package de.hpi.is.md.hybrid.impl.level.analyze;

import de.hpi.is.md.hybrid.MDUtil;
import de.hpi.is.md.hybrid.SupportedMD;
import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.level.Statistics;
import de.hpi.is.md.hybrid.impl.md.MDImpl;
import de.hpi.is.md.hybrid.md.MD;
import de.hpi.is.md.hybrid.md.MDElement;
import de.hpi.is.md.hybrid.md.MDSite;
import java.util.function.Consumer;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
class SupportedStrategy implements AnalyzeStrategy {

	@Getter
	private final Statistics statistics = new Statistics();
	private final long support;
	@NonNull
	private final MDSite lhs;
	@NonNull
	private final Consumer<SupportedMD> consumer;
	@NonNull
	private final InferHandler inferHandler;

	public static Factory factory(Consumer<SupportedMD> consumer, InferHandler inferHandler) {
		return new FactoryImpl(consumer, inferHandler);
	}

	@Override
	public void deduce(RhsResult rhsResult) {
		statistics.validated();
		MD md = toActualMd(rhsResult);
		if (rhsResult.isValidAndMinimal() && isNonTrivial(rhsResult)) {
			deduceValid(md);
		} else {
			statistics.invalid();
		}
		MD originalMd = toOriginalMd(rhsResult);
		Statistics newStatistics = inferHandler.infer(originalMd);
		statistics.add(newStatistics);
	}

	private MD toOriginalMd(RhsResult rhsResult) {
		MDElement rhs = rhsResult.getOriginal();
		return toMd(rhs);
	}

	private void deduceValid(MD md) {
		SupportedMD supportedMd = new SupportedMD(md, support);
		// we found a minimal md matching all requirements
		statistics.found();
		log.info("Found minimal MD {}", md);
		consumer.accept(supportedMd);
	}

	private boolean isNonTrivial(MDElement rhs) {
		return MDUtil.isNonTrivial(lhs, rhs);
	}

	private boolean isNonTrivial(RhsResult rhsResult) {
		MDElement rhs = rhsResult.getActual();
		return isNonTrivial(rhs);
	}

	private MD toMd(MDElement rhs) {
		return new MDImpl(lhs, rhs);
	}

	private MD toActualMd(RhsResult rhsResult) {
		MDElement rhs = rhsResult.getActual();
		return toMd(rhs);
	}

	@RequiredArgsConstructor
	private static class FactoryImpl implements Factory {

		@NonNull
		private final Consumer<SupportedMD> consumer;
		@NonNull
		private final InferHandler inferHandler;

		@Override
		public AnalyzeStrategy create(LhsResult lhsResult) {
			MDSite lhs = lhsResult.getLhs();
			long support = lhsResult.getSupport();
			return SupportedStrategy.builder()
				.lhs(lhs)
				.support(support)
				.consumer(consumer)
				.inferHandler(inferHandler)
				.build();
		}
	}
}
