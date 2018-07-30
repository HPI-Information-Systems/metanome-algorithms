package de.hpi.is.md.demo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.hash.HashCode;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.demo.Runner.MDConnection;
import de.hpi.is.md.demo.input.IOProvider;
import de.hpi.is.md.demo.input.IOReceiver;
import de.hpi.is.md.demo.input.InputLooper;
import de.hpi.is.md.impl.result.FileResultWriter;
import de.hpi.is.md.jcommander.Application;
import de.hpi.is.md.jcommander.JCommanderRunner;
import de.hpi.is.md.result.ResultListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal", "FieldCanBeLocal", "UseOfSystemOutOrSystemErr"})
@Slf4j
public class MDDemo implements Application {

	private static final String END_KEYWORD = "n";
	private static final String CONTINUE_KEYWORD = "y";
	@Parameter(names = "--query2")
	private String query2;
	@Parameter(names = "--query", required = true)
	private String query;
	@Parameter(names = {"-i", "--interactive"})
	private boolean interactive = false;
	@ParametersDelegate
	private Runner runner = new Runner();
	@Parameter(names = "--out")
	private File file = new File("results.txt");

	public static void main(String[] args) {
		Application app = new MDDemo();
		JCommanderRunner.create(app)
			.build()
			.run(args);
	}

	private static ResultProcessor with(Collection<MatchingDependencyResult> results) {
		return new ResultProcessor(results);
	}

	@Override
	public void run() {
		try (MDConnection connection = runner.get()) {
			with(connection).run();
		} catch (SQLException e) {
			log.warn("Error closing connection", e);
		}
	}

	private QueryReceiver with(MDConnection connection) {
		return new QueryReceiver(connection);
	}

	@RequiredArgsConstructor
	private class QueryReceiver implements IOReceiver {

		@NonNull
		private final MDConnection connection;

		@Override
		public String getContinueKeyword() {
			return CONTINUE_KEYWORD;
		}

		@Override
		public String getEndKeyword() {
			return END_KEYWORD;
		}

		@Override
		public void runOnce(IOProvider provider) {
			Optional<String> query = provider.readLine("Type a query:");
			provider.writeLine("Executing...");
			query.ifPresent(this::runWithOneQuery);
		}

		private Collection<MatchingDependencyResult> registerListeners(
			ResultListener<MatchingDependencyResult> writer) {
			Collection<MatchingDependencyResult> results = new ArrayList<>();
			connection.unregisterAll();
			connection.register(writer);
			connection.register(results::add);
			connection.register(System.out::println);
			return results;
		}

		private void run() {
			if (interactive) {
				InputLooper.loop(this);
			} else {
				runWithQueries();
			}
		}

		private Optional<HashCode> runWithOneQuery(String q) {
			try (FileResultWriter<MatchingDependencyResult> writer = new FileResultWriter<>(file)) {
				Collection<MatchingDependencyResult> results = registerListeners(writer);
				Optional<HashCode> result = connection.run(q);
				with(results).finish();
				return result;
			} catch (IOException e) {
				log.warn("Error creating FileWriter", e);
			}
			return Optional.empty();
		}

		private Optional<HashCode> runWithOneQuery() {
			return runWithOneQuery(query);
		}

		private void runWithQueries() {
			Optional.ofNullable(query2)
				.map(this::runWithTwoQueries)
				.orElseGet(this::runWithOneQuery);
		}

		private Optional<HashCode> runWithTwoQueries(String q2) {
			try (FileResultWriter<MatchingDependencyResult> writer = new FileResultWriter<>(file)) {
				Collection<MatchingDependencyResult> results = registerListeners(writer);
				Optional<HashCode> result = connection.run(query, q2);
				with(results).finish();
				return result;
			} catch (IOException e) {
				log.warn("Error creating FileWriter", e);
			}
			return Optional.empty();
		}
	}

}
