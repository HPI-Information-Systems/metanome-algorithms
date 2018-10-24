package de.metanome.algorithms.cfdfinder;

import static de.metanome.algorithms.cfdfinder.utils.LhsUtils.addSubsetsTo;
import static de.metanome.algorithms.cfdfinder.utils.LhsUtils.generateLhsSubsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnCombination;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.ConditionalFunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.StringParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementString;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ConditionalFunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.ConditionalFunctionalDependency;
import de.metanome.algorithms.cfdfinder.expansion.ConstantPatternExpansionStrategy;
import de.metanome.algorithms.cfdfinder.expansion.ExpansionStrategy;
import de.metanome.algorithms.cfdfinder.expansion.PositiveAndNegativeConstantPatternExpansionStrategy;
import de.metanome.algorithms.cfdfinder.expansion.RangePatternExpansionStrategy;
import de.metanome.algorithms.cfdfinder.pattern.Pattern;
import de.metanome.algorithms.cfdfinder.pattern.PatternTableau;
import de.metanome.algorithms.cfdfinder.pruning.LegacyPruning;
import de.metanome.algorithms.cfdfinder.pruning.PartialFdPruning;
import de.metanome.algorithms.cfdfinder.pruning.PruningStrategy;
import de.metanome.algorithms.cfdfinder.pruning.RhsFilterPruning;
import de.metanome.algorithms.cfdfinder.pruning.SupportIndependentPruning;
import de.metanome.algorithms.cfdfinder.result.DirectOutputResultStrategy;
import de.metanome.algorithms.cfdfinder.result.FileResultStrategy;
import de.metanome.algorithms.cfdfinder.result.PruningLatticeResultStrategy;
import de.metanome.algorithms.cfdfinder.result.PruningLatticeToFileResultStrategy;
import de.metanome.algorithms.cfdfinder.result.Result;
import de.metanome.algorithms.cfdfinder.result.ResultStrategy;
import de.metanome.algorithms.cfdfinder.structures.FDTreeElement;
import de.metanome.algorithms.cfdfinder.structures.PositionListIndex;
import de.metanome.algorithms.cfdfinder.utils.ValueComparator;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.FileUtils;
import it.unimi.dsi.fastutil.ints.Int2IntAVLTreeMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class CFDFinder implements ConditionalFunctionalDependencyAlgorithm, StringParameterAlgorithm, BooleanParameterAlgorithm, IntegerParameterAlgorithm, RelationalInputParameterAlgorithm {

	public enum Identifier {
		INPUT_GENERATOR, NULL_EQUALS_NULL, VALIDATE_PARALLEL, ENABLE_MEMORY_GUARDIAN, MAX_DETERMINANT_SIZE, INPUT_ROW_LIMIT,
		RESULT_STRATEGY, PRUNING_STRATEGY, EXPANSION_STRATEGY, MIN_SUPPORT, MIN_CONFIDENCE, MAX_PATTERNS, MIN_SUPPORT_GAIN,
		MAX_SUPPORT_DROP, RHS_FILTER, RESULT_FILE_NAME, MAX_G1
	}

	/*
	 *  Shared with HyFD
	 */
	private RelationalInputGenerator inputGenerator = null;

	private ValueComparator valueComparator;
	private final MemoryGuardian memoryGuardian = new MemoryGuardian(true);

	private String tableName;
	private List<String> attributeNames;
	private int numAttributes;

	/*
	 *  HyFD
	 */
	private boolean validateParallel = true;	// The validation is the most costly part in CFDFinder and it can easily be parallelized
	private int maxLhsSize = -1;				// The lhss can become numAttributes - 1 large, but usually we are only interested in FDs with lhs < some threshold (otherwise they would not be useful for normalization, key discovery etc.)
	private int inputRowLimit = -1;				// Maximum number of rows to be read from for analysis; values smaller or equal 0 will cause the algorithm to read all rows
	private float efficiencyThreshold = 0.01f;

	/*
	 *  CFDFinder
	 */
	private ConditionalFunctionalDependencyResultReceiver resultReceiver = null;

	private PLICache pliCache = new PLICache(50000);
	private int partialHits = 0;
	private int fullHits = 0;
	private int totalMisses = 0;

	private String resultStrategyName = PruningLatticeResultStrategy.getIdentifier();
	private String pruningStrategyName = SupportIndependentPruning.getIdentifier();
	private String expansionStrategyName = ConstantPatternExpansionStrategy.getIdentifier();

	private double minSupport = 0.8;
	private double minConfidence = 1.0;
	private int patternThreshold = Integer.MAX_VALUE;
	private double minSupportGain = 0.2;
	private double maxLevelSupportDrop = 0.0;
	private int rhsFilter = 0;
	private double maxG1 = 0.01;
	private String resultFileName = null;

	@Override
	public String getAuthors() {
		return "Maximilian Grundke";
	}

	@Override
	public String getDescription() {
		return "Discovering Interesting Conditional Functional Dependencies";
	}
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<ConfigurationRequirement<?>>(5);
		configs.add(new ConfigurationRequirementRelationalInput(CFDFinder.Identifier.INPUT_GENERATOR.name()));
		
		ConfigurationRequirementInteger inputRowLimit = new ConfigurationRequirementInteger(CFDFinder.Identifier.INPUT_ROW_LIMIT.name());
		Integer[] defaultInputRowLimit = {Integer.valueOf(this.inputRowLimit)};
		inputRowLimit.setDefaultValues(defaultInputRowLimit);
		inputRowLimit.setRequired(false);
		configs.add(inputRowLimit);

		ConfigurationRequirementString pruningStrategy = new ConfigurationRequirementString(CFDFinder.Identifier.PRUNING_STRATEGY.name());
		pruningStrategy.setDefaultValues(new String[] {pruningStrategyName});
		pruningStrategy.setRequired(false);
		configs.add(pruningStrategy);

		ConfigurationRequirementString expansionStrategy = new ConfigurationRequirementString(CFDFinder.Identifier.EXPANSION_STRATEGY.name());
		expansionStrategy.setDefaultValues(new String[] {expansionStrategyName});
		expansionStrategy.setRequired(false);
		configs.add(expansionStrategy);

		ConfigurationRequirementString resultStrategy = new ConfigurationRequirementString(CFDFinder.Identifier.RESULT_STRATEGY.name());
		resultStrategy.setDefaultValues(new String[] {resultStrategyName});
		resultStrategy.setRequired(false);
		configs.add(resultStrategy);

		ConfigurationRequirementString minSupport = new ConfigurationRequirementString(Identifier.MIN_SUPPORT.name());
		String[] defaultMinSupport = {String.valueOf(this.minSupport)};
		minSupport.setDefaultValues(defaultMinSupport);
		minSupport.setRequired(false);
		configs.add(minSupport);

		ConfigurationRequirementString minConfidence = new ConfigurationRequirementString(Identifier.MIN_CONFIDENCE.name());
		String[] defaultMinConfidence = {String.valueOf(this.minConfidence)};
		minConfidence.setDefaultValues(defaultMinConfidence);
		minConfidence.setRequired(false);
		configs.add(minConfidence);

		ConfigurationRequirementInteger maxPatterns = new ConfigurationRequirementInteger(Identifier.MAX_PATTERNS.name());
		Integer[] defaultMaxPatterns = {Integer.valueOf(this.patternThreshold)};
		maxPatterns.setDefaultValues(defaultMaxPatterns);
		maxPatterns.setRequired(false);
		configs.add(maxPatterns);

		ConfigurationRequirementString minSupportGain = new ConfigurationRequirementString(Identifier.MIN_SUPPORT_GAIN.name());
		String[] defaultMinSupportGain = {String.valueOf(this.minSupportGain)};
		minSupportGain.setDefaultValues(defaultMinSupportGain);
		minSupportGain.setRequired(false);
		configs.add(minSupportGain);

		ConfigurationRequirementString maxSupportDrop = new ConfigurationRequirementString(Identifier.MAX_SUPPORT_DROP.name());
		String[] defaultMaxSupportDrop = {String.valueOf(this.maxLevelSupportDrop)};
		maxSupportDrop.setDefaultValues(defaultMaxSupportDrop);
		maxSupportDrop.setRequired(false);
		configs.add(maxSupportDrop);

		return configs;
	}

	@Override
	public void setResultReceiver(ConditionalFunctionalDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values) throws AlgorithmConfigurationException {
		boolean value = values[0].booleanValue();
		if (CFDFinder.Identifier.NULL_EQUALS_NULL.name().equals(identifier))
			this.valueComparator = new ValueComparator(value);
		else if (CFDFinder.Identifier.VALIDATE_PARALLEL.name().equals(identifier))
			this.validateParallel = value;
		else if (CFDFinder.Identifier.ENABLE_MEMORY_GUARDIAN.name().equals(identifier))
			this.memoryGuardian.setActive(value);
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setStringConfigurationValue(String identifier, String... values) throws AlgorithmConfigurationException {
		if (Identifier.RESULT_STRATEGY.name().equals(identifier)) {
			this.resultStrategyName = values[0];
		} else if (Identifier.PRUNING_STRATEGY.name().equals(identifier)) {
			this.pruningStrategyName = values[0];
		} else if (Identifier.EXPANSION_STRATEGY.name().equals(identifier)) {
			this.expansionStrategyName = values[0];
		} else if (Identifier.MIN_SUPPORT.name().equals(identifier)) {
			this.minSupport = Double.parseDouble(values[0]);
		} else if (Identifier.MIN_CONFIDENCE.name().equals(identifier)) {
			this.minConfidence = Double.parseDouble(values[0]);
		} else if (Identifier.MIN_SUPPORT_GAIN.name().equals(identifier)) {
			this.minSupportGain = Double.parseDouble(values[0]);
		} else if (Identifier.MAX_SUPPORT_DROP.name().equals(identifier)) {
			this.maxLevelSupportDrop = Double.parseDouble(values[0]);
		} else if (Identifier.MAX_G1.name().equals(identifier)) {
			this.maxG1 = Double.parseDouble(values[0]);
		} else if (Identifier.RESULT_FILE_NAME.name().equals(identifier)) {
			this.resultFileName = values[0];
		} else {
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
		}
	}
	
	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values) throws AlgorithmConfigurationException {
		int value = values[0].intValue();
		if (CFDFinder.Identifier.MAX_DETERMINANT_SIZE.name().equals(identifier)) {
			this.maxLhsSize = value;
		} else if (CFDFinder.Identifier.INPUT_ROW_LIMIT.name().equals(identifier)) {
			if (values.length > 0) {
				this.inputRowLimit = value;
			}
		} else if (Identifier.MAX_PATTERNS.name().equals(identifier)) {
			this.patternThreshold = value;
		} else if (Identifier.RHS_FILTER.name().equals(identifier)) {
			this.rhsFilter = value;
		} else {
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
		}
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
		if (CFDFinder.Identifier.INPUT_GENERATOR.name().equals(identifier))
			this.inputGenerator = values[0];
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}
	
	private void handleUnknownConfiguration(String identifier, String value) throws AlgorithmConfigurationException {
		throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + value);
	}

	@Override
	public String toString() {
		return "CFDFinder:\r\n\t" +
				"inputGenerator: " + ((this.inputGenerator != null) ? this.inputGenerator.toString() : "-") + "\r\n\t" +
				"tableName: " + this.tableName + " (" + CollectionUtils.concat(this.attributeNames, ", ") + ")\r\n\t" +
				"numAttributes: " + this.numAttributes + "\r\n\t" +
				"inputRowLimit: " + this.inputRowLimit + "\r\n" +
				"\r\n" +
				"Progress log: \r\n" + de.metanome.algorithms.cfdfinder.utils.Logger.getInstance().read();
	}
	
	private void initialize(RelationalInput relationalInput) {
		this.tableName = relationalInput.relationName();
		this.attributeNames = relationalInput.columnNames();
		this.numAttributes = this.attributeNames.size();
		if (this.valueComparator == null)
			this.valueComparator = new ValueComparator(true);
	}
	
	private void log(String message) {
		de.metanome.algorithms.cfdfinder.utils.Logger.getInstance().writeln(message);
	}
	
	@Override
	public void execute() throws AlgorithmExecutionException {
		long startTime = System.currentTimeMillis();
		if (this.inputGenerator == null)
			throw new AlgorithmConfigurationException("No input generator set!");
		if (this.resultReceiver == null)
			throw new AlgorithmConfigurationException("No result receiver set!");
		
		this.executeAlgorithm();
		
		log("Time: " + (System.currentTimeMillis() - startTime) + " ms");
	}

	private void executeAlgorithm() throws AlgorithmExecutionException {
		/*
		 *  CFDFinder Pre-processing (find minimal FDs using HyFD)
		 */
		log("Initializing ...");
		RelationalInput relationalInput = this.getInput();
		this.initialize(relationalInput);

		///////////////////////////////////////////////////////
		// Build data structures for sampling and validation //
		///////////////////////////////////////////////////////

		// Calculate plis
		log("Reading data and calculating plis ...");
		de.metanome.algorithms.cfdfinder.structures.PLIBuilder pliBuilder = new de.metanome.algorithms.cfdfinder.structures.PLIBuilder(this.inputRowLimit);
		List<de.metanome.algorithms.cfdfinder.structures.PositionListIndex> plis = pliBuilder.getPLIs(relationalInput, this.numAttributes, this.valueComparator.isNullEqualNull());
		this.closeInput(relationalInput);

		final int numRecords = pliBuilder.getNumLastRecords();
		List<HashMap<String, IntArrayList>> clusterMaps = pliBuilder.clusterMaps;
		pliBuilder = null;

		if (numRecords == 0) {
			ObjectArrayList<ColumnIdentifier> columnIdentifiers = this.buildColumnIdentifiers();
			for (int attr = 0; attr < this.numAttributes; attr++)
				this.resultReceiver.receiveResult(new ConditionalFunctionalDependency(new ColumnCombination(), columnIdentifiers.get(attr), ""));
			return;
		}

		// Sort plis by number of clusters: For searching in the covers and for validation, it is good to have attributes with few non-unique values and many clusters left in the prefix tree
		/*log("Sorting plis by number of clusters ...");
		Collections.sort(plis, new Comparator<de.metanome.algorithms.cfdfinder.structures.PositionListIndex>() {
			@Override
			public int compare(de.metanome.algorithms.cfdfinder.structures.PositionListIndex o1, de.metanome.algorithms.cfdfinder.structures.PositionListIndex o2) {
				int numClustersInO1 = numRecords - o1.getNumNonUniqueValues() + o1.getClusters().size();
				int numClustersInO2 = numRecords - o2.getNumNonUniqueValues() + o2.getClusters().size();
				return numClustersInO2 - numClustersInO1;
			}
		});*/ //todo: use PLI sorting, but return correct column names

		// Calculate inverted plis
		log("Inverting plis ...");
		int[][] invertedPlis = this.invertPlis(plis, numRecords);

		// Extract the integer representations of all records from the inverted plis
		log("Extracting integer representations for the records ...");
		int[][] compressedRecords = new int[numRecords][];
		for (int recordId = 0; recordId < numRecords; recordId++)
			compressedRecords[recordId] = this.fetchRecordFrom(recordId, invertedPlis);

		// Initialize the negative cover
		de.metanome.algorithms.cfdfinder.structures.FDSet negCover = new de.metanome.algorithms.cfdfinder.structures.FDSet(this.numAttributes, this.maxLhsSize);

		// Initialize the positive cover
		de.metanome.algorithms.cfdfinder.structures.FDTree posCover = new de.metanome.algorithms.cfdfinder.structures.FDTree(this.numAttributes, this.maxLhsSize);
		posCover.addMostGeneralDependencies();

		//////////////////////////
		// Build the components //
		//////////////////////////

		Sampler sampler = new Sampler(negCover, posCover, compressedRecords, plis, this.efficiencyThreshold, this.valueComparator, this.memoryGuardian);
		Inductor inductor = new Inductor(negCover, posCover, this.memoryGuardian);
		Validator validator = new Validator(negCover, posCover, numRecords, compressedRecords, plis, this.efficiencyThreshold, this.validateParallel, this.memoryGuardian);

		List<de.metanome.algorithms.cfdfinder.structures.IntegerPair> comparisonSuggestions = new ArrayList<>();
		do {
			de.metanome.algorithms.cfdfinder.structures.FDList newNonFds = sampler.enrichNegativeCover(comparisonSuggestions);
			inductor.updatePositiveCover(newNonFds);
			comparisonSuggestions = validator.validatePositiveCover();
		}
		while (comparisonSuggestions != null);
		negCover = null;

		/*
		 *  CFDFinder Initialization
		 */
		log("Collecting initial CFD candidates...");
		List<FDTreeElement.InternalFunctionalDependency> fds = new LinkedList<>();
		posCover.getInternalFunctionalDependencies(fds, plis);
		//noinspection Since15
		fds.sort(new Comparator<FDTreeElement.InternalFunctionalDependency>() {
			@Override
			public int compare(FDTreeElement.InternalFunctionalDependency o1, FDTreeElement.InternalFunctionalDependency o2) {
				return o1.lhs.cardinality() - o2.lhs.cardinality();
			}
		});

		Set<FDTreeElement.InternalFunctionalDependency> candidates = new HashSet<>();
		candidates.addAll(inductor.maxNonFDs);
		candidates.addAll(validator.maxNonFDs);

		for (FDTreeElement.InternalFunctionalDependency fd : fds) {
			for (BitSet subset : generateLhsSubsets(fd.lhs)) {
				boolean skip = false;
				for (FDTreeElement.InternalFunctionalDependency candidate : candidates) {
					BitSet intersection = (BitSet) subset.clone();
					intersection.and(candidate.lhs);
					if (intersection.cardinality() >= subset.cardinality() && fd.rhs == candidate.rhs) {
						skip = true;
						break;
					}
				}
				if (!skip) {
					candidates.add(new FDTreeElement.InternalFunctionalDependency(subset, fd.rhs, numAttributes));
				}
			}
		}

		List<Set<FDTreeElement.InternalFunctionalDependency>> levels = new ArrayList<>();
		for (int i = 0; i < numAttributes - 1; i += 1) {
			levels.add(new HashSet<FDTreeElement.InternalFunctionalDependency>());
		}
		for (FDTreeElement.InternalFunctionalDependency candidate : candidates) {
			levels.get(candidate.lhs.cardinality() - 1).add(candidate);
		}
		log("Done. " + String.valueOf(candidates.size()) + " maximal non-FDs (initial candidates).");

		log("Building structures...");
		List<List<IntArrayList>> enrichedPLIs = new ArrayList<>();
		List<Map<Integer, String>> enrichedClusterMaps = new LinkedList<>();
		for (int i = 0; i < numAttributes; i += 1) {
			enrichedPLIs.add(enrichPLI(plis.get(i), numRecords));
			Map<Integer, String> clusterMapping = new HashMap<>();
			for (int j = 0; j < enrichedPLIs.get(i).size(); j += 1) {
				for (String key : clusterMaps.get(i).keySet()) {
					IntArrayList ial = clusterMaps.get(i).get(key);
					if (ial.contains(enrichedPLIs.get(i).get(j).getInt(0))) {
						clusterMapping.put(Integer.valueOf(j), key);
						break;
					}
				}
			}
			enrichedClusterMaps.add(clusterMapping);
		}
		enrichCompressedRecords(compressedRecords, enrichedPLIs);

		if (minSupportGain < 1 && minSupportGain >= 0) {
			minSupportGain = Math.max(minSupportGain * numRecords, 1);
		}

		StringBuilder ps = new StringBuilder("\tPruningStrategy: ");
		PruningStrategy oracle = new SupportIndependentPruning(patternThreshold, minSupportGain, maxLevelSupportDrop, minConfidence);
		if (LegacyPruning.getIdentifier().equals(this.pruningStrategyName)) {
			oracle = new LegacyPruning(minSupport, minConfidence, numRecords);
			ps.append(" ([0.0..1.0] minSupport=").append(minSupport).append("; [0.0..1.0] minConfidence=").append(minConfidence);
		} else if (SupportIndependentPruning.getIdentifier().equals(this.pruningStrategyName)) {
			ps.append(" ([int] patternThreshold=").append(patternThreshold)
				.append("; [1..numberOfTuples] minSupportGain=").append(minSupportGain)
				.append("; [0.0..1.0] maxLevelSupportDrop=").append(maxLevelSupportDrop)
				.append("; [0.0..1.0] minConfidence=").append(minConfidence);
		} else if (RhsFilterPruning.getIdentifier().equals(this.pruningStrategyName)) {
			oracle = new RhsFilterPruning(patternThreshold, minSupportGain, maxLevelSupportDrop, rhsFilter);
			ps.append(" ([int] patternThreshold=").append(patternThreshold)
				.append("; [1..numberOfTuples] minSupportGain=").append(minSupportGain)
				.append("; [0.0..1.0] maxLevelSupportDrop=").append(maxLevelSupportDrop)
				.append("; [column id] rhsFilter=").append(rhsFilter);
		} else if (PartialFdPruning.getIdentifier().equals(this.pruningStrategyName)) {
			oracle = new PartialFdPruning(numRecords, maxG1, invertedPlis);
			ps.append(" ([0..1] maxG1=").append(maxG1);
		}
		ps.append(")").insert(18, (oracle.getClass().getName()));
		log(ps.toString());

		ExpansionStrategy expansionStrategy = new ConstantPatternExpansionStrategy(compressedRecords);
		if (PositiveAndNegativeConstantPatternExpansionStrategy.getIdentifier().equals(this.expansionStrategyName)) {
			expansionStrategy = new PositiveAndNegativeConstantPatternExpansionStrategy(compressedRecords);
		} else if (RangePatternExpansionStrategy.getIdentifier().equals(this.expansionStrategyName)) {
			expansionStrategy = new RangePatternExpansionStrategy(compressedRecords, enrichedClusterMaps);
		}
		log("\tExpansion Strategy: " + expansionStrategy.getClass().getName());


		ResultStrategy resultStrategy = new DirectOutputResultStrategy(this.resultReceiver, buildColumnIdentifiers());
		if (PruningLatticeResultStrategy.getIdentifier().equals(this.resultStrategyName)) {
			resultStrategy = new PruningLatticeResultStrategy(this.resultReceiver, buildColumnIdentifiers());
		} else if (PruningLatticeToFileResultStrategy.getIdentifier().equals(this.resultStrategyName)) {
			resultStrategy = new PruningLatticeToFileResultStrategy(this.resultReceiver, buildColumnIdentifiers(), resultFileName);
		} else if (FileResultStrategy.getIdentifier().equals(this.resultStrategyName)) {
			resultStrategy = new FileResultStrategy(this.resultReceiver, buildColumnIdentifiers(), resultFileName);
		}
		log("\tResult Strategy: " + resultStrategy.getClass().getName());

		log("Traversing candidate lattice...");
		int numResults = 0;
		resultStrategy.startReceiving();
		int height = levels.size() - 1;
		while (height >= 0) {
		    Set<FDTreeElement.InternalFunctionalDependency> level = levels.get(height);
			log("\rCandidate level " + String.valueOf(height) + " (" + String.valueOf(level.size()) + " candidates)");
			int visited = 0;
			double overall = 0.0;
			long time = System.nanoTime();
			long lastTime = time;
			for (FDTreeElement.InternalFunctionalDependency candidate : level) {
                PositionListIndex lhs = getLhsPli(candidate.lhs, plis);
				int[] invertedRhsPli = invertedPlis[candidate.rhs];

				oracle.startNewTableau(candidate);
				PatternTableau tableau = generateTableau(candidate.lhs, compressedRecords, lhs, invertedRhsPli, numRecords, oracle, expansionStrategy);
				if (oracle.continueGeneration(tableau)) {
					resultStrategy.receiveResult(new Result(candidate, tableau, attributeNames, enrichedClusterMaps));
					numResults += 1;
					if (height > 0) {
						addSubsetsTo(candidate, levels.get(height - 1));
					}
				}

				visited += 1;
				double percent = 100f / level.size() * visited;
				long secondsFromLastTime = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - lastTime);
				if (percent == 100 || secondsFromLastTime >= 10) {
					visited = 0;
					overall += percent;
					lastTime = System.nanoTime();
					System.out.print("\r" + String.valueOf(Math.round(overall * 1000) / 1000f) + "%");
				}
            }
            level.clear();
            height -= 1;
            time = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - time);
			log("\rDone. (" + (time / 1000f) + " Seconds, " + numResults + " results)");
        }

		log("Total PLI computations: " + String.valueOf(totalMisses + fullHits + partialHits));
		log("Total PLI cache misses: " + String.valueOf(totalMisses));
		log("Total PLI cache hits: " + String.valueOf(fullHits + partialHits));
		log("Total full PLI cache hits: " + String.valueOf(fullHits));
		log("Total partial PLI cache hits: " + String.valueOf(partialHits));

		log("Sending results... (" + resultStrategy.getClass().getName() + ")");
		resultStrategy.stopReceiving();
		log(String.valueOf(resultStrategy.getNumResults()) + " results sent.");
		log("... done!");
	}

	private PositionListIndex getLhsPli(final BitSet lhs, final List<PositionListIndex> plis) {
        PositionListIndex result = pliCache.get(lhs);
        if (result != null) {
        	fullHits += 1;
        	return result;
		}
		BitSet currentlhs = new BitSet(lhs.length());
        for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
        	currentlhs.flip(i);
			PositionListIndex currentPLI = pliCache.get(currentlhs);
			if (currentPLI != null) {
				partialHits += 1;
				result = currentPLI;
			} else {
				PositionListIndex pli = plis.get(i);
				if (result == null) {
					result = pli;
				} else {
					totalMisses += 1;
					result = result.intersect(pli);
					pliCache.put((BitSet) currentlhs.clone(), result);
				}
			}
        }
        return result;
    }

	public static int findViolationsFor(final IntArrayList cluster, final int[] invertedRhsPLI) {
		int maxSize = 0;
		Int2IntAVLTreeMap clusterMap = new Int2IntAVLTreeMap();
		clusterMap.defaultReturnValue(0);
		for (int tuple : cluster) {
			int clusterId = invertedRhsPLI[tuple];
			if (clusterId == -1) {
				// single-tuple cluster, skip
				continue;
			}
			int count = clusterMap.get(clusterId);
			count += 1;
			clusterMap.put(clusterId, count);
			if (count > maxSize) {
				maxSize = count;
			}
		}
		if (maxSize > 0) {
			return cluster.size() - maxSize;
		}
		// if there is no cluster in the list, there are only single-tuple clusters on the rhs and thus, only one keeper
		return cluster.size() - 1;
	}

	private PatternTableau generateTableau(final BitSet attributes, final int[][] values, final PositionListIndex lhs, final int[] rhs, final int numberOfTuples, final PruningStrategy pruningStrategy, final ExpansionStrategy expansionStrategy) {
		Pattern nullPattern = expansionStrategy.generateNullPattern(attributes);
		int violations = 0;
		List<IntArrayList> enrichedClusters = enrichPLI(lhs, numberOfTuples);
		for (IntArrayList cluster : enrichedClusters) {
			nullPattern.getCover().add(cluster.clone());
			violations += findViolationsFor(cluster, rhs);
		}
		nullPattern.setNumKeepers(numberOfTuples - violations);

		PriorityQueue<Pattern> frontier = new PriorityQueue<>();

		nullPattern.setSupport(nullPattern.getNumCover());
		frontier.add(nullPattern);

		Set<Pattern> T = new HashSet<>();

		while (!frontier.isEmpty() && !pruningStrategy.hasEnoughPatterns(T)) {
			Pattern currentPattern = frontier.poll();
			if (pruningStrategy.isPatternWorthAdding(currentPattern)) {
				T.add(currentPattern);
				pruningStrategy.addPattern(currentPattern);
				PriorityQueue<Pattern> newFrontier = new PriorityQueue<>();
				for (Pattern p : frontier) {
					p.updateCover(currentPattern);
					p.updateKeepers(rhs);
					if (pruningStrategy.isPatternWorthConsidering(p)) {
						newFrontier.add(p);
					}
				}
				frontier = newFrontier;
			} else {
				pruningStrategy.expandPattern(currentPattern);
				for (Pattern c : expansionStrategy.getChildPatterns(currentPattern)) {
					if (pruningStrategy.validForProcessing(c)) {
						pruningStrategy.processChild(c);
						List<IntArrayList> cover = determineCover(c, currentPattern, values);
						c.setCover(cover);
						c.updateKeepers(rhs);
						c.setSupport(c.getNumCover());
						if (pruningStrategy.isPatternWorthConsidering(c) && !frontier.contains(c)) {
							frontier.add(c);
						}
					}
				}
			}
		}
		return new PatternTableau(T, numberOfTuples);
	}

	private List<IntArrayList> enrichPLI(final PositionListIndex pli, final int numberOfTuples) {
		List<IntArrayList> result = new ArrayList<>();
		result.addAll(pli.getClusters());
		for (int i = 0; i < numberOfTuples; i++) {
			boolean found = false;
			for (IntArrayList cluster : pli.getClusters()) {
				if (cluster.contains(i)) {
					found = true;
					break;
				}
			}
			if (!found) {
				result.add(new IntArrayList(new int[] {i}));
			}
		}
		return result;
	}

	private void enrichCompressedRecords(int[][] compressedRecords, final List<List<IntArrayList>> enrichedPLIs) {
		for (int tupleId = 0; tupleId < compressedRecords.length; tupleId += 1) {
			int[] tuple = compressedRecords[tupleId];
			for (int attr = 0; attr < tuple.length; attr += 1) {
				if (tuple[attr] == -1) {
					List<IntArrayList> clusters = enrichedPLIs.get(attr);
					for (int clusterId = clusters.size() - 1; clusterId >= 0; clusterId -= 1) {
						IntArrayList cluster = clusters.get(clusterId);
						if (cluster.getInt(0) == tupleId) {
							tuple[attr] = clusterId;
							break;
						}
					}
				}
			}
		}
	}

	private List<IntArrayList> determineCover(final Pattern c, final Pattern currentPattern, final int[][] values) {
	    List<IntArrayList> result = new LinkedList<>();
	    for (IntArrayList cluster : currentPattern.getCover()) {
	        int[] tuple = values[cluster.getInt(0)];
	        if (c.matches(tuple)) {
	            result.add(cluster.clone());
            }
        }
	    return result;
    }
	
	private RelationalInput getInput() throws InputGenerationException, AlgorithmConfigurationException {
		RelationalInput relationalInput = this.inputGenerator.generateNewCopy();
		if (relationalInput == null)
			throw new InputGenerationException("Input generation failed!");
		return relationalInput;
	}
	
	private void closeInput(RelationalInput relationalInput) {
		FileUtils.close(relationalInput);
	}

	private ObjectArrayList<ColumnIdentifier> buildColumnIdentifiers() {
		ObjectArrayList<ColumnIdentifier> columnIdentifiers = new ObjectArrayList<ColumnIdentifier>(this.attributeNames.size());
		for (String attributeName : this.attributeNames)
			columnIdentifiers.add(new ColumnIdentifier(this.tableName, attributeName));
		return columnIdentifiers;
	}

	@SuppressWarnings("unused")
	private ObjectArrayList<List<String>> loadData(RelationalInput relationalInput) throws InputIterationException {
		ObjectArrayList<List<String>> records = new ObjectArrayList<List<String>>();
		while (relationalInput.hasNext())
			records.add(relationalInput.next());
		return records;
	}

	private int[][] invertPlis(List<de.metanome.algorithms.cfdfinder.structures.PositionListIndex> plis, int numRecords) {
		int[][] invertedPlis = new int[plis.size()][];
		for (int attr = 0; attr < plis.size(); attr++) {
			int[] invertedPli = new int[numRecords];
			Arrays.fill(invertedPli, -1);
			
			for (int clusterId = 0; clusterId < plis.get(attr).size(); clusterId++) {
				for (int recordId : plis.get(attr).getClusters().get(clusterId))
					invertedPli[recordId] = clusterId;
			}
			invertedPlis[attr] = invertedPli;
		}
		return invertedPlis;
	}
	
	private int[] fetchRecordFrom(int recordId, int[][] invertedPlis) {
		int[] record = new int[this.numAttributes];
		for (int i = 0; i < this.numAttributes; i++)
			record[i] = invertedPlis[i][recordId];
		return record;
	}
}
