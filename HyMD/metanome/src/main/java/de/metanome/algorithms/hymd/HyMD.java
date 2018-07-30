package de.metanome.algorithms.hymd;

import com.bakdata.util.jackson.CPSTypeIdResolver;
import com.fasterxml.jackson.databind.ObjectReader;
import de.hpi.is.md.Discoverer;
import de.hpi.is.md.SupportCalculator;
import de.hpi.is.md.config.MappingConfiguration;
import de.hpi.is.md.hybrid.DiscoveryConfiguration;
import de.hpi.is.md.hybrid.HybridDiscoverer;
import de.hpi.is.md.hybrid.HybridDiscoverer.HybridDiscovererBuilder;
import de.hpi.is.md.hybrid.MDMapping;
import de.hpi.is.md.impl.SizeBasedSupportCalculator;
import de.hpi.is.md.relational.Relation;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.MatchingDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.MatchingDependencyResultReceiver;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class HyMD implements MatchingDependencyAlgorithm,
    RelationalInputParameterAlgorithm, StringParameterAlgorithm, IntegerParameterAlgorithm {

  static {
    CPSTypeIdResolver.addClassLoader(HyMD.class.getClassLoader());
  }

  private MatchingDependencyResultReceiver resultReceiver;
  private Relation leftRelation;
  private Relation rightRelation;
  private double minThreshold = 0.7;
  private SupportCalculator supportCalculator = new SizeBasedSupportCalculator();
  private MappingConfiguration mappingConfiguration = new MappingConfiguration();

  private static ConfigurationRequirement<?> createMinThresholdConfigurationRequirement() {
    ConfigurationRequirementString config = new ConfigurationRequirementString(
        Identifier.THRESHOLD.name());
    config.setRequired(false);
    config.setDefaultValues(new String[]{Double.toString(0.7)});
    return config;
  }

  private static ConfigurationRequirement<?> createRelationConfigurationRequirement() {
    ConfigurationRequirementRelationalInput config = new ConfigurationRequirementRelationalInput(
        Identifier.RELATION.name());
    config.setRequired(true);
    return config;
  }

  private static ConfigurationRequirement<?> createMappingConfigurationRequirement() {
    ConfigurationRequirementString config = new ConfigurationRequirementString(
        Identifier.CONFIG.name());
    config.setRequired(false);
    config.setDefaultValues(new String[]{"{}"});
    return config;
  }

  private static ConfigurationRequirement<?> createSupportConfigurationRequirement() {
    ConfigurationRequirementInteger config = new ConfigurationRequirementInteger(
        Identifier.SUPPORT.name());
    config.setRequired(false);
    config.setDefaultValues(new Integer[]{0});
    return config;
  }

  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
    ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<>();

    configs.add(createMinThresholdConfigurationRequirement());

    configs.add(createRelationConfigurationRequirement());

    configs.add(createMappingConfigurationRequirement());

    configs.add(createSupportConfigurationRequirement());

    return configs;
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    if (leftRelation == null) {
      throw new AlgorithmConfigurationException("At least one relation needed");
    }
    if (rightRelation == null) {
      MDMapping mappings = mappingConfiguration.createMapping(leftRelation);
      Discoverer discoverer = createDiscoverer(mappings);
      discoverer.discover(leftRelation);
    } else {
      MDMapping mappings = mappingConfiguration.createMapping(leftRelation, rightRelation);
      Discoverer discoverer = createDiscoverer(mappings);
      discoverer.discover(leftRelation, rightRelation);
    }
  }

  @Override
  public String getAuthors() {
    return "Philipp Schirmer";
  }

  @Override
  public String getDescription() {
    return "Discover Matching Dependencies";
  }

  @Override
  public void setRelationalInputConfigurationValue(String identifier,
      RelationalInputGenerator... values) throws AlgorithmConfigurationException {
    if (Identifier.RELATION.name().equals(identifier)) {
      if (values.length == 0 || values.length > 2) {
        throw new AlgorithmConfigurationException(
            "MD Discovery expects one or two relational inputs");
      }
      this.leftRelation = wrap(values[0]);
      if (values.length == 2) {
        this.rightRelation = wrap(values[1]);
      } else {
        this.rightRelation = null;
      }
    }
  }

  @Override
  public void setResultReceiver(MatchingDependencyResultReceiver resultReceiver) {
    this.resultReceiver = resultReceiver;
  }

  @Override
  public void setStringConfigurationValue(String identifier, String... values)
      throws AlgorithmConfigurationException {
    if (Identifier.THRESHOLD.name().equals(identifier)) {
      if (values.length > 0) {
        String value = values[0];
        try {
          this.minThreshold = Double.parseDouble(value);
        } catch (NumberFormatException e) {
          throw new AlgorithmConfigurationException("Min threshold is not valid", e);
        }
      } else {
        throw new AlgorithmConfigurationException("Min threshold not provided");
      }
    }
    if (Identifier.CONFIG.name().equals(identifier)) {
      if (values.length > 0) {
        String json = values[0];
        this.mappingConfiguration = readConfig(json);
      } else {
        throw new AlgorithmConfigurationException("Config file not provided");
      }
    }
  }

  private Discoverer buildDiscoverer(MDMapping mappings) {
    DiscoveryConfiguration configuration = createConfiguration();
    return newBuilder()
        .mappings(mappings)
        .configuration(configuration)
        .build();
  }

  private DiscoveryConfiguration createConfiguration() {
    DiscoveryConfiguration configuration = new DiscoveryConfiguration();
    configuration.setMinThreshold(minThreshold);
    configuration.setSupportCalculator(supportCalculator);
    return configuration;
  }

  private Discoverer createDiscoverer(MDMapping mappings) {
    Discoverer discoverer = buildDiscoverer(mappings);
    Optional.ofNullable(resultReceiver)
        .map(MetanomeResultListener::new)
        .ifPresent(discoverer::register);
    return discoverer;
  }

  private HybridDiscovererBuilder newBuilder() {
    return HybridDiscoverer.builder()
        .store(false)
        .parallel(true);
  }

  private Relation wrap(RelationalInputGenerator input) {
    return new MetanomeRelation(input);
  }

  private MappingConfiguration readConfig(String json) throws AlgorithmConfigurationException {
    try {
      ObjectReader reader = Jackson.createReader(MappingConfiguration.class);
      return reader.readValue(json);
    } catch (IOException e) {
      throw new AlgorithmConfigurationException("Error parsing json config", e);
    }
  }

  @Override
  public void setIntegerConfigurationValue(String identifier, Integer... values)
      throws AlgorithmConfigurationException {
    if (Identifier.SUPPORT.name().equals(identifier)) {
      if (values.length > 0) {
        int support = values[0];
        this.supportCalculator = createSupportCalculator(support);
      } else {
        throw new AlgorithmConfigurationException("Support not provided");
      }
    }
  }

  private SupportCalculator createSupportCalculator(int support) {
    SizeBasedSupportCalculator supportCalculator = new SizeBasedSupportCalculator();
    supportCalculator.setNonReflexiveMatches(support);
    return supportCalculator;
  }

  public enum Identifier {
    THRESHOLD, RELATION, CONFIG, SUPPORT
  }
}
