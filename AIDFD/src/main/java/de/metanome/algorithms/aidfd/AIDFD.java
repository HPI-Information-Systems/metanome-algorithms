package de.metanome.algorithms.aidfd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;

import ch.javasoft.bitset.IBitSet;
import ch.javasoft.bitset.LongBitSet;

import de.metanome.algorithms.aidfd.results.PrefixTreeResultGen;
import de.metanome.algorithms.aidfd.helpers.Cluster;
import de.metanome.algorithms.aidfd.helpers.FastBloomFilter;
import de.metanome.algorithms.aidfd.helpers.Partition;
import de.metanome.algorithms.aidfd.helpers.StrippedPartition;
import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.algorithm_types.FunctionalDependencyAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.BooleanParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.IntegerParameterAlgorithm;
import de.metanome.algorithm_integration.algorithm_types.RelationalInputParameterAlgorithm;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirement;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementRelationalInput;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementBoolean;
import de.metanome.algorithm_integration.configuration.ConfigurationRequirementInteger;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;

public class AIDFD implements FunctionalDependencyAlgorithm,
	BooleanParameterAlgorithm, IntegerParameterAlgorithm, RelationalInputParameterAlgorithm {
	
	public static final String INPUT_RELATION_FILE = "input file";
	public static final String INPUT_USE_BLOOMFILTER = "use bloomfilter";
	public static final String INPUT_CHECK_CORRECTNESS = "check correctness";
	public static final String INPUT_UNTIL_ITERATION_K = "until iteration k";
	public static final String INPUT_TIMEOUT = "timeout [s]";
	public static final String INPUT_NEG_COVER_THRESH = "neg-cover growth thresh [x/1000000]";
	public static final String INPUT_NEG_COVER_WINDOW_SIZE = "neg-cover growth window size";

	private FunctionalDependencyResultReceiver resultReceiver;
	private RelationalInputGenerator inputGenerator;

	/* Configuration parameters */
	private boolean useBloomfilter;
	private boolean checkCorrectness;
	private int untilIterationK = -1;
	private int timeout = -1;
	private double negCoverThresh = -1;
	private int negCoverWindowSize = 1;
	
	/* Data Structures */
	/** For each cell, stores a reference to the cluster */
	private ArrayList<Cluster[]> clusters;
	/** For each cell, stores its index in its cluster */
	private ArrayList<int[]> clusterIndices;
	/** For each tuple, stores whether it still needs to be looked at */

	private int numberTuples;
	private int numberAttributes;

	private long startTime;
	private PrefixTreeResultGen resultGenerator;
	private FastBloomFilter bloomFilter;
	private double[] lastNegCoverRatios;
	private IBitSet constantColumns;

	@Override
	public void execute() throws AlgorithmExecutionException {
		startTime = System.currentTimeMillis();

		RelationalInput input = inputGenerator.generateNewCopy();
		resultGenerator = new PrefixTreeResultGen(input, resultReceiver, checkCorrectness);

		readInput(input);

		checkConstantColumns();
		checkClusters();

		resultGenerator.generateResults();
		
		System.out.println("Finished execution after " + (System.currentTimeMillis() - startTime) + "ms");
	}

	@SuppressWarnings("unchecked")
	private void readInput(RelationalInput input) throws InputGenerationException, InputIterationException {
		clusters = new ArrayList<>();
		clusterIndices = new ArrayList<>();

		long time = System.currentTimeMillis();

		Map<String, Cluster> map[] = new HashMap[input.numberOfColumns()];
		for (int i = 0; i < input.numberOfColumns(); ++i) {
			map[i] = new HashMap<String, Cluster>();
		}

		int lineNumber = 0;
		while (input.hasNext()) {
			List<String> line = input.next();

			Cluster[] lineClusters = new Cluster[input.numberOfColumns()];
			int[] lineClusterIndices = new int[input.numberOfColumns()];

			for (int i = 0; i < input.numberOfColumns(); ++i) {
				Cluster cluster;
				if (map[i].containsKey(line.get(i))) {
					cluster = map[i].get(line.get(i));
				} else {
					cluster = new Cluster(i);
					map[i].put(line.get(i), cluster);
				}
				lineClusters[i] = cluster;
				lineClusterIndices[i] = cluster.size();
				cluster.add(lineNumber);
			}

			clusters.add(lineClusters);
			clusterIndices.add(lineClusterIndices);

			++lineNumber;
		}

		numberTuples = lineNumber;
		numberAttributes = input.numberOfColumns();

		if (checkCorrectness) {
			StrippedPartition.columns = new ArrayList[numberAttributes];
			for (int i = 0; i < numberAttributes; ++i) {
				StrippedPartition.columns[i] = new ArrayList<Partition>();
				for (Cluster c : map[i].values()) {
					if (c.size() > 1)
						StrippedPartition.columns[i].add(new Partition(c));
				}
			}

			StrippedPartition.clusters = clusters;
		}

		System.out.println("Reading data finished after " + (System.currentTimeMillis() - time) + "ms");
		System.out.println("Initial neg-cover size: " + resultGenerator.getNegCoverSize());
	}

	private int pseudoRandom(int n, int k) {
		int prim = 10619863 % n;
		return (prim * k) % n;
	}

	private boolean makeCheck(int tuple, int k) {
		boolean madeCheck = false;

		Cluster[] tupleClusters = clusters.get(tuple);
		int[] tupleIndices = clusterIndices.get(tuple);

		for (int i = 0; i < numberAttributes; i++) {
			// we don't need to sample inside a constant column
			if(constantColumns.get(i))
				continue;

			Cluster cluster = tupleClusters[i];
			int index = tupleIndices[i];

			// all tuples in question are checked already
			if (index < k) {
				continue;
			}

			int otherTuple = cluster.get(pseudoRandom(index, k));

			// use bloomFilter to avoid duplicate checks
			if(useBloomfilter && bloomFilter.containsAndAdd(((long)tuple << 32) + otherTuple)) {
			 continue;
			}

			madeCheck = true;

			IBitSet bitset = LongBitSet.FACTORY.create(numberAttributes);
			Cluster[] otherTupleClusters = clusters.get(otherTuple);

			for (int j = 0; j < numberAttributes; j++) {
				bitset.set(j, tupleClusters[j] == otherTupleClusters[j]);
			}

			resultGenerator.add(bitset);
		}

		return madeCheck;
	}

	private void checkConstantColumns() {
		constantColumns = LongBitSet.FACTORY.create();
		for (int i = 0; i < numberAttributes; ++i) {
			if(clusters.get(0)[i].size() == numberTuples) {
				constantColumns.set(i);
			}
		}
		resultGenerator.setConstantColumns(constantColumns);
	}

	private void checkClusters() {
		if (useBloomfilter) {
		bloomFilter = new FastBloomFilter(numberTuples * numberTuples / 10, 2);
		}

		if(negCoverThresh >= 0) {
			lastNegCoverRatios = new double[negCoverWindowSize];
			Arrays.fill(lastNegCoverRatios, 1);
		}

		double negCoverRatio;
		boolean madeCheck;

		// Iterate over k
		int k = 0;
		do {
			k++;
			long time = System.currentTimeMillis();
			int lastNegCoverSize = resultGenerator.getNegCoverSize();
			madeCheck = false;

			for (int tuple = 0; tuple < numberTuples; tuple++) {
				madeCheck |= makeCheck(tuple, k);
				// If it didn't do anything in this iteration, it won't in the next either
			}
			
			negCoverRatio = lastNegCoverSize > 0.0
				? (double)(resultGenerator.getNegCoverSize() - lastNegCoverSize) / lastNegCoverSize
			: Double.MAX_VALUE;

			System.out.println("k=" + k +
				" in " + (System.currentTimeMillis() - time) + "ms -" +
				" negCoverSize: " + resultGenerator.getNegCoverSize() + "," +
				" negCoverRatio: " + negCoverRatio);
		} while((useBloomfilter || madeCheck) && k < numberTuples && !terminationCriteriaMet(k, negCoverRatio));
	}

	private boolean terminationCriteriaMet(int k, double negCoverRatio) {
		if (untilIterationK >= 0 && k >= untilIterationK) {
			System.out.println("Termination criterion met: until iteration k");
			return true;
		}
		
		if (timeout >= 0) {
			long timeDiff = System.currentTimeMillis() - startTime;
			if(timeDiff / 1000 >= timeout) {
				System.out.println("Termination criterion met: timeout");
				return true;
			}
		}
		
		if (negCoverThresh >= 0.0) {
			int index = k % negCoverWindowSize;
			lastNegCoverRatios[index] = negCoverRatio;
			double averageRatio = (DoubleStream.of(lastNegCoverRatios).sum()) / (double)negCoverWindowSize;
			if (averageRatio <= negCoverThresh) {
				System.out.println("Termination criterion met: neg-cover growth ratio");
				return true;
			}
		}

		return false;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public ArrayList<ConfigurationRequirement<?>> getConfigurationRequirements() {
		ArrayList<ConfigurationRequirement<?>> reqs = new ArrayList<ConfigurationRequirement<?>>();
		reqs.add(new ConfigurationRequirementRelationalInput(INPUT_RELATION_FILE));
		reqs.add(new ConfigurationRequirementBoolean(INPUT_USE_BLOOMFILTER));
		reqs.add(new ConfigurationRequirementBoolean(INPUT_CHECK_CORRECTNESS));
		reqs.add(new ConfigurationRequirementInteger(INPUT_UNTIL_ITERATION_K));
		reqs.add(new ConfigurationRequirementInteger(INPUT_TIMEOUT));
		reqs.add(new ConfigurationRequirementInteger(INPUT_NEG_COVER_THRESH));
		reqs.add(new ConfigurationRequirementInteger(INPUT_NEG_COVER_WINDOW_SIZE));
		
		for(ConfigurationRequirement req : reqs) {
			req.setRequired(false);
		}
		reqs.get(0).setRequired(true);

		return reqs;
	}

	@Override
	public void setResultReceiver(FunctionalDependencyResultReceiver resultReceiver) {
		this.resultReceiver = resultReceiver;
	}

	@Override
	public void setRelationalInputConfigurationValue(String identifier, RelationalInputGenerator... values)
		throws AlgorithmConfigurationException {
		if (identifier.equals(INPUT_RELATION_FILE)) {
			this.inputGenerator = values[0];
		}
	}
	
	@Override
	public void setBooleanConfigurationValue(String identifier, Boolean... values)
		throws AlgorithmConfigurationException {
		if (values.length == 0) {
			return;
		}

		if (identifier.equals(INPUT_USE_BLOOMFILTER)) {
			this.useBloomfilter = values[0];
		}

		if (identifier.equals(INPUT_CHECK_CORRECTNESS)) {
			this.checkCorrectness = values[0];
		}
	}

	@Override
	public void setIntegerConfigurationValue(String identifier, Integer... values)
		throws AlgorithmConfigurationException {
		if (values.length == 0) {
			return;
		}

		if (identifier.equals(INPUT_UNTIL_ITERATION_K)) {
			this.untilIterationK = values[0];
		}

		if (identifier.equals(INPUT_TIMEOUT)) {
			this.timeout = values[0];
		}

		if (identifier.equals(INPUT_NEG_COVER_THRESH)) {
			this.negCoverThresh = (double)values[0] / 1000000.0;
		}

		if (identifier.equals(INPUT_NEG_COVER_WINDOW_SIZE)) {
			this.negCoverWindowSize = values[0];
		}
	}

}
