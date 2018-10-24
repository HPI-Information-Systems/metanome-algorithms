package de.metanome.algorithms.normalize;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.configuration.ConfigurationSettingFileInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithms.normalize.config.Config;
import de.metanome.backend.input.file.DefaultFileInputGenerator;
import de.metanome.backend.result_receiver.ResultCache;
import de.uni_potsdam.hpi.utils.FileUtils;

public class Main {

	public static void main(String[] args) {
		Config conf = new Config();
		conf.isHumanInTheLoop = true;
		
		if (args.length != 0)
			conf.setDataset(args[0]);
		
		executeNormi(conf);
/**/		
	//	executeSchema();
    }
	
	private static void executeNormi(Config conf) {
		try {
			Normi normi = new Normi();
			
			RelationalInputGenerator relationalInputGenerator = null;
			ResultCache resultReceiver = new ResultCache("MetanomeMock", null);
			
			relationalInputGenerator = new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
					conf.inputFolderPath + conf.inputDatasetName + conf.inputFileEnding, true,
					conf.inputFileSeparator, conf.inputFileQuotechar, conf.inputFileEscape, conf.inputFileStrictQuotes, 
					conf.inputFileIgnoreLeadingWhiteSpace, conf.inputFileSkipLines, conf.inputFileHasHeader, conf.inputFileSkipDifferingLines, conf.inputFileNullString));
			
			normi.setRelationalInputConfigurationValue(Normi.Identifier.INPUT_GENERATOR.name(), relationalInputGenerator);
			normi.setResultReceiver(resultReceiver);
			
			// A human in the loop works only outside of Metanome. Hence, this is not a Metanome parameter
			normi.setIsHumanInTheLoop(conf.isHumanInTheLoop);
			
			normi.execute();
			
			if (conf.writeResults) {
				final String outputPath = conf.measurementsFolderPath + conf.inputDatasetName + File.separator;
				Stream<Result> results = resultReceiver.fetchNewResults().stream();
				
				final File resultFile = new File(outputPath + conf.resultFileName);
				FileUtils.createFile(outputPath + conf.resultFileName, true);
				
				final FileWriter writer = new FileWriter(resultFile, false);
				
				results.map(result -> result.toString()).forEach(fd -> writeToFile(writer, fd));
				
				writer.close();
				results.close();
			}
		}
		catch (AlgorithmExecutionException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void executeSchema() {
		Config conf = new Config();
		conf.isHumanInTheLoop = false;
		conf.inputFileSeparator = ',';
		conf.inputFileHasHeader = true;
		conf.inputFolderPath = conf.inputFolderPath + "schema" + File.separator;
		
		File folder = new File(conf.inputFolderPath);
		for (String fullFileName : folder.list()) {
			conf.inputDatasetName = fullFileName.split("\\.")[0];
			
			executeNormi(conf);
		}
	}
	
	private static void executeClosureEvaluation(Config conf) {
		try {
			Normi normi = new Normi();
			
			RelationalInputGenerator relationalInputGenerator = null;
			ResultCache resultReceiver = new ResultCache("MetanomeMock", null);
			
			relationalInputGenerator = new DefaultFileInputGenerator(new ConfigurationSettingFileInput(
					conf.inputFolderPath + conf.inputDatasetName + conf.inputFileEnding, true,
					conf.inputFileSeparator, conf.inputFileQuotechar, conf.inputFileEscape, conf.inputFileStrictQuotes, 
					conf.inputFileIgnoreLeadingWhiteSpace, conf.inputFileSkipLines, conf.inputFileHasHeader, conf.inputFileSkipDifferingLines, conf.inputFileNullString));
			
			normi.setRelationalInputConfigurationValue(Normi.Identifier.INPUT_GENERATOR.name(), relationalInputGenerator);
			normi.setResultReceiver(resultReceiver);
			
			normi.evaluateCalculateClosure(30);
		}
		catch (AlgorithmExecutionException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void writeToFile(FileWriter writer, String line) {
		try {
			writer.write(String.format("%s%n", line));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
