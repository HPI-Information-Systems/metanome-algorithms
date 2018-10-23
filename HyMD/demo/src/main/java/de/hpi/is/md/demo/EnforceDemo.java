package de.hpi.is.md.demo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.google.common.io.Files;
import de.hpi.is.md.MatchingDependency.ColumnMatchWithThreshold;
import de.hpi.is.md.demo.Runner.MDConnection;
import de.hpi.is.md.jcommander.Application;
import de.hpi.is.md.jcommander.JCommanderRunner;
import de.hpi.is.md.util.Differ;
import de.hpi.is.md.util.Differ.DiffResult;
import de.hpi.is.md.util.StreamUtils;
import de.hpi.is.md.util.enforce.EnforceMatch;
import de.hpi.is.md.util.enforce.MDEnforcer;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Seq;


@Slf4j
public class EnforceDemo implements Application {

	private static final Charset CHARSET = Charset.defaultCharset();
	@Parameter(names = "--file", required = true)
	private File mdFile;
	@Parameter(names = "--query2")
	private String query2;
	@Parameter(names = "--query", required = true)
	private String query;
	@ParametersDelegate
	private Runner runner = new Runner();
	@Parameter(names = "--gold", required = true)
	private File goldStandard;
	@Parameter(names = "--leftId")
	private int leftId = 0;
	@Parameter(names = "--rightId")
	private int rightId = 0;

	public static void main(String[] args) {
		Application app = new EnforceDemo();
		JCommanderRunner.create(app)
			.build()
			.run(args);
	}

	private static String toMatchEntry(Object v1, Object v2) {
		return v1 + ";" + v2;
	}

	@Override
	public void run() {
		try (MDConnection connection = runner.get()) {
			with(connection).run()
				.ifPresent(System.out::println);
		} catch (SQLException e) {
			log.warn("Error closing connection", e);
		}
	}

	private Collection<Collection<ColumnMatchWithThreshold<?>>> getRules() {
		try {
			List<String> mds = Files.readLines(mdFile, CHARSET);
			return StreamUtils.seq(mds)
				.map(MDParser::parseMd)
				.toList();
		} catch (IOException e) {
			log.error("Error reading MDs", e);
			return Collections.emptyList();
		}
	}

	private WithEnforcer with(MDEnforcer enforcer) {
		return new WithEnforcer(enforcer);
	}

	private WithMDConnection with(MDConnection connection) {
		return new WithMDConnection(connection);
	}

	@RequiredArgsConstructor
	private class WithEnforcer {

		@NonNull
		private final MDEnforcer enforcer;

		private Optional<ContingencyTable> enforce() {
			Collection<Collection<ColumnMatchWithThreshold<?>>> rules = getRules();
			return enforceAll(rules);
		}

		private Collection<String> enforce(Collection<ColumnMatchWithThreshold<?>> lhs) {
			Collection<EnforceMatch> matches = enforcer.enforce(lhs);
			return StreamUtils.seq(matches)
				.map(this::toString)
				.flatMap(Collection::stream)
				.toList();
		}

		private Optional<ContingencyTable> enforceAll(
			Iterable<Collection<ColumnMatchWithThreshold<?>>> rules) {
			Collection<String> matches = StreamUtils.seq(rules)
				.map(this::enforce)
				.flatMap(Collection::stream)
				.toSet();
			log.info("Enforced lhs");
			return getQuality(matches);
		}

		@SuppressWarnings("boxing")
		private Collection<String> toString(EnforceMatch enforceMatch) {
			Seq<Integer> left = StreamUtils.seq(enforceMatch.getLeft())
				.map(o -> o[leftId])
				.map(Objects::toString)
				.map(Integer::parseInt);
			Seq<Integer> right = StreamUtils.seq(enforceMatch.getRight())
				.map(o -> o[rightId])
				.map(Objects::toString)
				.map(Integer::parseInt);
			return left.crossJoin(right)
				.filter(t -> t.map((v1, v2) -> v1 < v2))
				.map(t -> t.map(EnforceDemo::toMatchEntry))
				.toList();
		}

		private Optional<ContingencyTable> getQuality(Collection<String> matches) {
			try {
				List<String> gs = Files.readLines(goldStandard, CHARSET);
				log.info("Read gold standard");
				return getQuality(gs, matches);
			} catch (IOException e) {
				log.warn("Error reading gold standard");
				return Optional.empty();
			}
		}

		private Optional<ContingencyTable> getQuality(Collection<String> gs,
			Collection<String> matches) {
			DiffResult<String> diff = Differ.diff(gs, matches);
			int tp = diff.getCommon().size();
			int fn = diff.getOnlyA().size();
			int fp = diff.getOnlyB().size();
			return Optional.of(ContingencyTable.create(tp, fn, fp));
		}
	}

	@RequiredArgsConstructor
	private class WithMDConnection {

		@NonNull
		private final MDConnection connection;

		private Optional<ContingencyTable> enforce(MDEnforcer enforcer) {
			return with(enforcer).enforce();
		}

		private Optional<ContingencyTable> run() {
			return Optional.ofNullable(query2)
				.map(this::runWithTwoQueries)
				.orElseGet(this::runWithOneQuery);
		}

		private Optional<ContingencyTable> runWithOneQuery() {
			return connection.createEnforcer(query)
				.flatMap(this::enforce);
		}

		private Optional<ContingencyTable> runWithTwoQueries(String q2) {
			return connection.createEnforcer(query, q2)
				.flatMap(this::enforce);
		}
	}

}
