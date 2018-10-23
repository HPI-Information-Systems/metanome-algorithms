package de.hpi.is.md.eval;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.hash.HashCode;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.demo.Runner;
import de.hpi.is.md.demo.Runner.MDConnection;
import de.hpi.is.md.jcommander.Application;
import de.hpi.is.md.jcommander.JCommanderRunner;
import de.hpi.is.md.util.CollectionUtils;
import de.hpi.is.md.util.Reporter;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class Evaluator implements Application {

	@Parameter(names = "--query2")
	private String query2;
	@Parameter(names = "--query", required = true)
	private String query;
	@ParametersDelegate
	private Runner runner = new Runner();
	@Parameter(names = {"--reportTo"})
	private File reportDirectory = new File("report/");

	public static void main(String[] args) {
		Application app = new Evaluator();
		JCommanderRunner.create(app)
			.build()
			.run(args);
	}

	@Override
	public void run() {
		try (MDConnection connection = runner.get()) {
			WithResults results = new WithResults();
			connection.register(results::add);
			with(connection).run()
				.ifPresent(results::evaluate);
		} catch (SQLException e) {
			log.warn("Error closing connection", e);
		}
	}

	private WithMDConnection with(MDConnection connection) {
		return new WithMDConnection(connection);
	}

	@RequiredArgsConstructor
	private class WithMDConnection {

		private final MDConnection connection;

		private Optional<HashCode> run() {
			return Optional.ofNullable(query2)
				.map(this::runWithTwoQueries)
				.orElseGet(this::runWithOneQuery);
		}

		private Optional<HashCode> runWithOneQuery() {
			return connection.run(query);
		}

		private Optional<HashCode> runWithTwoQueries(String q2) {
			return connection.run(query, q2);
		}

	}

	private class WithResults {

		private final Collection<MatchingDependencyResult> results = new ArrayList<>();

		private void add(MatchingDependencyResult result) {
			results.add(result);
		}

		private void evaluate(HashCode hash) {
			Collection<String> stringResults = CollectionUtils.toString(results);
			EvaluationTask task = EvaluationTask.builder()
				.hash(hash)
				.result(stringResults)
				.build();
			task.evaluateOrCreate();
			report(hash);
		}

		private void report(HashCode hash) {
			File directory = new File(reportDirectory, hash.toString());
			Reporter.reportTo(directory);
			Reporter.reportTo(log);
		}

	}

}
