package de.metanome.algorithms.dfd;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.algorithm_types.FileInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementFileInput;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;
import fdiscovery.approach.runner.DFDMiner;
import fdiscovery.columns.ColumnCollection;
import fdiscovery.general.FunctionalDependencies;
import fdiscovery.preprocessing.SVFileProcessor;

public class DFDMetanome implements FunctionalDependencyAlgorithm,
                                    FileInputParameterAlgorithm {

  private FunctionalDependencyResultReceiver resultReceiver;
  private FileInputGenerator[] fileInputGenerators;
  String identifier;

  public enum Identifier {INPUT_FILE}

  @Override
  public void setFileInputConfigurationValue(String identifier,
                                             FileInputGenerator... fileInputGenerators)
      throws AlgorithmConfigurationException {
    if (DFDMetanome.Identifier.INPUT_FILE.name().equals(identifier)) {
      this.fileInputGenerators = fileInputGenerators;
      this.identifier = identifier;
    }

  }

  @Override
  public void setResultReceiver(
      FunctionalDependencyResultReceiver functionalDependencyResultReceiver) {
    this.resultReceiver = functionalDependencyResultReceiver;
  }


  @Override
  public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
    ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<>();
    configs.add(new ConfigurationRequirementFileInput(DFDMetanome.Identifier.INPUT_FILE.name()));
    return configs;
  }

  @Override
  public void execute() throws AlgorithmExecutionException {
    for (FileInputGenerator fileInput : fileInputGenerators) {
      File source = fileInput.getInputFile();
      SVFileProcessor inputFileProcessor = null;
      try {
        inputFileProcessor = new SVFileProcessor(source);
        inputFileProcessor.init();
        inputFileProcessor.createColumnFiles();
      } catch (IOException e) {
        e.printStackTrace();
      }
      DFDMiner dfdMiner = new DFDMiner(inputFileProcessor);
      dfdMiner.run();
      FunctionalDependencies fds = dfdMiner.getDependencies();
      
      RelationalInput input = fileInput.generateNewCopy();
      String relationName = input.relationName();
      List<String> columnNames = input.columnNames();
      
      for (ColumnCollection determining : fds.keySet()) {
        for (int dependentColumn : fds.get(determining).getSetBits()) {
          ColumnIdentifier[]
              determiningColumns =
              new ColumnIdentifier[determining.getSetBits().length];
          int i = 0;
          for (int determiningColumn : determining.getSetBits()) {
            determiningColumns[i] =
                new ColumnIdentifier(relationName, columnNames.get(determiningColumn));
            i++;
          }
          FunctionalDependency fd =
              new FunctionalDependency(
                  new ColumnCombination(determiningColumns),
                  new ColumnIdentifier(relationName, columnNames.get(dependentColumn)));
          this.resultReceiver.receiveResult(fd);
        }
      }

    }
  }

  @Override
  public String getAuthors() {
  	return "Patrick Schulze";
  }

  @Override
  public String getDescription() {
	return "Random Walk-based FD discovery";
  }

}
