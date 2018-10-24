package de.metanome.algorithms.normalize.structures;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.normalize.utils.Utils;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class Schema {

	private int size;
	private BitSet attributes;
	private List<FunctionalDependency> fds;
	private List<FunctionalDependency> fdKeys;

	private List<FunctionalDependency> allKeys;
	private FunctionalDependency primaryKey;
	private List<Schema> referencedSchemata;

	private int[] minValueLengths;							// Length of the shortest value in each attribute
	private int[] maxValueLengths;							// Length of the longest value in each attribute
	private int[] nullValueCounts;							// Number of NULL values in each attribute
	private List<BloomFilter<CharSequence>> bloomFilters;	// Distinct value approximation for each attribute
	
	public int getSize() {
		return this.size;
	}
	
	public BitSet getAttributes() {
		return this.attributes;
	}

	public FunctionalDependency getPrimaryKey() {
		return this.primaryKey;
	}

	public void setPrimaryKey(FunctionalDependency primaryKey) {
		Optional<FunctionalDependency> pk = this.fds.stream().filter(fd -> fd.getLhs().equals(primaryKey.getLhs())).findFirst();
		this.primaryKey = pk.orElse(primaryKey);
	}

	public List<FunctionalDependency> getFdKeys() {
		return this.fdKeys;
	}
	
	public List<FunctionalDependency> getAllKeys() {
		if (this.allKeys == null)
			this.computeAllKeys();
		return this.allKeys;
	}
	
	private void computeAllKeys() {
		List<BitSet> keys = new ArrayList<>();
		LhsTree keyTree = new LhsTree(this.size);

		List<BitSet> currentLevel = new ArrayList<>();
		List<BitSet> nextLevel = new ArrayList<>();
		
		IntList extensionAttributes = new IntArrayList();
		for (int attribute = this.attributes.nextSetBit(0); attribute >= 0; attribute = this.attributes.nextSetBit(attribute + 1)) {
			if (this.nullValueCounts[attribute] == 0)
				extensionAttributes.add(attribute);
			
			BitSet attributeBitSet = new BitSet(this.size);
			attributeBitSet.set(attribute);
			currentLevel.add(attributeBitSet);
		}
		
		while (!currentLevel.isEmpty()) {
			List<BitSet> nonKeys = new ArrayList<>();
			for (BitSet keyCandidate : currentLevel) {
				if (this.isKey(keyCandidate)) {
					keys.add(keyCandidate);
					keyTree.add(keyCandidate);
				}
				else {
					nonKeys.add(keyCandidate);
				}
			}
			for (BitSet nonKey : nonKeys) {
				for (int attribute : extensionAttributes) {
					if (nonKey.get(attribute)) // Attribute should not already exist
						continue;
					if (nonKey.nextSetBit(attribute) >= 0) // Only add attributes that are larger than all the present ones to generate each candidate only once
						continue;
					BitSet newKeyCandidate = (BitSet) nonKey.clone();
					newKeyCandidate.set(attribute);
					if (keyTree.containsLhsOrSubset(newKeyCandidate)) // Check for minimality
						continue;
					nextLevel.add(newKeyCandidate);
				}
			}
			
			currentLevel = nextLevel;
			nextLevel = new ArrayList<>();
		}
		
		keyTree = null;
		this.allKeys = new ArrayList<>(keys.size());
		for (BitSet key : keys) {
			BitSet rhs = (BitSet) this.attributes.clone();
			rhs.andNot(key);
			this.allKeys.add(new FunctionalDependency(key, rhs, this));
		}
		
//		Set<FunctionalDependency> keys = new HashSet<>();
//		List<FunctionalDependency> nonKeys = new ArrayList<>();
//		
//		// Initialize
//		for (FunctionalDependency fd : this.fds) {
//			if (fd.isKey())
//				keys.add(fd);
//			else
//				nonKeys.add(fd);
//		}
//		
//		// Merge non-keys to generate keys
//		List<FunctionalDependency> currentLevel = new ArrayList<>(nonKeys);
//		List<FunctionalDependency> nextLevel = new ArrayList<>();
//		while (!currentLevel.isEmpty()) {
//			for (FunctionalDependency currentFd : currentLevel) {
//				BitSet currentAttributes = currentFd.getAttributes();
//				for (FunctionalDependency extensionFd : nonKeys) {
//					if (Utils.andNotCount(extensionFd.getLhs(), currentAttributes) == 0)
//						continue;
//					FunctionalDependency mergedFd = currentFd.merge(extensionFd);
//					if (mergedFd.isKey())
//						keys.add(mergedFd);
//					else
//						nextLevel.add(mergedFd);
//				}
//			}
//			currentLevel = nextLevel;
//			nextLevel = new ArrayList<>();
//		}
//		
//		// Delete non-minimal Keys
//		
//		this.allKeys = new ArrayList<>(keys);
	}
	
	private boolean isKey(BitSet attributes) {
		BitSet closure = (BitSet) attributes.clone();
		long previousSize;
		do {
			previousSize = closure.cardinality();
			for (FunctionalDependency fd : this.fds)
				if (Utils.andNotCount(fd.getLhs(), closure) == 0)
					closure.or(fd.getRhs());
		}
		while (closure.cardinality() != previousSize);
		return closure.equals(this.attributes);
	}

	public List<FunctionalDependency> getFds() {
		return this.fds;
	}

	public List<Schema> getReferencedSchemata() {
		return this.referencedSchemata;
	}

	public void addReferencedSchema(Schema referencedSchema) {
		this.referencedSchemata.add(referencedSchema);
	}

	public int getNumAttributes() {
		return this.attributes.cardinality();
	}

	public int[] getMinValueLengths() {
		return this.minValueLengths;
	}

	public int[] getMaxValueLengths() {
		return this.maxValueLengths;
	}

	public int[] getNullValueCounts() {
		return this.nullValueCounts;
	}

	public List<BloomFilter<CharSequence>> getBloomFilters() {
		return this.bloomFilters;
	}

	public int getMinValueLengthOf(int attribute) {
		if (this.minValueLengths[attribute] == Integer.MAX_VALUE)
			return -1;
		return this.minValueLengths[attribute];
	}

	public int getMaxValueLengthOf(int attribute) {
		if (this.minValueLengths[attribute] == Integer.MIN_VALUE)
			return -1;
		return this.maxValueLengths[attribute];
	}

	public int getNullValueCountOf(int attribute) {
		return this.nullValueCounts[attribute];
	}

	public BloomFilter<CharSequence> getBloomFilterOf(int attribute) {
		return this.bloomFilters.get(attribute);
	}
	
	public Schema(int size, BitSet attributes, FunctionalDependency primaryKey, List<FunctionalDependency> fdKeys, List<FunctionalDependency> fds, List<Schema> referencedSchemata, int[] minValueLengths, int[] maxValueLengths, int[] nullValueCounts, List<BloomFilter<CharSequence>> bloomFilters) {
		this.size = size;
		this.attributes = attributes;
		this.primaryKey = primaryKey;
		this.fdKeys = fdKeys;
		this.fds = fds;
		this.referencedSchemata = referencedSchemata;
		this.minValueLengths = minValueLengths;
		this.maxValueLengths = maxValueLengths;
		this.nullValueCounts = nullValueCounts;
		this.bloomFilters = bloomFilters;
	}

	private static RelationalInput open(RelationalInputGenerator inputGenerator) throws AlgorithmExecutionException {
		try {
			return inputGenerator.generateNewCopy();
		}
		catch (InputGenerationException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
	}
	
	private static void close(RelationalInput relationalInput) throws AlgorithmExecutionException {
		try {
			relationalInput.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
	}
	
	private static boolean isNull(String value) {
		return value == null || value.equals("") || value.toLowerCase().equals("null"); 
	}
	
	public static Schema create(RelationalInputGenerator inputGenerator, Map<BitSet, BitSet> functionalDependencies) throws AlgorithmExecutionException {
		System.out.println("Analyzing schema ...");
		
		RelationalInput relationalInput = open(inputGenerator);
		
		int numAttributes = relationalInput.numberOfColumns();
		
		BitSet attributes = new BitSet(numAttributes);
		attributes.set(0, numAttributes);
		
		int[] minValueLengths = new int[numAttributes];
		int[] maxValueLengths = new int[numAttributes];
		int[] nullValueCounts = new int[numAttributes];
		List<BloomFilter<CharSequence>> bloomFilters = new ArrayList<>(numAttributes);
		
		for (int attr = 0; attr < numAttributes; attr++) {
			minValueLengths[attr] = Integer.MAX_VALUE;
			maxValueLengths[attr] = Integer.MIN_VALUE;
			nullValueCounts[attr] = 0;
			bloomFilters.add(BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 1000000, 0.5f));
		}
		
		System.out.print("\tReading data for min, max, and bloomfilter calculations ... ");
		long time = System.currentTimeMillis();
		while (relationalInput.hasNext()) {
			List<String> record = relationalInput.next();
			
			for (int attr = 0; attr < numAttributes; attr++) {
				if (isNull(record.get(attr))) {
					nullValueCounts[attr] = nullValueCounts[attr] + 1;
				}
				else {
					minValueLengths[attr] = Math.min(minValueLengths[attr], record.get(attr).length());
					maxValueLengths[attr] = Math.max(minValueLengths[attr], record.get(attr).length());
					bloomFilters.get(attr).put(record.get(attr));
				}
			}
		}
		System.out.println("done! " + (System.currentTimeMillis() - time));
		
		close(relationalInput);
		
		List<FunctionalDependency> fds = new ArrayList<>(functionalDependencies.keySet().size());
		List<FunctionalDependency> fdKeys = new ArrayList<>();

		Schema schema = new Schema(numAttributes, attributes, null, fdKeys, fds, new ArrayList<Schema>(), minValueLengths, maxValueLengths, nullValueCounts, bloomFilters);
		
		functionalDependencies.entrySet().stream()
				.map(fd -> new FunctionalDependency(fd.getKey(), fd.getValue(), schema))
				.filter(fd -> !fd.containsNullValuesInLhs()) // Ignore the FDs that contain NULL values in their lhs, because the lhs becomes a key when used for normalization and keys cannot have NULL values
				.peek(fd -> fd.removeKeyAttributes()) // Remove the key attributes to preserve the schema's primary key constraint
				.filter(fd -> !fd.violatesConstraint()) // Ignore FDs that would destroy a foreign-key constraint when used for decomposition
				.forEach(fd -> fds.add(fd));
		
		fds.stream().filter(fd -> fd.isKey()).forEach(key -> fdKeys.add(key));

		////// This is both not true, because the keys of this schema are not all keys! ////////
/*		// If there is no FD building the key, all attributes together must be the key
		if (keys.isEmpty())
			keys.add(new FunctionalDependency(attributes.clone(), new BitSet(attributes.cardinality()), schema));		
		// If there is only one key then it must be the primary key
		if (keys.size() == 1)
			schema.setPrimaryKey(keys.get(0));
*/		////////////////////////////////////////////////////////////////////////////////////////
		
		return schema;
	}
	
	public static Schema create(BitSet attributes, Schema parentSchema) {
		List<FunctionalDependency> fds = new ArrayList<>(parentSchema.getFds().size());
		List<FunctionalDependency> fdKeys = new ArrayList<>(parentSchema.getFdKeys().size());
		
		List<Schema> referencedSchemata = parentSchema.getReferencedSchemata().stream()
				.filter(schema -> Utils.andNotCount(schema.getPrimaryKey().getLhs(), attributes) == 0)
				.collect(Collectors.toList());
		
		Schema schema = new Schema(parentSchema.getSize(), attributes, null, fdKeys, fds, referencedSchemata, parentSchema.getMinValueLengths(), parentSchema.getMaxValueLengths(), parentSchema.getNullValueCounts(), parentSchema.getBloomFilters());
		
		// Filter the FDs for the new schema from its parent schema
		parentSchema.getFds().stream()
				.map(fd -> new FunctionalDependency(fd.getLhs(), fd.getRhs(), schema))
				.filter(fd -> fd.compliesTo(attributes))
				.map(fd -> fd.restrictedCopy(attributes))
				.peek(fd -> fd.removeKeyAttributes()) // Remove the key attributes to preserve the schema's primary key constraint
				.filter(fd -> !fd.violatesConstraint()) // Ignore FDs that would destroy a foreign-key constraint when used for decomposition
				.forEach(fd -> fds.add(fd));
		
		// Identify the keys within the FDs
		fds.stream().filter(fd -> fd.isKey()).forEach(key -> fdKeys.add(key));
		
		////// This is both not true, because the keys of this schema are not all keys! ////////
/*		// If there is no FD building the key, all attributes together must be the key
		if (keys.isEmpty())
			keys.add(new FunctionalDependency(attributes.clone(), new BitSet(attributes.cardinality()), schema));
		// If there is only one key then it must be the primary key
		if (keys.size() == 1)
			schema.setPrimaryKey(keys.get(0));
*/		////////////////////////////////////////////////////////////////////////////////////////
		
		return schema;
	}

	public List<FunctionalDependency> getViolatingFds() {
//		System.out.print("\tViolating FD identification ... ");
//		long time = System.currentTimeMillis();
		
		// Build a prefix tree for the keys of this schema
		int numAttributesForTree = this.lastAttributeNumber() + 1;
		LhsTree keyTree = new LhsTree(numAttributesForTree);
		for (FunctionalDependency key : this.fdKeys)
			keyTree.add(key.getLhs());
		
		// Initialize thread pool
		int numThreads = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		
		// Submit all FDs as checking tasks
		List<Future<FunctionalDependency>> checkingResults = new ArrayList<>(this.fds.size());
		for (FunctionalDependency fd : this.fds)
			checkingResults.add(executor.submit(new CheckingTask(fd, keyTree, this.primaryKey, this.attributes, this.referencedSchemata)));
		
		// Collect the results
		List<FunctionalDependency> violatingFds = checkingResults.stream()
				.map(future -> {
					try { return future.get(); } 
					catch (InterruptedException | ExecutionException e) { e.printStackTrace(); throw new RuntimeException(e.getMessage()); }})
				.filter(fd -> fd != null)
				.collect(Collectors.toCollection(ArrayList::new));
		
		// Wait until all FDs are extended
		executor.shutdown();
		try {
			executor.awaitTermination(365, TimeUnit.DAYS);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		
//		System.out.println(System.currentTimeMillis() - time);
		return violatingFds;
	}
	
	private int lastAttributeNumber() {
		int lastAttributeNumber = -1;
		for (int attribute = this.attributes.nextSetBit(0); attribute >= 0; attribute = this.attributes.nextSetBit(attribute + 1))
			lastAttributeNumber = attribute;
		return lastAttributeNumber;
	}

	private class CheckingTask implements Callable<FunctionalDependency> {
		private FunctionalDependency fd;
		private LhsTree keyTree;
		private FunctionalDependency primaryKey;
		private BitSet attributes;
		private List<Schema> referencedSchemata;
		public CheckingTask(FunctionalDependency fd, LhsTree keyTree, FunctionalDependency primaryKey, BitSet attributes, List<Schema> referencedSchemata) {
			this.fd = fd;
			this.keyTree = keyTree;
			this.primaryKey = primaryKey;
			this.attributes = attributes;
			this.referencedSchemata = referencedSchemata;
		}
		@Override
		public FunctionalDependency call() throws Exception {
			// Ignore the FD Emptyset --> X, because then everything defines X and this cannot be used for normalization
			if (this.fd.getLhs().cardinality() == 0)
				return null;
			// Violating FDs are all those FDs whose lhs is not a (super-)key of ANY key!
			if (this.keyTree.containsLhsOrSubset(this.fd.getLhs()))
				return null;
			// Ignore FDs that would otherwise split existing foreign keys; splitting a foreign key is possible in theory but defining the foreign key attributes across tables is technically difficult, so we avoid this
			// -> if the rhs+lhs contain the full foreign key, that's fine
			// -> if R\rhs contains the full foreign key, that's also fine
			// -> everything else: not fine
			BitSet generatingSchema = (BitSet) this.fd.getLhs().clone();
			generatingSchema.or(this.fd.getRhs());
			BitSet remainingSchema = (BitSet) this.attributes.clone();
			remainingSchema.andNot(this.fd.getRhs());
			for (Schema referencedSchema : this.referencedSchemata)
				if ((Utils.andNotCount(referencedSchema.getPrimaryKey().getLhs(), generatingSchema) != 0) && (Utils.andNotCount(referencedSchema.getPrimaryKey().getLhs(), remainingSchema) != 0))
					return null;
			
			FunctionalDependency violatingFd = this.fd.clone();
			// Remove the key attributes from the violating FD's rhs, because splitting the schema's key would require finding a new key and this would confuse the user who just defined this key; because the violating FD cannot be a key itself (per definition), the rhs will not be empty afterwards
			// -> if the rhs overlaps with the key, remove that overlap; rhs cannot be empty then, because if it would reference all key attributes, it would have been a key itself and, hence, no violating fd
			// -> if the lhs overlaps with the key, that is ok, because the attributes will stay in that relation and can serve as foreign-key as well
			if (this.primaryKey != null)
				violatingFd.getRhs().andNot(this.primaryKey.getLhs());
			
			return (violatingFd.getRhs().cardinality() > 0) ? violatingFd : null;
		}
	}
	
/*	public boolean validateKeys(List<FunctionalDependency> otherKeys) {
		if (this.fdKeys.size() != otherKeys.size())
			return false;
		return this.fdKeys.containsAll(otherKeys) && otherKeys.containsAll(this.fdKeys);
	}
	
	public boolean validateFds(List<FunctionalDependency> otherFds) {
		if (this.fds.size() != otherFds.size())
			return false;
		return this.fds.containsAll(otherFds) && otherFds.containsAll(this.fds);
	}
	
	public boolean validateViolatingFds(List<FunctionalDependency> otherViolatingFds) {
		List<FunctionalDependency> violatingFds = this.getViolatingFds();
		if (violatingFds.size() != otherViolatingFds.size())
			return false;
		return violatingFds.containsAll(otherViolatingFds) && otherViolatingFds.containsAll(violatingFds);
	}
*/
	@Override
	public int hashCode() {
		return this.attributes.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Schema))
			return false;
		Schema other = (Schema) obj;
		return this.attributes.equals(other.getAttributes());
	}

	@Override
	public String toString() {
		return "[" + this.concat(this.attributes, this.maxValueLengths.length, ",") + "]";
	}
	
	private String concat(BitSet bits, int length, String separator) {
		if (bits == null)
			return "";
		
		IntArrayList ints = new IntArrayList(length);
		for (int bit = 0; bit < length; bit++)
			ints.add(bits.get(bit) ? 1 : 0);
		
		return CollectionUtils.concat(ints, separator);
	}
}
