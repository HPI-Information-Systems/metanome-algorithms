package de.hpi.is.md.demo;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParametersDelegate;
import com.fasterxml.jackson.databind.ObjectReader;
import com.google.common.hash.HashCode;
import de.hpi.is.md.Discoverer;
import de.hpi.is.md.MatchingDependencyResult;
import de.hpi.is.md.config.MappingConfiguration;
import de.hpi.is.md.hybrid.DiscoveryConfiguration;
import de.hpi.is.md.hybrid.HybridDiscoverer;
import de.hpi.is.md.hybrid.HybridEnforcerBuilder;
import de.hpi.is.md.hybrid.MDMapping;
import de.hpi.is.md.hybrid.impl.sim.threshold.CollectingThresholdMap;
import de.hpi.is.md.jcommander.JCommanderJdbcConfiguration;
import de.hpi.is.md.relational.Relation;
import de.hpi.is.md.relational.jdbc.ResultSetRelation;
import de.hpi.is.md.result.AbstractResultEmitter;
import de.hpi.is.md.util.enforce.EnforcerBuilder;
import de.hpi.is.md.util.enforce.MDEnforcer;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Runner {

	@Parameter(names = {"--parallel"})
	private boolean parallel = false;
	@Parameter(names = "--store")
	private boolean store = false;
	@Parameter(names = "--cache")
	private boolean useCache = false;
	@Parameter(names = "--config", required = true)
	private File config;
	@ParametersDelegate
	private JCommanderJdbcConfiguration jdbcConfiguration = new JCommanderJdbcConfiguration();

	public MDConnection get() {
		configureCaches();
		try {
			Connection connection = jdbcConfiguration.createConnection();
			return with(connection);
		} catch (SQLException e) {
			String message = "Error creating connection to " + jdbcConfiguration.getUrl();
			throw new RuntimeException(message, e);
		} catch (ClassNotFoundException e) {
			String message = "Driver class not found " + jdbcConfiguration.getDriverName();
			throw new RuntimeException(message, e);
		}
	}

	private void configureCaches() {
		CollectingThresholdMap.useCache(useCache);
	}

	private RunnerConfiguration createConfig() throws IOException {
		ObjectReader reader = Jackson.createReader(RunnerConfiguration.class);
		return reader.readValue(config);
	}

	private MDConnection with(Connection connection) {
		try {
			RunnerConfiguration configuration = createConfig();
			return new MDConnection(connection, configuration);
		} catch (IOException e) {
			throw new RuntimeException("Error reading configuration", e);
		}
	}

	@RequiredArgsConstructor
	public class MDConnection extends AbstractResultEmitter<MatchingDependencyResult> implements
		AutoCloseable {

		@NonNull
		private final Connection connection;
		@NonNull
		private final RunnerConfiguration configuration;

		@Override
		public void close() throws SQLException {
			connection.close();
		}

		public Optional<MDEnforcer> createEnforcer(String query1, String query2) {
			try (Statement statement = connection.createStatement()) {
				Relation relation1 = new ResultSetRelation(statement, query1);
				Relation relation2 = new ResultSetRelation(statement, query2);
				MappingConfiguration mapper = configuration.getMapping();
				MDMapping mappings = mapper.createMapping(relation1, relation2);
				MDEnforcer enforcer = createEnforcerBuilder(mappings)
					.create(relation1, relation2);
				return Optional.ofNullable(enforcer);
			} catch (SQLException e) {
				log.warn("Error executing queries", e);
				return Optional.empty();
			}
		}

		public Optional<MDEnforcer> createEnforcer(String query) {
			try (Statement statement = connection.createStatement()) {
				Relation relation = new ResultSetRelation(statement, query);
				MappingConfiguration mapper = configuration.getMapping();
				MDMapping mappings = mapper.createMapping(relation);
				MDEnforcer enforcer = createEnforcerBuilder(mappings)
					.create(relation);
				return Optional.ofNullable(enforcer);
			} catch (SQLException e) {
				log.warn("Error executing queries", e);
				return Optional.empty();
			}
		}

		public Optional<HashCode> run(String query1, String query2) {
			try (Statement statement = connection.createStatement()) {
				Relation relation1 = new ResultSetRelation(statement, query1);
				Relation relation2 = new ResultSetRelation(statement, query2);
				HashCode hash = discover(relation1, relation2);
				return Optional.of(hash);
			} catch (SQLException e) {
				log.warn("Error executing queries", e);
				return Optional.empty();
			}
		}

		public Optional<HashCode> run(String query) {
			try (Statement statement = connection.createStatement()) {
				Relation relation = new ResultSetRelation(statement, query);
				HashCode hash = discover(relation);
				return Optional.of(hash);
			} catch (SQLException e) {
				log.warn("Error executing query '" + query + "'", e);
				return Optional.empty();
			}
		}

		private Discoverer createDiscoverer(MDMapping mappings) {
			DiscoveryConfiguration configuration = this.configuration.getDiscovery();
			Discoverer discoverer = HybridDiscoverer.builder()
				.configuration(configuration)
				.mappings(mappings)
				.parallel(parallel)
				.store(store)
				.build();
			discoverer.register(this::emitResult);
			return discoverer;
		}

		private EnforcerBuilder createEnforcerBuilder(MDMapping mappings) {
			return HybridEnforcerBuilder.builder()
				.mappings(mappings)
				.store(store)
				.build();
		}

		private HashCode discover(Relation left, Relation right) {
			MappingConfiguration mapper = configuration.getMapping();
			MDMapping mapping = mapper.createMapping(left, right);
			Discoverer discoverer = createDiscoverer(mapping);
			discoverer.discover(left, right);
			return hash(left, right, mapping);
		}

		private HashCode discover(Relation relation) {
			MappingConfiguration mapper = configuration.getMapping();
			MDMapping mapping = mapper.createMapping(relation);
			Discoverer discoverer = createDiscoverer(mapping);
			discoverer.discover(relation);
			return hash(relation, relation, mapping);
		}

		private HashCode hash(Relation left, Relation right, MDMapping mapping) {
			DiscoveryConfiguration configuration = this.configuration.getDiscovery();
			return configuration.hash(left, right, mapping);
		}

	}


}
