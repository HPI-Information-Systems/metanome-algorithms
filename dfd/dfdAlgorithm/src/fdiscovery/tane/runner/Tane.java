package fdiscovery.tane.runner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;

import fdiscovery.columns.ColumnCollection;

import com.rits.cloning.Cloner;

import fdiscovery.equivalence.EquivalenceGroupTIntHashSet;
import fdiscovery.equivalence.TEquivalence;
import fdiscovery.partitions.StrippedPartition;
import fdiscovery.partitions.StrippedPartitions;
import fdiscovery.preprocessing.SVFileProcessor;
import fdiscovery.tane.AprioriGeneration;
import fdiscovery.general.CLIParserMiner;
import fdiscovery.general.CollectionSet;
import fdiscovery.general.ColumnFiles;
import fdiscovery.general.FunctionalDependencies;
import fdiscovery.general.Miner;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.THashMap;

public class Tane extends Miner {

	private int numberOfColumns;
	private int numberOfRows;
	private int[] T, Te;
	private FunctionalDependencies minimalDependencies;
	private StrippedPartitions strippedPartitions;
	private HashMap<ColumnCollection, ColumnCollection> cPlus; 
	private ArrayList<CollectionSet<ColumnCollection>> levels;
	private ColumnCollection rSet;
	
	public FunctionalDependencies getDependencies() {
		return this.minimalDependencies;
	}
	
