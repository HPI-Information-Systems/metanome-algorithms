package de.metanome.algorithms.normalize;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.algorithm_types.BasicStatisticsAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.BasicStatisticsResultReceiver;
import de.metanome.algorithm_integration.results.BasicStatistic;
import de.metanome.algorithm_integration.results.Result;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValue;
import de.metanome.algorithm_integration.results.basic_statistic_values.BasicStatisticValueString;
import de.metanome.algorithms.normalize.aspects.NormiConversion;
import de.metanome.algorithms.normalize.aspects.NormiPersistence;
import de.metanome.algorithms.normalize.fddiscovery.FdDiscoverer;
import de.metanome.algorithms.normalize.fddiscovery.HyFDFdDiscoverer;
import de.metanome.algorithms.normalize.fdextension.FdExtender;
import de.metanome.algorithms.normalize.fdextension.PullingFdExtender;
import de.metanome.algorithms.normalize.structures.FunctionalDependency;
import de.metanome.algorithms.normalize.structures.Schema;
import de.metanome.algorithms.normalize.utils.Utils;
import de.uni_potsdam.hpi.utils.CollectionUtils;

public class Normi implements BasicStatisticsAlgorithm, RelationalInputParameterAlgorithm {

	public enum Identifier {
		INPUT_GENERATOR
	};

	private String tableName;
	private List<ColumnIdentifier> columnIdentifiers;

	private String tempResultsPath;
	private String tempExtendedResultsPath;
	
	private NormiConversion converter;
	private NormiPersistence persister;
	
	private RelationalInputGenerator inputGenerator = null;
	private BasicStatisticsResultReceiver resultReceiver = null;
	
	private Boolean nullEqualsNull = Boolean.valueOf(true);
	
	private int maxLinesToPrint = 50;
	
	public boolean isHumanInTheLoop = false;

	public void setIsHumanInTheLoop(boolean isHumanInTheLoop) {
		this.isHumanInTheLoop = isHumanInTheLoop;
	}
	
	@Override
	public String getAuthors() {
		return "Thorsten Papenbrock";
	}

	@Override
	public String getDescription() {
		return "Schema normalization into BCNF using HyFD";
	}
	
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> configs = new ArrayList<ConfigurationRequirement<?>>(1);
		configs.add(new ConfigurationRequirementRelationalInput(Normi.Identifier.INPUT_GENERATOR.name()));
		
