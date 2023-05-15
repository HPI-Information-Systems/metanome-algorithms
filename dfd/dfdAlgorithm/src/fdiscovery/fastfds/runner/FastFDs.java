package fdiscovery.fastfds.runner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;

import fdiscovery.columns.AgreeSets;
import fdiscovery.columns.ColumnCollection;
import fdiscovery.columns.DifferenceSets;
import fdiscovery.columns.Path;

import com.rits.cloning.Cloner;

import fdiscovery.partitions.StrippedPartitions;
import fdiscovery.preprocessing.SVFileProcessor;
import fdiscovery.fastfds.EquivalenceClasses;
import fdiscovery.fastfds.MaximalEquivalenceClasses;
import fdiscovery.fastfds.PartialOrder;
import fdiscovery.general.CLIParserMiner;
import fdiscovery.general.ColumnFiles;
import fdiscovery.general.FunctionalDependencies;
import fdiscovery.general.Miner;

public class FastFDs extends Miner {
	
	private int numberOfColumns;
	private int numberOfRows;
	private FunctionalDependencies minimalDependencies;
	private DifferenceSets differenceSets;
	
	public static void main2(String[] args) {
		createColumDirectory();
		createResultDirectory();
		
		File source = new File(Miner.input);
		SVFileProcessor inputFileProcessor = null;
		try {
			long timeStart = System.currentTimeMillis();

			inputFileProcessor = new SVFileProcessor(source);
			inputFileProcessor.init();
			System.out.println("Delimiter:\t" + inputFileProcessor.getDelimiter());
			System.out.println("Columns:\t" + inputFileProcessor.getNumberOfColumns());
			System.out.println("Rows:\t" + inputFileProcessor.getNumberOfRows());
			inputFileProcessor.createColumnFiles();
			FastFDs fastFDRunner = new FastFDs(inputFileProcessor);
			
			fastFDRunner.run();
			System.out.println(String.format("Dependencies: %d.", Integer.valueOf(fastFDRunner.minimalDependencies.getCount())));
			long timeFindFDs = System.currentTimeMillis();
			System.out.println("Total time:\t" + (timeFindFDs - timeStart)/1000 + "s");
			System.out.println(fastFDRunner.getDependencies());
		} catch (FileNotFoundException e) {
			System.out.println("The input file could not be found.");
		} catch (IOException e) {
			System.out.println("The input reader could not be reset.");
		}
	}
	
	public static void main(String[] args) {
		CLIParserMiner parser = new CLIParserMiner();
		CommandLine cli = parser.parse(args);
		String inputFilename = new String();
		String columnFileDirectory = new String();
		String resultFile = new String();
		int numberOfColumns = 0;
		int numberOfRows = 0;
		
		if (cli.hasOption("file")) {
			inputFilename = cli.getOptionValue("file");
		}
		if (cli.hasOption("input")) {
			columnFileDirectory = cli.getOptionValue("input");
		}
		if (cli.hasOption("result")) {
			resultFile = cli.getOptionValue("result");
		}
		if (cli.hasOption("columns")) {
			numberOfColumns = Integer.valueOf(cli.getOptionValue("columns")).intValue();
		}
		if (cli.hasOption("rows")) {
			numberOfRows = Integer.valueOf(cli.getOptionValue("rows")).intValue();
		}
		ColumnFiles columnFiles = new ColumnFiles(new File(columnFileDirectory), numberOfColumns, numberOfRows);
		long timeStart = System.currentTimeMillis();
		try {
			FastFDs runner = new FastFDs(columnFiles, numberOfRows);
			runner.run();
			long timeEnd = System.currentTimeMillis();
			runner.writeOutputSuccessful(resultFile, timeEnd - timeStart, inputFilename);
		} catch(OutOfMemoryError e) {
			System.exit(Miner.STATUS_OOM);
		}
		System.exit(0);
	}
	