	public static void main2(String[] args) {
		createColumDirectory();
		createResultDirectory();
		
		File source = new File(Miner.input);
		SVFileProcessor inputFileProcessor = null;
		try {
			long timeStart = System.currentTimeMillis();

			inputFileProcessor = new SVFileProcessor(source);
			inputFileProcessor.init();
			System.out.println("TANE");
			System.out.println("Delimiter:\t" + inputFileProcessor.getDelimiter());
			System.out.println("Columns:\t" + inputFileProcessor.getNumberOfColumns());
			System.out.println("Rows:\t" + inputFileProcessor.getNumberOfRows());
			inputFileProcessor.createColumnFiles();
			Tane taneRunner = new Tane(inputFileProcessor);
			taneRunner.run();
			
			System.out.println(String.format("Number of dependencies:\t%d", taneRunner.minimalDependencies.getCount()));;
			long timeFindFDs = System.currentTimeMillis();
			System.out.println("Total time:\t" + (timeFindFDs - timeStart)/1000 + "s");
			System.out.println(taneRunner.getDependencies());

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
			numberOfColumns = Integer.valueOf(cli.getOptionValue("columns"));
		}
		if (cli.hasOption("rows")) {
			numberOfRows = Integer.valueOf(cli.getOptionValue("rows"));
		}
		ColumnFiles columnFiles = new ColumnFiles(new File(columnFileDirectory), numberOfColumns, numberOfRows);
		long timeStart = System.currentTimeMillis();
		try {
			Tane runner = new Tane(columnFiles, numberOfRows);
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
		outputBuilder.append(String.format("%d\t", this.numberOfRows));
		outputBuilder.append(String.format("%d\t", this.numberOfColumns));
		outputBuilder.append(String.format("%s\t", timeString));
		outputBuilder.append(String.format("%d\t", this.minimalDependencies.getCount()));
		outputBuilder.append(String.format("%d\t", this.minimalDependencies.getCountForSizeLesserThan(2)));
		outputBuilder.append(String.format("%d\t", this.minimalDependencies.getCountForSizeLesserThan(3)));
		outputBuilder.append(String.format("%d\t", this.minimalDependencies.getCountForSizeLesserThan(4)));
		outputBuilder.append(String.format("%d\t", this.minimalDependencies.getCountForSizeLesserThan(5)));
		outputBuilder.append(String.format("%d\t", this.minimalDependencies.getCountForSizeLesserThan(6)));
		outputBuilder.append(String.format("%d\t", this.minimalDependencies.getCountForSizeGreaterThan(5)));
		outputBuilder.append(String.format("%d\t", this.strippedPartitions.size()));
		outputBuilder.append(String.format("%d\t", this.strippedPartitions.size()));
		outputBuilder.append(String.format("%d\n", Runtime.getRuntime().totalMemory()));
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
	
	public Tane(ColumnFiles columnFiles, int numberOfRows) throws OutOfMemoryError {
		this.numberOfColumns = columnFiles.getNumberOfColumns();
		this.numberOfRows = numberOfRows;
		this.minimalDependencies = new FunctionalDependencies();
		this.strippedPartitions = new StrippedPartitions(columnFiles);
		columnFiles.clear();
	}
	
	
	public Tane(SVFileProcessor table) throws OutOfMemoryError {
		this.numberOfColumns = table.getNumberOfColumns();
		this.numberOfRows = table.getNumberOfRows();
		this.minimalDependencies = new FunctionalDependencies();
		this.strippedPartitions = new StrippedPartitions(table.getColumnFiles());
	}
	
	public THashMap<ColumnCollection, ColumnCollection> run() throws OutOfMemoryError {
		
		levels = new ArrayList<>();
		cPlus = new HashMap<>();
		
		// Level 0 is the empty set
		levels.add(new CollectionSet<ColumnCollection>());
		// Level 1 initialization
		levels.add(new CollectionSet<ColumnCollection>());

		ColumnCollection emptyLHSSet = new ColumnCollection(this.numberOfColumns);
		rSet = new ColumnCollection(this.numberOfColumns);

		cPlus.put(emptyLHSSet, rSet);

		this.T = new int[this.numberOfRows + 1];
		this.Te = new int[this.numberOfRows + 1];
		// initialize T to all -1, because it is specified to be all "NULL"
		// (!=0) in TANE
		for (int i = 0; i < T.length; i++) {
			T[i] = -1;
		}

		// Initialization
		for (int i = 0; i < this.numberOfColumns; i++) {
			// set all bits in R
			rSet.set(i);
			// build atomic attribute-sets
			ColumnCollection subset = new ColumnCollection(this.numberOfColumns);
			subset.set(i);
			// add to first level
			levels.get(1).add(subset);
		}
		
		// main algorithm
		int level = 1;
		while (!levels.get(level).isEmpty()) {
//			System.out.println("Level:\t" + level);
			this.computeDependencies(levels.get(level));
			this.prune(levels.get(level));
			levels.add(this.generateNextLevel(levels.get(level)));
			levels.get(level).clear();
			level++;
		}
		return minimalDependencies;
	}
	
	private CollectionSet<ColumnCollection> generateNextLevel(CollectionSet<ColumnCollection> currentLevel) {
		CollectionSet<ColumnCollection> nextLevel = new CollectionSet<>();
		
		Cloner cloner = new Cloner();
		AprioriGeneration<ColumnCollection> prefixBlockGenerator = new AprioriGeneration<>(cloner.deepClone(currentLevel));
		for (CollectionSet<ColumnCollection> k : prefixBlockGenerator.prefixBlocks()) {
			for (ColumnCollection y : k) {
				for (ColumnCollection z : k.tailSet(y)) {
					ColumnCollection x = y.orCopy(z);
					boolean xInNextLevel = true;
					for (Integer a : x.getSetBits()) {
						x.clear(a);
						if (!currentLevel.contains(x)) {
							xInNextLevel = false;
							break;
						}
						x.set(a);
					}
					if (xInNextLevel) {
						nextLevel.add(x);
						strippedPartitions.put(x, strippedProduct(strippedPartitions.get(y), strippedPartitions.get(z)));
					}
				}
			}
		}

		return nextLevel;
	}
	
	private void computeDependencies(CollectionSet<ColumnCollection> currentLevel) {
		for (ColumnCollection x : currentLevel) {
			addCPlusOfX(x);
		}
		
		for (ColumnCollection x : currentLevel) {
			for (Integer a : x.andCopy(cPlus.get(x)).getSetBits()) {
				boolean isDependency = isValidDependency(x.clearCopy(a), a);

				if (isDependency) {
					minimalDependencies.addRHSColumn(x.clearCopy(a), a);
					cPlus.get(x).clear(a);

					for (Integer B : rSet.removeCopy(x).getSetBits()) {
						cPlus.get(x).clear(B);
					}
				}
			}

		}
	}
	
	private ColumnCollection addCPlusOfX(ColumnCollection x) {
		ColumnCollection cPlusOfX = cPlus.get(x.clearCopy(x.nextSetBit(0)));
		
		// if cPlusOfX was not in the list it has to be computed recursively
		if (cPlusOfX == null) {
			cPlusOfX = (ColumnCollection) addCPlusOfX(x.clearCopy(x.nextSetBit(0))).clone();
		} else {
			cPlusOfX = (ColumnCollection) cPlusOfX.clone();
		}
		for (Integer a : x.getSetBits()) {
			ColumnCollection nextCPlusOfX = cPlus.get(x.clearCopy(a));

			if (nextCPlusOfX == null) {
				nextCPlusOfX = (ColumnCollection) addCPlusOfX(x.clearCopy(a)).clone();
			} else {
				nextCPlusOfX = (ColumnCollection) nextCPlusOfX.clone();
			}

			cPlusOfX.and(nextCPlusOfX);
		}
		cPlus.put(x, cPlusOfX);

		return cPlusOfX;
	}
	
	private void prune(CollectionSet<ColumnCollection> currentLevel) {
		Iterator<ColumnCollection> currentLevelIterator = currentLevel.iterator();
		
		while (currentLevelIterator.hasNext()) {
			ColumnCollection x = currentLevelIterator.next();

			ColumnCollection cPlusOfX = cPlus.get(x);
			if (cPlusOfX == null) {
				cPlusOfX = addCPlusOfX(x);
			}

			if (cPlusOfX.isEmpty()) {
				currentLevelIterator.remove();
				continue;
			}

			boolean isSuperKey = isSuperKey(x);
			if (isSuperKey) {
				for (Integer a : cPlus.get(x).removeCopy(x).getSetBits()) {
					ColumnCollection firstCPlusCandidatesKey = x.setCopy(a).clearCopy(x.nextSetBit(0));
					ColumnCollection firstCPlusCandidates = cPlus.get(firstCPlusCandidatesKey);
					if (firstCPlusCandidates == null) {
						firstCPlusCandidates = (ColumnCollection) addCPlusOfX(firstCPlusCandidatesKey).clone();
					} else {
						firstCPlusCandidates = (ColumnCollection) firstCPlusCandidates.clone();
					}
					for (Integer b : x.getSetBits()) {

						ColumnCollection nextCPlusCandidates = cPlus.get(x.setCopy(a).clearCopy(b));
						if (nextCPlusCandidates == null) {
							nextCPlusCandidates = (ColumnCollection) addCPlusOfX(x.setCopy(a).clearCopy(b)).clone();
						} else {
							nextCPlusCandidates = (ColumnCollection) nextCPlusCandidates.clone();
						}

						firstCPlusCandidates.and(nextCPlusCandidates);
					}
					if (firstCPlusCandidates.get(a)) {
						minimalDependencies.addRHSColumn(x, a);
					}
				}
				currentLevelIterator.remove();
			}
		}
	}
	
	protected boolean isSuperKey(ColumnCollection LHS) {
		StrippedPartition partitionOfX = strippedPartitions.get(LHS);

		int sumOfSizesOfEquivalenceClasses = 0;
		int numberOfEquivalenceClasses = 0;

		for (TEquivalence equivalenceGroup : partitionOfX) {
			sumOfSizesOfEquivalenceClasses += equivalenceGroup.size();
			numberOfEquivalenceClasses++;
		}
		
		// equation (1) in the paper
		boolean result = (((sumOfSizesOfEquivalenceClasses - numberOfEquivalenceClasses) / (double) this.numberOfColumns) == 0);

		return result;
	}
	
	private double error(StrippedPartition xPartition, StrippedPartition xUnionAPartition) {
		int e = 0;

		for (TEquivalence equivalenceGroup : xUnionAPartition) {
			Te[equivalenceGroup.getIdentifier()] = equivalenceGroup.size();
		}
		for (TEquivalence equivalenceGroup : xPartition) {
			int m = 1;
		
			for (TIntIterator tIt=equivalenceGroup.iterator(); tIt.hasNext(); ) {
//			for (Integer t : equivalenceGroup) {
				m = Math.max(m, Te[tIt.next()]);
			}
			e = e + equivalenceGroup.size() - m;

		}
		for (TEquivalence equivalenceGroup : xUnionAPartition) {
			Te[equivalenceGroup.getIdentifier()] = 0;
		}
		
		return (double)e / this.numberOfRows;
	}

	
	private boolean isValidDependency(ColumnCollection LHS, Integer RHS) {
		if (LHS.isEmpty()) {
			return false;
		}
		
		return (this.error(strippedPartitions.get(LHS), strippedPartitions.get(LHS.setCopy(RHS))) == 0);
	}
	
	public StrippedPartition strippedProduct(StrippedPartition yPartition, StrippedPartition zPartition) {
		StrippedPartition xPartition = new StrippedPartition();
		HashMap<Integer, TEquivalence> S = new HashMap<>();
		
		if (yPartition.size() > zPartition.size()) {
			StrippedPartition swap = zPartition;
			zPartition = yPartition;
			yPartition = swap;
		}
		
		// build some kind of probe table
		int i = 1;
		for (TEquivalence cI : yPartition) {
			for (TIntIterator tIt=cI.iterator(); tIt.hasNext(); ) {
				int tValue = tIt.next();
				T[tValue] = i;
				
			}
			S.put(i, new EquivalenceGroupTIntHashSet());
			i++;
		}
		
		for (TEquivalence cI : zPartition) {
			for (TIntIterator tIt=cI.iterator(); tIt.hasNext(); ) {
				int tValue = tIt.next();
				if (T[tValue] != -1) {
					TEquivalence sOld = S.get(T[tValue]);
					sOld.add(tValue);
				}
			}
			for (TIntIterator tIt=cI.iterator(); tIt.hasNext(); ) {
				int tValue = tIt.next();
				TEquivalence s = S.get(T[tValue]);
				if (s != null && s.size() > 1) {
					xPartition.add(s);
				}
				S.put(T[tValue], new EquivalenceGroupTIntHashSet());
			}
		}
		i = 1;
		for (TEquivalence cI : yPartition) {
			for (TIntIterator tIt=cI.iterator(); tIt.hasNext(); ) {
				int tValue = tIt.next();
				T[tValue] = -1;
			}
		}

		return xPartition;
	}
}