		return configs;
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values) throws AlgorithmConfigurationException {
		if (Normi.Identifier.INPUT_GENERATOR.name().equals(identifier))
			this.inputGenerator = values[0];
		else
			this.handleUnknownConfiguration(identifier, CollectionUtils.concat(values, ","));
	}

	@Override
	public void setResultReceiver(BasicStatisticsResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	private void handleUnknownConfiguration(String identifier, String value) throws AlgorithmConfigurationException {
		throw new AlgorithmConfigurationException("Unknown configuration: " + identifier + " -> " + value);
	}
	
	@Override
	public void execute() throws AlgorithmExecutionException {
		System.out.println();
		System.out.println("///// Initialization /////");
		System.out.println();
		
		this.initialize();

		System.out.println(">>> " + this.tableName + " <<<");
		
		// Statistics
//		System.out.println("Exact duplicates: " + this.findExactDuplicates());
		
		System.out.println();
		System.out.println("///// FD-Discovery ///////");
		System.out.println();
		
		FdDiscoverer fdDiscoverer = new HyFDFdDiscoverer(this.converter, this.persister, this.tempResultsPath);
		Map<BitSet, BitSet> fds = fdDiscoverer.calculateFds(this.inputGenerator, this.nullEqualsNull, true);
		
		// Statistics
		int numFds = (int)fds.values().stream().mapToLong(BitSet::cardinality).sum();
		float avgFdsLhsLength = fds.entrySet().stream().mapToLong(entry -> entry.getKey().cardinality() * entry.getValue().cardinality()).sum() / (float)numFds;
		float avgFdsRhsLength = 1.0f;
		
		// Statistics
		int numAggregatedFds = fds.keySet().size();
		float avgAggregatedFdsLhsLength = fds.keySet().stream().mapToLong(BitSet::cardinality).sum() / (float)numAggregatedFds;
		float avgAggregatedFdsRhsLength = fds.values().stream().mapToLong(BitSet::cardinality).sum() / (float)numAggregatedFds;
		
		System.out.println();
		System.out.println("///// Key-Analysis ///////");
		System.out.println();
		
	//	FdExtender fdExtender = new NaiveFdExtender(this.persister, this.tempExtendedResultsPath);
	//	FdExtender fdExtender = new PushingFdExtender(this.persister, this.tempExtendedResultsPath);
		FdExtender fdExtender = new PullingFdExtender(this.persister, this.tempExtendedResultsPath, this.columnIdentifiers.size(), true);
		fds = fdExtender.calculateClosure(fds, true);
		
		// Statistics
		int numExtendedFds = fds.keySet().size();
		float avgExtendedFdsLhsLength = fds.keySet().stream().mapToLong(BitSet::cardinality).sum() / (float)numExtendedFds;
		float avgExtendedFdsRhsLength = fds.values().stream().mapToLong(BitSet::cardinality).sum() / (float)numExtendedFds;
		
		List<BitSet> fdKeys = this.extractKeys(fds, this.columnIdentifiers.size());
		int numFdKeys = fdKeys.size();
		float avgFdKeyLength = fdKeys.stream().mapToLong(BitSet::cardinality).sum() / (float)numFdKeys;
		
		System.out.println();
		System.out.println("# FDs: " + numFds + " (avg lhs size: " + avgFdsLhsLength + "; avg rhs size: " + avgFdsRhsLength + ")");
		System.out.println("# aggregated FDs: " + numAggregatedFds + " (avg lhs size: " + avgAggregatedFdsLhsLength + "; avg rhs size: " + avgAggregatedFdsRhsLength + ")");
		System.out.println("# extended FDs: " + numExtendedFds + " (avg lhs size: " + avgExtendedFdsLhsLength + "; avg rhs size: " + avgExtendedFdsRhsLength + ")");
		System.out.println("# FD-Keys: " + numFdKeys + " (avg size: " + avgFdKeyLength + ")");
		
		System.out.println();
		System.out.println("///// BCNF-Building //////");
		System.out.println();
		
		List<Schema> bcnf = this.buildBcnf(fds);
		
		// Print final schemata
		System.out.println();
		this.print(bcnf);
		
		// Output final schemata
		for (Schema schema : bcnf) {
			Map<String, BasicStatisticValue> keys = new HashMap<>(schema.getReferencedSchemata().size() + 1);
			keys.put("PrimaryKey", new BasicStatisticValueString(this.BitSetAttributesToString(schema.getPrimaryKey().getLhs())));
			
			for (Schema referencedSchema : schema.getReferencedSchemata())
				keys.put("ForeignKey", new BasicStatisticValueString(this.BitSetAttributesToString(referencedSchema.getPrimaryKey().getLhs())));
			
			BasicStatistic result = new BasicStatistic(keys, this.getColumnIdentifiersFor(schema.getAttributes()));
			
			this.resultReceiver.receiveResult(result);
		}
	}

	private String BitSetAttributesToString(BitSet bitSetAttributes) {
		return this.ColumnIdentifiersToString(this.getColumnIdentifiersFor(bitSetAttributes));
	}
	
	private String ColumnIdentifiersToString(ColumnIdentifier[] columnIdentifiers) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < columnIdentifiers.length; i++) {
			builder.append(columnIdentifiers[i].toString());
			if (i < columnIdentifiers.length - 1)
				builder.append(", ");
		}
		return builder.toString();
	}
	
	private ColumnIdentifier[] getColumnIdentifiersFor(BitSet attributes) {
		ColumnIdentifier[] columnIdentifiers = new ColumnIdentifier[attributes.cardinality()];
		int insertIndex = 0;
		for (int attribute = attributes.nextSetBit(0); attribute >= 0; attribute = attributes.nextSetBit(attribute + 1)) {
			columnIdentifiers[insertIndex] = this.columnIdentifiers.get(attribute);
			insertIndex++;
		}
		return columnIdentifiers;
	}
	
	private void initialize() throws AlgorithmExecutionException {
		RelationalInput relationalInput;
		try {
			relationalInput = this.inputGenerator.generateNewCopy();
		}
		catch (InputGenerationException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}		
		
		System.out.println("Reading table metadata ...");
		this.tableName = relationalInput.relationName();// TODO: Für schema experiments: .replaceAll("\\.csv|\\.tsv|\\.txt", "");
		List<String> attributeNames = relationalInput.columnNames();
		this.columnIdentifiers = attributeNames.stream().map(name -> new ColumnIdentifier(this.tableName, name)).collect(Collectors.toCollection(ArrayList::new));
		
		try {
			relationalInput.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		
		System.out.println("Processing table metadata ...");
		int columnIdentifierNumber = 0;
		Map<ColumnIdentifier, Integer> name2number = new HashMap<>();
		Map<Integer, ColumnIdentifier> number2name = new HashMap<>();
		for (ColumnIdentifier identifier : this.columnIdentifiers) {
			name2number.put(identifier, Integer.valueOf(columnIdentifierNumber));
			number2name.put(Integer.valueOf(columnIdentifierNumber), identifier);
			columnIdentifierNumber++;
		}
		
		this.tempResultsPath = "temp" + File.separator + this.tableName + "-hyfd.txt";
		this.tempExtendedResultsPath = "temp" + File.separator + this.tableName + "-hyfd_extended.txt";
		
		this.converter = new NormiConversion(this.columnIdentifiers, name2number, number2name);
		this.persister = new NormiPersistence(this.columnIdentifiers);
	}
	
	private List<BitSet> extractKeys(Map<BitSet, BitSet> fds, int numAttributes) {
		System.out.print("Extracting FD-Keys ... ");
		long time = System.currentTimeMillis();
		List<BitSet> keys = new ArrayList<>();
		List<BitSet> nonKeys = new ArrayList<>();
		for (Map.Entry<BitSet, BitSet> entry : fds.entrySet()) {
			if ((entry.getKey().cardinality() + entry.getValue().cardinality()) == numAttributes)
				keys.add(entry.getKey());
			else
				nonKeys.add(entry.getKey());
		}
		System.out.println(System.currentTimeMillis() - time);
		
/*		// Build all 2, 3, 4, 5, ... combinations of FDs; check if they became keys this way; if so, stop extending them, check they are minimal and add them if so.
		// When combining FDs, we might find a key that when checked against the known keys is apparently minimal, but only because we did not find the really minimal subset key yet --> need a clever combination technique
		List<List<BitSet>> lhssPerRhs = new ArrayList<>(numAttributes);
		for (int i = 0; i < numAttributes; i++)
			lhssPerRhs.addAll(new ArrayList<>());
		for (BitSet nonKey : nonKeys) {
			BitSet rhs = fds.get(nonKey);
			for (int rhsAttr = rhs.nextSetBit(0); rhsAttr >= 0; rhsAttr = rhs.nextSetBit(rhsAttr + 1))
				lhssPerRhs.get(rhsAttr).add(nonKey);
		}
		for (int i = 0; i < numAttributes; i++) {
			
		}
		
		// Find all composite keys of two FDs. We only discover composite keys of two FDs, because we only consider join tables of two relations
		for (int i = 0; i < nonKeys.size() - 1; i++) {
			BitSet lhs1 = nonKeys.get(i).clone();
			BitSet rhs1 = fds.get(lhs1);
			for (int j = i + 1; j < nonKeys.size(); j++) {
				BitSet lhs2 = nonKeys.get(i);
				BitSet rhs2 = fds.get(lhs2);
				
				BitSet combined = lhs1.clone();
				combined.or(lhs2);
				combined.or(rhs1);
				combined.or(rhs2);
				
				if (combined.cardinality() == numAttributes) {
					BitSet combinedLhs = lhs1.clone();
					combinedLhs.or(lhs2);
					
					boolean isMinimal = true;
					for (BitSet key : keys) {
						if (Utils.andNotCount(key, combinedLhs) == 0) {
							isMinimal = false;
							break;
						}
					}
					
					if (isMinimal)
						keys.add(combinedLhs);
				}
			}
		}
		
		// Build a "missing tree", i.e., a prefix tree that holds the missing attribute combinations for FDs to be key
		
		// For each FD, check if its rhs covers some missing attribute combination; if so, this FD together with the according FD forms a key
		
		// If a key has been found, make sure that no subset of it is already a key
*/		
		return keys;
	}
	
	private List<Schema> buildBcnf(Map<BitSet, BitSet> extendedFds) throws AlgorithmExecutionException {
		Scanner scanner = null;
		try {
			if (this.isHumanInTheLoop)
				scanner = new Scanner(System.in);
			
			return this.buildBcnf(extendedFds, scanner);
		}
		finally {
			if (scanner != null)
				scanner.close();
		}
	}
	
	private List<Schema> buildBcnf(Map<BitSet, BitSet> extendedFds, Scanner scanner) throws AlgorithmExecutionException {
		List<Schema> finalSchemata = new ArrayList<>();
		List<Schema> workingSchemata = new ArrayList<>();
		
		Schema baseSchema = Schema.create(this.inputGenerator, extendedFds);
		workingSchemata.add(baseSchema);
		
		while (!workingSchemata.isEmpty()) {
			Schema currentSchema = workingSchemata.remove(workingSchemata.size() - 1);
			
			// Violating FD selection
			List<FunctionalDependency> violatingFds = currentSchema.getViolatingFds();
			if (violatingFds.isEmpty()) {
				finalSchemata.add(currentSchema);
				continue;
			}
			
			Collections.sort(violatingFds, (o1, o2) -> (int) Math.signum(o1.fdScore() - o2.fdScore()));
			
			if (violatingFds.size() > this.maxLinesToPrint)
				System.out.println("\t...");
			
			for (int i = Math.max(0, violatingFds.size() - this.maxLinesToPrint); i < violatingFds.size(); i++) {
				FunctionalDependency violatingFd = violatingFds.get(i);
				System.out.println("\t(" + (violatingFds.size() - i) + ")\t" + violatingFd.toFdString() + "\t" + this.converter.formatFd(violatingFd.getLhs(), violatingFd.getRhs()));
			}
			
			System.out.println("Current schema is: ");
			BitSet primaryKey = (currentSchema.getPrimaryKey() == null) ? null : currentSchema.getPrimaryKey().getLhs();
			System.out.println(this.converter.formatSchema(currentSchema.getAttributes(), primaryKey));
			System.out.println("Which violating fd should we choose for normalization? (Choose 0 for \"no normalization needed\")");
			int fdChoice = (scanner != null) ? scanner.nextInt() : 1;
			
			if (fdChoice == 0) {
				finalSchemata.add(currentSchema);
				continue;
			}

			FunctionalDependency splitFd = violatingFds.get(violatingFds.size() - fdChoice).clone();
			
			// Violating FD shortening: If an rhs attribute does not only depend on the chosen lhs, then the user might want to spare it for a different split later on
			BitSet removableRhsAttributes = new BitSet(this.columnIdentifiers.size());
			BitSet attributes1 = splitFd.getAttributes();
			BitSet attributes2 = (BitSet) currentSchema.getAttributes().clone();
			attributes2.andNot(splitFd.getRhs());
			for (int rhsAttr = splitFd.getRhs().nextSetBit(0); rhsAttr >= 0; rhsAttr = splitFd.getRhs().nextSetBit(rhsAttr + 1)) {
				// If the rhs attribute is part of a foreign key in the current schema and the foreign key is fully covered by the split FD, then we take the foreign key to the new schema and the user should not remove it from the split
				boolean rhsAttrImportant = false;
				for (Schema referencedSchema : currentSchema.getReferencedSchemata()) {
					if (referencedSchema.getPrimaryKey().getLhs().get(rhsAttr) && (Utils.andNotCount(referencedSchema.getPrimaryKey().getLhs(), attributes1) == 0)) {
						rhsAttrImportant = true;
						break;
					}
				}
				if (rhsAttrImportant)
					continue;
				
				// Check if any other violating FD could also utilize the rhs attribute later on
				for (FunctionalDependency fd : violatingFds) {
					if (fd.getLhs().equals(splitFd.getLhs()))
						continue;
					
					if (Utils.andNotCount(fd.getLhs(), attributes2) == 0) { // Its lhs must be fully contained in the original schema and the attribute must be taken away; if the split violates the fds lhs, it cannot be used for further normalization steps, anyway, and if it is contained in the new schema, it can still be used the split away the current attribute later on
						removableRhsAttributes.set(rhsAttr);
						break;
					}
				}
			}
			
			if (removableRhsAttributes.cardinality() > 1) {
				System.out.println("The following rhs attributes of the chosen violating FD are also determined by other violating FDs right hand sides. Therefore, they could be used in later normalization steps as well.");
				System.out.println(this.converter.formatKey(removableRhsAttributes));
				System.out.println("Should some of them be excluded from the current normalization step? Enter a comma-separated list of attributes you want to exclude or \"0\" if no attrbute should be excluded:");
				String exclusionChoice = (scanner != null) ? scanner.next() : "0";
				
				if (!"0".equals(exclusionChoice))
					Arrays.stream(exclusionChoice.replaceAll(" ", "").split(","))
							.map(string -> Integer.valueOf(string))
							.filter(i -> (i.intValue() > 0) && (i.intValue() < this.columnIdentifiers.size()))
							.map(i -> {
								int attr = 0;
								for (int j = 0; j < i.intValue(); j++)
									attr = removableRhsAttributes.nextSetBit(attr);
								return Integer.valueOf(attr);
							})
							.forEach(i -> splitFd.getRhs().clear(i.intValue()));
				
				if (splitFd.getRhs().cardinality() == 0)
					throw new RuntimeException("You removed all attributes from the rhs. How do you think the normalization should work now?");
			}
			
			// First relation with the FD-attributes
			attributes1 = splitFd.getAttributes();
			Schema schema1 = Schema.create(attributes1, currentSchema);
			schema1.setPrimaryKey(schema1.getFdKeys().stream().filter(fd -> fd.getLhs().equals(splitFd.getLhs())).findFirst().get());
			workingSchemata.add(schema1);
			
			// Second relation with the remaining attributes and the lhs-attributes
			attributes2 = (BitSet) currentSchema.getAttributes().clone();
			attributes2.andNot(splitFd.getRhs());
			Schema schema2 = Schema.create(attributes2, currentSchema);
			if ((currentSchema.getPrimaryKey() != null) && (Utils.andNotCount(currentSchema.getPrimaryKey().getLhs(), attributes2) == 0)) // This should always be true, because we do not split key attributes away
				schema2.setPrimaryKey(currentSchema.getPrimaryKey());
			schema2.addReferencedSchema(schema1);
			workingSchemata.add(schema2);
		}

		System.out.println("\nDecomposition is complete. We now need to find good primary keys for relations without a primary key.\n");
		
		// Primary key selection
		for (Schema finalSchema : finalSchemata) {
			if (finalSchema.getPrimaryKey() == null) {
				List<FunctionalDependency> keys = finalSchema.getAllKeys();

				Collections.sort(keys, (o1, o2) -> (int) Math.signum(o1.keyScore() - o2.keyScore()));
				
				if (keys.size() == 0) {
					// All attributes together form the primary key
					finalSchema.setPrimaryKey(new FunctionalDependency(finalSchema.getAttributes(), new BitSet(), finalSchema));
				}
				else if (keys.size() == 1) {
					// The only key is the primary key
					finalSchema.setPrimaryKey(keys.get(0));
				}
				else {
					// Choose a key
					if (keys.size() > this.maxLinesToPrint)
						System.out.println("\t...");
					
					for (int i = Math.max(0, keys.size() - this.maxLinesToPrint); i < keys.size(); i++) {
						FunctionalDependency key = keys.get(i);
						System.out.println("\t(" + (keys.size() - i) + ")\t" + key.toKeyString() + "\t" + this.converter.formatKey(key.getLhs()));
					}
					
					System.out.println("Current schema is: ");
					System.out.println(this.converter.formatSchema(finalSchema.getAttributes(), new BitSet(this.columnIdentifiers.size())));
					System.out.println("Which key should we choose as primary key?");
					int keyChoice = (scanner != null) ? scanner.nextInt() : 1;
					
					finalSchema.setPrimaryKey(keys.get(keys.size() - keyChoice));
				}
			}
		}
		
		return finalSchemata;
	}
	
	private void print(List<Schema> schemata) {
		schemata.stream().map(schema -> this.converter.formatSchema(schema.getAttributes(), schema.getPrimaryKey().getLhs())).forEach(System.out::println);
	}
	
/*	private List<Schema> buildBcnf(Map<BitSet, BitSet> fds, List<BitSet> keys) throws AlgorithmExecutionException {
		Analyzer analyzer = new Analyzer(this.inputGenerator, this.columnIdentifiers);
		Scanner scanner = new Scanner(System.in);
		
		BitSet baseAttributes = new BitSet(this.columnIdentifiers.size());
		baseAttributes.set(0, this.columnIdentifiers.size());
		Schema baseSchema = new Schema(baseAttributes, fds, keys);
		
		
		// TODO: calculate the FD properties here with the analyzer once! than use them in all subsequent steps (Note: the bloom filters should be filled outside the properties objects)
		
		
		
		List<Schema> finalSchemata = new ArrayList<>();
		List<Schema> workingSchemata = new ArrayList<>();
		workingSchemata.add(baseSchema);
		while (!workingSchemata.isEmpty()) {
			Schema currentSchema = workingSchemata.remove(workingSchemata.size() - 1);
			
			// Primary key selection
			if (currentSchema.getPrimaryKey() == null) {
				List<KeyStatistic> keyStatistics = analyzer.analyze(currentSchema.getKeys());
				for (int i = 0; i < keyStatistics.size(); i++) {
					KeyStatistic statistic = keyStatistics.get(i);
					System.out.println("\t(" + (keyStatistics.size() - i) + ")\t" + statistic.toString() + "\t" + this.converter.formatKey(statistic.getKey()));
				}
				
				System.out.println("Current schema is: ");
				System.out.println(this.converter.format(this.converter.toColumnCombination(currentSchema.getAttributes())));
				System.out.println("Which key should we choose as primary key?");
				int keyChoice = scanner.nextInt();
				
				currentSchema.setPrimaryKey(keyStatistics.get(keyStatistics.size() - keyChoice).getKey());
			}
			
			// Violating FD selection
			if (currentSchema.getViolatingFds().isEmpty()) {
				finalSchemata.add(currentSchema);
				continue;
			}
			
			List<FdStatistic> fdStatistics = analyzer.analyze(currentSchema.getViolatingFds());
			for (int i = 0; i < fdStatistics.size(); i++) {
				FdStatistic statistic = fdStatistics.get(i);
				System.out.println("\t(" + (fdStatistics.size() - i) + ")\t" + statistic.toString() + "\t" + this.converter.formatFd(statistic.getKey(), statistic.getRhs()));
			}
			
			System.out.println("Current schema is: ");
			System.out.println(this.converter.formatSchema(currentSchema.getAttributes(), currentSchema.getPrimaryKey()));
			System.out.println("Which violating fd should we choose for normalization? (Choose 0 for \"no normalization needed\")");
			int fdChoice = scanner.nextInt();
			
			if (fdChoice == 0) {
				finalSchemata.add(currentSchema);
				continue;
			}

			// TODO: We should take those FDs that have an overlap with the primary key out of the process although they technically work as normalization keys, because the user does not expect to choose a primary key that is afterwards destroyed again
			
			BitSet lhs = fdStatistics.get(fdStatistics.size() - fdChoice).getKey();
			BitSet rhs = fdStatistics.get(fdStatistics.size() - fdChoice).getRhs();
			
			// First relation with the FD-attributes
			BitSet attributes1 = lhs.clone();
			attributes1.or(rhs);
			Map<BitSet, BitSet> fds1 = this.filter(fds, attributes1);
			List<BitSet> keys1 = this.extractKeys(fds1, (int)attributes1.cardinality());
			Schema schema1 = new Schema(attributes1, fds1, keys1);
			schema1.setPrimaryKey(lhs);
			workingSchemata.add(schema1);
			
			// Second relation with the remaining attributes and the lhs-attributes
			BitSet attributes2 = currentSchema.getAttributes().clone();
			attributes2.andNot(rhs);
			Map<BitSet, BitSet> fds2 = this.filter(fds, attributes2);
			List<BitSet> keys2 = this.extractKeys(fds2, (int)attributes2.cardinality());
			Schema schema2 = new Schema(attributes2, fds2, keys2);
			if (Utils.andNotCount(currentSchema.getPrimaryKey(), attributes2) == 0)
				schema2.setPrimaryKey(currentSchema.getPrimaryKey());
			workingSchemata.add(schema2);
		}
		scanner.close();
		
		// Print final schemata
		System.out.println();
		finalSchemata.stream().map(schema -> this.converter.formatSchema(schema.getAttributes(), schema.getPrimaryKey())).forEach(System.out::println);
		
		return finalSchemata;
	}
	
	private Map<BitSet, BitSet> filter(Map<BitSet, BitSet> fds, BitSet attributes) {
		Map<BitSet, BitSet> filteredFds = new HashMap<>(fds.size());
		for (Entry<BitSet, BitSet> fd : fds.entrySet()) {
			BitSet lhs = fd.getKey();
			if (Utils.andNotCount(lhs, attributes) != 0)
				continue;
			
			BitSet rhs = fd.getValue().clone();
			rhs.and(attributes);
			
			if (rhs.cardinality() != 0)
				filteredFds.put(lhs, rhs);
		}
		return filteredFds;
	}
*/
	@SuppressWarnings("unused")
	private List<Result> executeNormiForBase(List<Result> allFds) throws AlgorithmExecutionException {
		System.out.println();
		System.out.println("///// Normi - Base /////");
		System.out.println();
		
		System.out.println("Building fds map ...");
		Map<BitSet, BitSet> fds = this.converter.toFunctionalDependencyMap(allFds);
		
		System.out.println("Building fds closures and keys ...");
		Map<BitSet, BitSet> fdsClosure = new HashMap<>(fds.size());
		List<BitSet> keys = new ArrayList<>();
		for (Entry<BitSet, BitSet> entry : fds.entrySet()) {
			BitSet lhs = entry.getKey();
			BitSet rhs = (BitSet) entry.getValue().clone();
			
			// Add the trivial fds to the current fd's rhs for the closure construction (we need to check against ALL the attributes in this fd to build its closure)
			rhs.or(lhs);
			
			// Extend rhs
			while (true) {
				long rhsCardinality = rhs.cardinality();
				
				for (Entry<BitSet, BitSet> other : fds.entrySet())
					if (Utils.andNotCount(other.getKey(), rhs) == 0)
						for (int rhsAttr = other.getValue().nextSetBit(0); rhsAttr >= 0; rhsAttr = other.getValue().nextSetBit(rhsAttr + 1))
							rhs.set(rhsAttr);
				
				if (rhsCardinality == rhs.cardinality())
					break;
			}
			
			// Store the lhs, if it is a key
			if (rhs.cardinality() == this.columnIdentifiers.size())
				keys.add(lhs);
			
			// Store the closure and remove the trivial fds first
			rhs.andNot(lhs);
			fdsClosure.put(lhs, rhs);
		}
		
		System.out.println("\tNumber of keys = " + keys.size());
		
		System.out.println("Filtering fds ...");
		for (Entry<BitSet, BitSet> entry : fdsClosure.entrySet()) {
			BitSet lhs = entry.getKey();
			BitSet rhs = fds.get(lhs);
			BitSet rhsClosure = entry.getValue();
			
			// Remove transitive fds
			List<BitSet> connectors = new ArrayList<>();
			connectors.add((BitSet) rhsClosure.clone());
			Set<BitSet> handledConnectors = new HashSet<>();
			
			while (!connectors.isEmpty()) {
				BitSet connector = connectors.remove(connectors.size() - 1);
				handledConnectors.add(connector);
				
				for (Entry<BitSet, BitSet> other : fdsClosure.entrySet()) {
					if ((Utils.andNotCount(other.getKey(), connector) == 0) && (!lhs.equals(other.getKey()))) {
						
						rhs.andNot(other.getValue());
						rhsClosure.andNot(other.getValue());
						
						if (!handledConnectors.contains(other.getValue()))
							connectors.add(other.getValue());
					}
				}
			}
		}
		fdsClosure = null;
		
		// TODO: Test if we can calculate the original result back ???
		
//		System.out.println("Analyzing the keys ...");
//		Analyzer analyzer = new Analyzer(this.inputGenerator, this.columnIdentifiers);
//		List<KeyStatistic> statistics = analyzer.analyze(keys);
//		for (KeyStatistic statistic : statistics)
//			System.out.println("\t" + statistic.toString() + "\t" + this.converter.format(this.converter.toColumnCombination(statistic.getKey())));
		
		System.out.println("Restoring fd format ...");
		List<Result> basis = new ArrayList<>(allFds.size());
		for (Entry<BitSet, BitSet> entry : fds.entrySet())
			basis.addAll(this.converter.toFunctionalDependencies(entry.getKey(), entry.getValue()));
		
		return basis;
	}

	public void evaluateCalculateClosure(int numEvaluationSteps) throws AlgorithmExecutionException {
		this.initialize();
		
		FdDiscoverer fdDiscoverer = new HyFDFdDiscoverer(this.converter, this.persister, this.tempResultsPath);
		Map<BitSet, BitSet> allFds = fdDiscoverer.calculateFds(this.inputGenerator, this.nullEqualsNull, true);
		
		int numAllFds = (int)allFds.values().stream().mapToLong(BitSet::cardinality).sum();
		int stepSize = (int)Math.ceil((double) numAllFds / numEvaluationSteps);
		
		for (int maxNumFds = stepSize; maxNumFds <= numAllFds; maxNumFds += stepSize) {
			System.gc();
			try {
				Thread.sleep(10000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			int numFds = 0;
			Map<BitSet, BitSet> fds = new HashMap<>(allFds.size());
			for (BitSet lhs : allFds.keySet()) {
				BitSet rhs = allFds.get(lhs);
				
				fds.put(lhs, rhs);
				
				numFds += rhs.cardinality();
				if (numFds >= maxNumFds)
					break;
			}
			System.out.println("Number of FDs for closure calculation: " + numFds);
			
//			FdExtender fdExtender = new NaiveFdExtender(this.persister, this.tempExtendedResultsPath);
//			FdExtender fdExtender = new PushingFdExtender(this.persister, this.tempExtendedResultsPath);
			FdExtender fdExtender = new PullingFdExtender(this.persister, this.tempExtendedResultsPath, this.columnIdentifiers.size(), true);
			fds = fdExtender.calculateClosure(fds, false);
			
			fds = null;
			
			System.gc();
			try {
				Thread.sleep(10000);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int findExactDuplicates() throws AlgorithmExecutionException {
		RelationalInput relationalInput;
		try {
			relationalInput = this.inputGenerator.generateNewCopy();
		}
		catch (InputGenerationException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}		
		
		System.out.println("Finding exact duplicates ...");
		List<List<String>> records = new ArrayList<List<String>>(100000);
		while (relationalInput.hasNext()) {
			List<String> record = relationalInput.next();
			records.add(record);
		}
		
		try {
			relationalInput.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		
		Comparator<List<String>> recordComparator = new Comparator<List<String>>() {
			@Override
			public int compare(List<String> o1, List<String> o2) {
				int i = 0;
				
				if ((o1 == null) || (o2 == null) || (o1.size() != o2.size()))
					throw new RuntimeException("Comparing lists of differend lengths is forbidden!");
				
				while ((i < o1.size()) && (((o1.get(i) == null) && (o2.get(i) == null)) || ((o1.get(i) != null) && (o2.get(i) != null) && o1.get(i).equals(o2.get(i)))))
					i++;
				if (i == o1.size())
					return 0;
				if (o1.get(i) == null)
					return -1;
				if (o2.get(i) == null)
					return 1;
				return o1.get(i).compareTo(o2.get(i));
			}
		};
		
		Collections.sort(records, recordComparator);
		
		int duplicates = 0;
		for (int i = 0; i < records.size(); i++) {
			for (int j = i + 1; j < records.size(); j++) {
				if (recordComparator.compare(records.get(i), records.get(j)) == 0)
					duplicates++;
				else
					break;
			}
		}
		
		return duplicates;
	}

}
