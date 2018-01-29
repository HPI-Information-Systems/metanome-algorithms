package de.hpi.is.md.hybrid.impl.validation.arbitrary;

import com.codahale.metrics.annotation.Timed;
import de.hpi.is.md.hybrid.ValidationResult;
import de.hpi.is.md.hybrid.ValidationResult.LhsResult;
import de.hpi.is.md.hybrid.ValidationResult.RhsResult;
import de.hpi.is.md.hybrid.impl.LhsSelector;
import de.hpi.is.md.hybrid.impl.RecordGrouper;
import de.hpi.is.md.hybrid.impl.Selector;
import de.hpi.is.md.hybrid.impl.validation.RhsValidationTask;
import de.hpi.is.md.hybrid.impl.validation.ValidationTask;
import de.hpi.is.md.hybrid.md.MDSite;
import de.hpi.is.md.util.StreamUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.tuple.Tuple2;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
//@Metrics
class ArbitraryValidationTask implements ValidationTask {

	@NonNull
	private final LhsSelector intersector;
	@NonNull
	private final Collection<RhsValidationTask> rhs;
	@NonNull
	private final RecordGrouper grouper;
	@NonNull
	private final MDSite lhs;
	private final long minSupport;
	private long support = 0;

	@Override
	public ValidationResult validate() {
		//does order have an impact on runtime?
		Iterable<Tuple2<Selector, Collection<int[]>>> selectors = grouper.buildSelectors();
		for (Tuple2<Selector, Collection<int[]>> group : selectors) {
			Selector selector = group.v1();
			Collection<int[]> records = group.v2();
			support += validate(selector, records);
			if (isSupported() && allRejected()) {
				break;
			}
		}
		return collectResults();
	}

	private boolean allRejected() {
		return rhs.stream().noneMatch(RhsValidationTask::shouldUpdate);
	}

	private ValidationResult collectResults() {
		Collection<RhsResult> results = StreamUtils.seq(rhs)
			.map(RhsValidationTask::createResult)
			.toList();
		LhsResult lhsResult = new LhsResult(lhs, support);
		return new ValidationResult(lhsResult, results);
	}

	private boolean isSupported() {
		return support >= minSupport;
	}

	@Timed
	private long validate(Selector selector, Collection<int[]> left) {
		IntCollection rightMatches = intersector.findLhsMatches(selector);
//		log.trace("Validating {} left matches with {} right matches", left.size(),
//			rightMatches.size());
		rhs.forEach(rhsTask -> rhsTask.validate(left, rightMatches));
		return (long) left.size() * rightMatches.size();
	}
}
