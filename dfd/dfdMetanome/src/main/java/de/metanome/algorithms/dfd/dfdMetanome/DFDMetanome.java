package de.metanome.algorithms.dfd.dfdMetanome;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fdiscovery.approach.runner.DFDMiner;
import fdiscovery.columns.ColumnCollection;
import fdiscovery.general.FunctionalDependencies;
import fdiscovery.preprocessing.SVFileProcessor;

public class DFDMetanome implements FunctionalDependencyAlgorithm,
                                   FileInputParameterAlgorithm {

  private FunctionalDependencyResultReceiver resultReceiver;

  private int numberColumns;

  private int numberRows;
  private FileInputGenerator[] fileInputGenerators;
  String indentifier;

  public enum Identifier {INPUT_GENERATOR}

  @Override
  public void setFileInputConfigurationValue(String identifier,
                                             FileInputGenerator... fileInputGenerators)
      throws AlgorithmConfigurationException {
    if (DFDMetanome.Identifier.INPUT_GENERATOR.name().equals(identifier)) {
      this.fileInputGenerators = fileInputGenerators;
      this.indentifier = identifier;
    }

  }

  @Override
  public void setResultReceiver(
      FunctionalDependencyResultReceiver functionalDependencyResultReceiver) {
    this.resultReceiver = functionalDependencyResultReceiver;
  }


  @Override
  public ArrayList<ConfigurationRequirement> getConfigurationRequirements() {
    ArrayList<ConfigurationRequirement> configs = new ArrayList<ConfigurationRequirement>(1);
    configs.add(new ConfigurationRequirementFileInput(DFDMetanome.Identifier.INPUT_GENERATOR.name(),
                                                      ConfigurationRequirement.ARBITRARY_NUMBER_OF_VALUES));
    return configs;
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    for (FileInputGenerator fileInput : fileInputGenerators) {

      String filePath = fileInput.getInputFile().getAbsolutePath();
      File source = new File(filePath);
      SVFileProcessor inputFileProcessor = null;
      try {
        inputFileProcessor = new SVFileProcessor(source);
      } catch (IOException e) {
        e.printStackTrace();
      }
      DFDMiner dfdMiner = new DFDMiner(inputFileProcessor);
      dfdMiner.run();
      FunctionalDependencies fds = dfdMiner.getDependencies();
      for (ColumnCollection determining : fds.keySet()) {
        for (Integer dependentColumn : fds.get(determining).getSetBits()) {
          List<ColumnIdentifier> determiningColumns = new ArrayList<ColumnIdentifier>();
          for (Integer determiningColumn : determining.getSetBits()) {
            determiningColumns.add(
                new ColumnIdentifier(this.indentifier, "Column " + determiningColumn));
          }
          FunctionalDependency fd =
              new FunctionalDependency(
                  new ColumnCombination((ColumnIdentifier[]) determiningColumns.toArray()),
                  new ColumnIdentifier(this.indentifier, "Column " + dependentColumn));
          this.resultReceiver.receiveResult(fd);
        }
      }

    }

  }
}
