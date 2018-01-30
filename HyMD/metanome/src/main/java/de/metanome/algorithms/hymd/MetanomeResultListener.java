package de.metanome.algorithms.hymd;

import de.hpi.is.md.ColumnMapping;
import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.ColumnPair;
import de.hpi.is.md.result.ResultListener;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.MatchingCombination;
import de.metanome.algorithm_integration.MatchingIdentifier;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.MatchingDependencyResultReceiver;
import de.metanome.algorithm_integration.results.MatchingDependency;
import java.util.Collection;
import org.jooq.lambda.Seq;

public class MetanomeResultListener implements ResultListener<MatchingDependencyResult> {

	private final MatchingDependencyResultReceiver resultReceiver;

	MetanomeResultListener(MatchingDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void receiveResult(MatchingDependencyResult result) {
		MatchingDependency md = transform(result);
		try {
			System.out.println("received result: " + result);
			resultReceiver.receiveResult(md);
			System.out.println("sent result to " + resultReceiver);
		} catch (CouldNotReceiveResultException | ColumnNameMismatchException e) {
			e.printStackTrace();
		}
	}

	private MatchingIdentifier getDependant(de.hpi.is.md.MatchingDependency md) {
		ColumnMatchWithThreshold<?> rhs = md.getRhs();
		return toIdentifier(rhs);
	}

	private MatchingCombination getDeterminant(de.hpi.is.md.MatchingDependency md) {
		Collection<ColumnMatchWithThreshold<?>> lhs = md.getLhs();
		MatchingIdentifier[] matchingIdentifiers = Seq.seq(lhs)
			.map(this::toIdentifier)
			.toArray(MatchingIdentifier[]::new);
		return new MatchingCombination(matchingIdentifiers);
	}

	private ColumnIdentifier toIdentifier(Column<?> column) {
		String tableName = column.getTableName().orElse("");
		String name = column.getName();
		return new ColumnIdentifier(tableName, name);
	}

	private MatchingIdentifier toIdentifier(ColumnMatchWithThreshold<?> matchWithThreshold) {
		ColumnMapping<?> match = matchWithThreshold.getMatch();
		ColumnPair<?> columns = match.getColumns();
		ColumnIdentifier leftIdentifier = toIdentifier(columns.getLeft());
		ColumnIdentifier rightIdentifier = toIdentifier(columns.getRight());
		String similarityMeasure = match.getSimilarityMeasure().toString();
		double threshold = matchWithThreshold.getThreshold();
		return new MatchingIdentifier(leftIdentifier, rightIdentifier, similarityMeasure,
			threshold);
	}

	private MatchingDependency transform(MatchingDependencyResult result) {
		de.hpi.is.md.MatchingDependency md = result.getDependency();
		MatchingCombination determinant = getDeterminant(md);
		MatchingIdentifier dependant = getDependant(md);
		long support = result.getSupport();
		return new MatchingDependency(determinant, dependant, support);
	}
}
