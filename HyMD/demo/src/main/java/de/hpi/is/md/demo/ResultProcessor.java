package de.hpi.is.md.demo;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.MatchingDependency;
import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.util.StreamUtils;
import de.hpi.is.md.util.StringUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class ResultProcessor {

	private final Collection<MatchingDependencyResult> results;

	private static ColumnMapping<?> getRhs(MatchingDependencyResult result) {
		MatchingDependency matchingDependency = result.getDependency();
		ColumnMatchWithThreshold<?> rhs = matchingDependency.getRhs();
		return rhs.getMatch();
	}

	private static void print(MatchingDependencyResult result) {
		Collection<Object> values = new ArrayList<>();
		values.add(Long.valueOf(result.getSupport()));
		MatchingDependency md = result.getDependency();
		ColumnMatchWithThreshold<?> rhs = md.getRhs();
		values.add(rhs.getMatch());
		values.add(Double.valueOf(rhs.getThreshold()));
		values.addAll(md.getLhs());
		System.out.println(StringUtils.join("\t", values));
	}

	void finish() {
		System.out.println("Found " + results.size() + " matching dependencies");
		printGrouped();
	}

	private static void printGroup(Iterable<MatchingDependencyResult> mds) {
		mds.forEach(ResultProcessor::print);
	}

	private Iterable<List<MatchingDependencyResult>> groupResults() {
		Map<ColumnMapping<?>, List<MatchingDependencyResult>> grouped = StreamUtils.seq(results)
			.groupBy(ResultProcessor::getRhs);
		return grouped.values();
	}

	private void printGrouped() {
		Iterable<List<MatchingDependencyResult>> groups = groupResults();
		groups.forEach(ResultProcessor::printGroup);
	}

}