	private void writeOutputSuccessful(String outputFile, long time, String inputFileName) {
		String timeString = (time != -1)? String.format("%.1f", (double)(time)/1000) : "-1";
		
		StringBuilder outputBuilder = new StringBuilder();
		if (!inputFileName.isEmpty()) {
			outputBuilder.append(String.format("%s\t", inputFileName));
		}
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.numberOfRows)));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.numberOfColumns)));
		outputBuilder.append(String.format("%s\t", timeString));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.minimalDependencies.getCount())));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.minimalDependencies.getCountForSizeLesserThan(2))));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.minimalDependencies.getCountForSizeLesserThan(3))));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.minimalDependencies.getCountForSizeLesserThan(4))));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.minimalDependencies.getCountForSizeLesserThan(5))));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.minimalDependencies.getCountForSizeLesserThan(6))));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(this.minimalDependencies.getCountForSizeGreaterThan(5))));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(0)));
		outputBuilder.append(String.format("%d\t", Integer.valueOf(0)));
		outputBuilder.append(String.format("%d\n", Long.valueOf(Runtime.getRuntime().totalMemory())));
		outputBuilder.append(String.format("#Memory: %s\n", Miner.humanReadableByteCount(Runtime.getRuntime().totalMemory(), false)));

		try {
			BufferedWriter resultFileWriter = new BufferedWriter(new FileWriter(new File(outputFile), true));
			resultFileWriter.write(outputBuilder.toString());
			System.out.print(outputBuilder.toString());
			resultFileWriter.close();
		} catch (IOException e) {
			System.out.println("Couldn't write output.");
		}
	}
	
	public FastFDs(ColumnFiles columnFiles, int numberOfRows) throws OutOfMemoryError {
		this.minimalDependencies = new FunctionalDependencies();
		this.numberOfColumns = columnFiles.getNumberOfColumns();
		this.numberOfRows = numberOfRows;
		
		StrippedPartitions strippedPartitions = new StrippedPartitions(columnFiles);
		EquivalenceClasses equivalenceClasses = new EquivalenceClasses(strippedPartitions);
		MaximalEquivalenceClasses maximalEquivalenceClasses = new MaximalEquivalenceClasses(strippedPartitions);
		strippedPartitions.clear();
		AgreeSets agreeSets = new AgreeSets(maximalEquivalenceClasses, equivalenceClasses, this.numberOfColumns, this.numberOfRows);
		maximalEquivalenceClasses.clear();
		equivalenceClasses.clear();
		this.differenceSets = new DifferenceSets(agreeSets);
		agreeSets.clear();
	}
	
	public FastFDs(SVFileProcessor table) throws OutOfMemoryError {
		this.minimalDependencies = new FunctionalDependencies();
		this.numberOfColumns = table.getNumberOfColumns();
		this.numberOfRows = table.getNumberOfRows();
		
		ColumnFiles columnFiles = table.getColumnFiles();
		StrippedPartitions strippedPartitions = new StrippedPartitions(columnFiles);
		EquivalenceClasses equivalenceClasses = new EquivalenceClasses(strippedPartitions);
		MaximalEquivalenceClasses maximalEquivalenceClasses = new MaximalEquivalenceClasses(strippedPartitions);
		strippedPartitions.clear();
		AgreeSets agreeSets = new AgreeSets(maximalEquivalenceClasses, equivalenceClasses, this.numberOfColumns, this.numberOfRows);
		maximalEquivalenceClasses.clear();
		equivalenceClasses.clear();
		this.differenceSets = new DifferenceSets(agreeSets);
		agreeSets.clear();
	}
	
	public void run() throws OutOfMemoryError {
		int numberOfColumns = this.numberOfColumns;
		
		DifferenceSets[] differenceSetsModulo = this.differenceSets.allModulo(this.numberOfColumns);
		for (int rhsIndex = 0; rhsIndex < numberOfColumns; rhsIndex++) {
			DifferenceSets orig = differenceSetsModulo[rhsIndex];
			Cloner cloner = new Cloner();
			DifferenceSets uncovered = cloner.deepClone(orig);
			if (orig.isEmpty()) {
				ColumnCollection lhs = new ColumnCollection(this.numberOfColumns);
				
				for (int lhsIndex : lhs.setCopy(rhsIndex).complement().getSetBits()) {
					this.minimalDependencies.addRHSColumn(lhs.setCopy(lhsIndex), rhsIndex);
				}
			} 
			else if (!orig.containsEmptySet()) {
				PartialOrder currentOrder = new PartialOrder(orig);
				Path path = new Path(numberOfColumns);
				findCovers(rhsIndex, orig, uncovered, path, currentOrder);
			}
		}
	}
	
	public void findCovers(int columnIndex, DifferenceSets orig, DifferenceSets uncovered, Path currentPath, PartialOrder currentOrder) {
		// no dependencies here
		if (currentOrder.isEmpty() && !uncovered.isEmpty()) {
			return;
		}
		
		if (uncovered.isEmpty()) {
			if (!orig.maximumSubsetCoversDifferenceSet(currentPath)) {
				this.minimalDependencies.addRHSColumn(currentPath, columnIndex);
			} else {
				// dependency not minimal
				return;
			}
		}

		// RECURSIVE CASE
		for (int remainingColumn : currentOrder.getOrderedColumns()) {
			DifferenceSets nextDifferenceSets = uncovered.removeCovered(remainingColumn);
			PartialOrder nextOrder = new PartialOrder(nextDifferenceSets, remainingColumn);
			Path nextPath = (Path) currentPath.addColumn(remainingColumn);
					
			nextPath.addColumn(remainingColumn);
			findCovers(columnIndex, orig, nextDifferenceSets, nextPath, nextOrder);
		}
	}
	
	public FunctionalDependencies getDependencies() {
		return this.minimalDependencies;
	}
}
