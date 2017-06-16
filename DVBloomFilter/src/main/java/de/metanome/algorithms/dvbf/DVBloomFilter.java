package de.metanome.algorithms.dvbf;


import java.util.ArrayList;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.BasicStatisticsAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.ListBoxParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementListBox;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
/**
*
*@author Hazar.Harmouch
*Referances:
*S. J. Swamidass and P. Baldi. Mathematical correction for ngerprint similarity measures to improve chemical retrieval. Journal of chemical information and modeling, 47(3):952-964, 2007
*O. Papapetrou, W. Siberski, and W. Nejdl. Cardinality estimation and dynamic length adaptation for Bloom filters. Distributed and Parallel Databases, 28(2):119{156, 2010
*
*/

public class DVBloomFilter extends DVBloomFilterAlgorithm implements BasicStatisticsAlgorithm,
    RelationalInputParameterAlgorithm, ListBoxParameterAlgorithm, IntegerParameterAlgorithm {


  String APROACH1 = "Swamidass, S. J., & Baldi, P. (2007)";
  String APROACH2 = "Papapetrou, O., Siberski, W., & Nejdl, W. (2010)";

  public enum Identifier {
    INPUT_GENERATOR, Num_bits_per_element, APPROACH
  };

  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
    ArrayList<ConfigurationRequirement<?>> conf = new ArrayList<>();

    conf.add(new ConfigurationRequirementRelationalInput(
        DVBloomFilter.Identifier.INPUT_GENERATOR.name()));


    ConfigurationRequirementInteger bits =
        new ConfigurationRequirementInteger(DVBloomFilter.Identifier.Num_bits_per_element.name());
    bits.setRequired(false);
    Integer[] defaults = {4};
    bits.setDefaultValues(defaults);
    conf.add(bits);


    ArrayList<String> methods = new ArrayList<>();
    methods.add(APROACH1);
    methods.add(APROACH2);

    ConfigurationRequirementListBox approach =
        new ConfigurationRequirementListBox(DVBloomFilter.Identifier.APPROACH.name(), methods);

    approach.setRequired(false);
    conf.add(approach);

    // conf.add(new
    // ConfigurationRequirementRelationalInput(MyIndDetector.Identifier.INPUT_GENERATOR.name(),
    // ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES)); // For IND discovery, the number of
    // inputs is arbitrary
    // conf.add(new
    // ConfigurationRequirementInteger(MyOdDetector.Identifier.IMPORTANT_PARAMETER.name())); // The
    // algorithm can ask the user for other parameters if needed. If so, then implement the
    // Integer/String/BooleanParameterAlgorithm interfaces as well
    return conf;
  }

  @Override
  public void setResultReceiver(BasicStatisticsResultReceiver resultReceiver) {
    this.resultReceiver = resultReceiver;
  }

  @Override
  public void setRelationalInputConfigurationValue(String identifier,
      RelationalInputGenerator... values) throws AlgorithmConfigurationException {
    if (!DVBloomFilter.Identifier.INPUT_GENERATOR.name().equals(identifier))
      throw new AlgorithmConfigurationException(
          "Input generator does not match the expected identifier: " + identifier + " (given) but "
              + DVBloomFilter.Identifier.INPUT_GENERATOR.name() + " (expected)");
    this.inputGenerator = values[0];
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    super.execute();
  }

  @Override
  public void setIntegerConfigurationValue(String identifier, Integer... values)
      throws AlgorithmConfigurationException {
    if (DVBloomFilter.Identifier.Num_bits_per_element.name().equals(identifier))
      if (values != null && !values[0].equals("")) {
        try {
          int bits = values[0];
          if (bits > 0 && bits < 15)
            this.bitperelement = bits;
          else
            throw new Exception();
        } catch (Exception ex) {
          throw new AlgorithmConfigurationException(
              "number of bits per element should be a positive double in (1, 8) range");
        }

      }


  }

  @Override
  public String getAuthors() {

    return "Hazar Harmouch";
  }

  @Override
  public String getDescription() {
    return "Bloom filters";
  }

  @Override
  public void setListBoxConfigurationValue(String identifier, String... values)
      throws AlgorithmConfigurationException {
    if (DVBloomFilter.Identifier.APPROACH.name().equals(identifier)) {
      if (values != null && !values[0].equals("")) {
        try {
          if (values[0].equals(APROACH1))
            this.approache = 0;
          else if (values[0].equals(APROACH2))
            this.approache = 1;
          else
            throw new Exception();
        } catch (Exception ex) {
          throw new AlgorithmConfigurationException("please choose an approach");
        }

      }
    }
  }



}
