package de.metanome.algorithms.binder.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.lucene.util.OpenBitSet;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.algorithms.binder.io.FileInputIterator;
import de.metanome.algorithms.binder.io.InputIterator;
import de.metanome.algorithms.binder.io.SqlInputIterator;
import de.metanome.algorithms.binder.structures.Attribute;
import de.metanome.algorithms.binder.structures.AttributeCombination;
import de.metanome.algorithms.binder.structures.IntSingleLinkedList;
import de.metanome.algorithms.binder.structures.IntSingleLinkedList.ElementIterator;
import de.metanome.algorithms.binder.structures.Level;
import de.metanome.algorithms.binder.structures.PruningStatistics;
import de.uni_potsdam.hpi.dao.DataAccessObject;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.DatabaseUtils;
import de.uni_potsdam.hpi.utils.FileUtils;
import de.uni_potsdam.hpi.utils.LoggingUtils;
import de.uni_potsdam.hpi.utils.MeasurementUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.longs.LongArrayList;

// Bucketing IND ExtractoR (BINDER)
public class BINDER {

	protected DatabaseConnectionGenerator databaseConnectionGenerator = null;
	protected RelationalInputGenerator[] fileInputGenerator = null;
	protected InclusionDependencyResultReceiver resultReceiver = null;
	protected DataAccessObject dao = null;
	protected String[] tableNames = null;
	protected String databaseName = null;
	protected String tempFolderPath = "BINDER_temp"; // TODO: Use Metanome temp file functionality here (interface TempFileAlgorithm)
	protected boolean cleanTemp = true;
	protected boolean detectNary = false;
	protected boolean filterKeyForeignkeys = false;
	protected int maxNaryLevel = -1;
	protected int inputRowLimit = -1;
	protected int numBucketsPerColumn = 10; // Initial number of buckets per column
	protected int memoryCheckFrequency = 100; // Number of new, i.e., so far unseen values during bucketing that trigger a memory consumption check
	protected int maxMemoryUsagePercentage = 60; // The algorithm spills to disc if memory usage exceeds X% of available memory

	private int overheadPerValueForIndexes = 64; // Bytes that each value requires in the comparison phase for the indexes
	private int numColumns;
	private long availableMemory;
	private long maxMemoryUsage;

	private File tempFolder = null;
	
	private Int2ObjectOpenHashMap<List<List<String>>> attribute2subBucketsCache = null;
	
	private int[] tableColumnStartIndexes = null;
	private List<String> columnNames = null;
	private List<String> columnTypes = null;
	
	private Int2ObjectOpenHashMap<IntSingleLinkedList> dep2ref = null;
	private int numUnaryINDs = 0;
	
	private Map<AttributeCombination, List<AttributeCombination>> naryDep2ref = null;
	private int[] column2table = null;
	private String valueSeparator = "#";
	private int numNaryINDs = 0;
	
	private OpenBitSet nullValueColumns;
	
	private long unaryStatisticTime = -1;
	private long unaryLoadTime = -1;
	private long unaryCompareTime = -1;
	private LongArrayList naryGenerationTime = null;
	private LongArrayList naryLoadTime = null;
	private LongArrayList naryCompareTime = null;
	private long outputTime = -1;

	private IntArrayList activeAttributesPerBucketLevel;
	private IntArrayList naryActiveAttributesPerBucketLevel;
	private int[] spillCounts = null;
	private List<int[]> narySpillCounts = null;
	private int[] refinements = null;
	private List<int[]> naryRefinements = null;
	private int[] bucketComparisonOrder = null;
	private LongArrayList columnSizes = null;
	
	private PruningStatistics pruningStatistics = null;
	
	@Override
	public String toString() {
		String input = "-";
		if (this.databaseConnectionGenerator != null) 
			input = this.databaseConnectionGenerator.getClass().getName();
		else if (this.fileInputGenerator != null)
			input = this.fileInputGenerator[0].getClass().getName() + " (" + this.fileInputGenerator.length + ")";
		 
		return "BINDER: \r\n\t" +
				"input: " + input + "\r\n\t" +
				"dao: " + ((this.dao != null) ? this.dao.getClass().getName() : "-") + "\r\n\t" +
				"databaseName: " + this.databaseName + "\r\n\t" +
				"inputRowLimit: " + this.inputRowLimit + "\r\n\t" +
				"resultReceiver: " + ((this.resultReceiver != null) ? this.resultReceiver.getClass().getName() : "-") + "\r\n\t" +
				"tempFolderPath: " + this.tempFolder.getPath() + "\r\n\t" +
				"tableNames: " + ((this.tableNames != null) ? CollectionUtils.concat(this.tableNames, ", ") : "-") + "\r\n\t" +
				"numColumns: " + this.numColumns + " (" + ((this.spillCounts != null) ? String.valueOf(CollectionUtils.countNotN(this.spillCounts, 0)) : "-") + " spilled)\r\n\t" +
				"numBucketsPerColumn: " + this.numBucketsPerColumn + "\r\n\t" +
				"bucketComparisonOrder: " + ((this.bucketComparisonOrder != null) ? CollectionUtils.concat(this.bucketComparisonOrder, ", ") : "-") + "\r\n\t" +
				"memoryCheckFrequency: " + this.memoryCheckFrequency + "\r\n\t" +
				"maxMemoryUsagePercentage: " + this.maxMemoryUsagePercentage + "%\r\n\t" +
				"availableMemory: " + this.availableMemory + " byte (spilled when exeeding " + this.maxMemoryUsage + " byte)\r\n\t" +
				"numBucketsPerColumn: " + this.numBucketsPerColumn + "\r\n\t" +
				"memoryCheckFrequency: " + this.memoryCheckFrequency + "\r\n\t" +
				"cleanTemp: " + this.cleanTemp + "\r\n\t" +
				"detectNary: " + this.detectNary + "\r\n\t" +
				"numUnaryINDs: " + this.numUnaryINDs + "\r\n\t" +
				"numNaryINDs: " + this.numNaryINDs + "\r\n\t" +
			"\r\n" +
			"nullValueColumns: " + this.toString(this.nullValueColumns) + 
			"\r\n" +
			((this.pruningStatistics != null) ? String.valueOf(this.pruningStatistics.getPrunedCombinations()) : "-") + " candidates pruned by statistical pruning\r\n\t" +
			"\r\n" +
			"columnSizes: " + ((this.columnSizes != null) ? CollectionUtils.concat(this.columnSizes, ", ") : "-") + "\r\n" +	
			"numEmptyColumns: " + ((this.columnSizes != null) ? String.valueOf(CollectionUtils.countN(this.columnSizes, 0)) : "-") + "\r\n" +
			"\r\n" +
			"activeAttributesPerBucketLevel: " + ((this.activeAttributesPerBucketLevel != null) ? CollectionUtils.concat(this.activeAttributesPerBucketLevel, ", ") : "-") + "\r\n" +	
			"naryActiveAttributesPerBucketLevel: " + ((this.naryActiveAttributesPerBucketLevel == null) ? "-" : CollectionUtils.concat(this.naryActiveAttributesPerBucketLevel, ", ")) + "\r\n" +	
			"\r\n" +
			"spillCounts: " + ((this.spillCounts != null) ? CollectionUtils.concat(this.spillCounts, ", ") : "-") + "\r\n" +	
			"narySpillCounts: " + ((this.narySpillCounts == null) ? "-" : CollectionUtils.concat(this.narySpillCounts, ", ", "\r\n")) + "\r\n" + 
			"\r\n" +
			"refinements: " + ((this.refinements != null) ? CollectionUtils.concat(this.refinements, ", ") : "-") + "\r\n" +	
			"naryRefinements: " + ((this.naryRefinements == null) ? "-" : CollectionUtils.concat(this.naryRefinements, ", ", "\r\n")) + "\r\n" + 
			"\r\n" +
			"unaryStatisticTime: " + this.unaryStatisticTime + "\r\n" +
			"unaryLoadTime: " + this.unaryLoadTime + "\r\n" +
			"unaryCompareTime: " + this.unaryCompareTime + "\r\n" +
			"naryGenerationTime: " + this.naryGenerationTime + "\r\n" +
			"naryLoadTime: " + this.naryLoadTime + "\r\n" +
			"naryCompareTime: " + this.naryCompareTime + "\r\n" +
			"outputTime: " + this.outputTime;
	}
	
	private String toString(OpenBitSet o) {
		StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < o.length(); i++)
			builder.append((o.get(i) == true) ? 1 : 0);
		builder.append("]");
		return builder.toString();
	}

	protected String getAuthorName() {
		return "Thorsten Papenbrock";
	}

	protected String getDescriptionText() {
		return "Divide and Conquer-based IND discovery";
	}

	public void execute() throws AlgorithmExecutionException {
		// Disable Logging (FastSet sometimes complains about skewed key distributions with lots of WARNINGs)
		LoggingUtils.disableLogging();
		
		try {
			////////////////////////////////////////////////////////
			// Phase 0: Initialization (Collect basic statistics) //
			////////////////////////////////////////////////////////
			this.unaryStatisticTime = System.currentTimeMillis();
			this.initialize();
			this.unaryStatisticTime = System.currentTimeMillis() - this.unaryStatisticTime;
			
			//////////////////////////////////////////////////////
			// Phase 1: Bucketing (Create and fill the buckets) //
			//////////////////////////////////////////////////////
			this.unaryLoadTime = System.currentTimeMillis();
			this.bucketize();
			this.unaryLoadTime = System.currentTimeMillis() - this.unaryLoadTime;
			
			//////////////////////////////////////////////////////
			// Phase 2: Checking (Check INDs using the buckets) //
			//////////////////////////////////////////////////////
			this.unaryCompareTime = System.currentTimeMillis();
			//this.checkViaHashing();
			//this.checkViaSorting();
			//this.checkViaTwoStageIndexAndBitSets();
			this.checkViaTwoStageIndexAndLists();
			this.unaryCompareTime = System.currentTimeMillis() - this.unaryCompareTime;
			
			/////////////////////////////////////////////////////////
			// Phase 3: N-ary IND detection (Find INDs of size > 1 //
			/////////////////////////////////////////////////////////
			if (this.detectNary && (this.maxNaryLevel > 1 || this.maxNaryLevel <= 0))
				this.detectNaryViaBucketing();
				//this.detectNaryViaSingleChecks();
			
			//////////////////////////////////////////////////////
			// Phase 4: Output (Return and/or write the results //
			//////////////////////////////////////////////////////
			this.outputTime = System.currentTimeMillis();
			this.output();
			this.outputTime = System.currentTimeMillis() - this.outputTime;
			
			System.out.println(this.toString());
			System.out.println();
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		finally {
			FileUtils.close(this.databaseConnectionGenerator);
			
			// Clean temp
			if (this.cleanTemp)
				FileUtils.cleanDirectory(this.tempFolder);
		}
	}
	
	private void initialize() throws InputGenerationException, SQLException, InputIterationException, AlgorithmConfigurationException {
		System.out.println("Initializing ...");
		
		// Ensure the presence of an input generator
		if ((this.databaseConnectionGenerator == null) && (this.fileInputGenerator == null))
			throw new InputGenerationException("No input generator specified!");
		
		// Initialize temp folder
		this.tempFolder = new File(this.tempFolderPath + File.separator + "temp");
		
		// Clean temp if there are files from previous runs that may pollute this run
		FileUtils.cleanDirectory(this.tempFolder);
		
		// Initialize memory management
		this.availableMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		this.maxMemoryUsage = (long)(this.availableMemory * (this.maxMemoryUsagePercentage / 100.0f));
		
		// Query meta data for input tables
		this.tableColumnStartIndexes = new int[this.tableNames.length];
		this.columnNames = new ArrayList<String>();
		this.columnTypes = new ArrayList<String>();
		
		for (int tableIndex = 0; tableIndex < this.tableNames.length; tableIndex++) {
			this.tableColumnStartIndexes[tableIndex] = this.columnNames.size();
			
			if (this.databaseConnectionGenerator != null)
				this.collectStatisticsFrom(this.databaseConnectionGenerator, tableIndex);
			else
				this.collectStatisticsFrom(this.fileInputGenerator[tableIndex]);
		}
		
		this.numColumns = this.columnNames.size();

		this.activeAttributesPerBucketLevel = new IntArrayList(this.numBucketsPerColumn);
		
		this.spillCounts = new int[this.numColumns];
		for (int columnNumber = 0; columnNumber < this.numColumns; columnNumber++)
			this.spillCounts[columnNumber] = 0;
		
		this.refinements = new int[this.numBucketsPerColumn];
		for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++)
			this.refinements[bucketNumber] = 0;
		
		this.nullValueColumns = new OpenBitSet(this.columnNames.size());
		
		this.pruningStatistics = new PruningStatistics();
		
		// Build an index that assigns the columns to their tables, because the n-ary detection can only group those attributes that belong to the same table and the foreign key detection also only groups attributes from different tables.
		this.column2table = new int[this.numColumns];
		int table = 0;
		for (int i = 0; i < this.tableColumnStartIndexes.length; i++) {
			int currentStart = this.tableColumnStartIndexes[i];
			int nextStart = ((i + 1) == this.tableColumnStartIndexes.length) ? this.numColumns : this.tableColumnStartIndexes[i + 1];
			
			for (int j = currentStart; j < nextStart; j++)
				this.column2table[j] = table;
			table++;
		}
	}
	
	private void collectStatisticsFrom(DatabaseConnectionGenerator inputGenerator, int tableIndex) throws InputGenerationException, AlgorithmConfigurationException {
		ResultSet resultSet = null;
		try {
			// Query attribute names and types
			resultSet = inputGenerator.generateResultSetFromSql(this.dao.buildColumnMetaQuery(this.databaseName, this.tableNames[tableIndex]));
			this.dao.extract(this.columnNames, new ArrayList<String>(), this.columnTypes, resultSet);
		}
		catch (SQLException e) {
			e.printStackTrace();
			throw new InputGenerationException(e.getMessage());
		}
		finally {
			DatabaseUtils.close(resultSet);
			try {
				inputGenerator.closeAllStatements();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void collectStatisticsFrom(RelationalInputGenerator inputGenerator) throws InputIterationException, InputGenerationException, AlgorithmConfigurationException {
		RelationalInput input = null;
		try {
			// Query attribute names and types
			input = inputGenerator.generateNewCopy();
			for (String columnName : input.columnNames()) {
				this.columnNames.add(columnName);
				this.columnTypes.add("String"); // TODO: Column types as parameter or from the file?
			}
		}
		finally {
			FileUtils.close(input);
		}
	}
	
	private void bucketize() throws InputGenerationException, InputIterationException, IOException, AlgorithmConfigurationException {
		System.out.print("Bucketizing ... ");
		
		// Initialize the counters that count the empty buckets per bucket level to identify sparse buckets and promising bucket levels for comparison
		int[] emptyBuckets = new int[this.numBucketsPerColumn];
		for (int levelNumber = 0; levelNumber < this.numBucketsPerColumn; levelNumber++)
			emptyBuckets[levelNumber] = 0;
		
		// Initialize aggregators to measure the size of the columns
		this.columnSizes = new LongArrayList(this.numColumns);
		for (int column = 0; column < this.numColumns; column++)
			this.columnSizes.add(0);
		
		for (int tableIndex = 0; tableIndex < this.tableNames.length; tableIndex++) {
			String tableName = this.tableNames[tableIndex];
			System.out.print(tableName + " ");
			
			int numTableColumns = (this.tableColumnStartIndexes.length > tableIndex + 1) ? this.tableColumnStartIndexes[tableIndex + 1] - this.tableColumnStartIndexes[tableIndex] : this.numColumns - this.tableColumnStartIndexes[tableIndex];
			int startTableColumnIndex = this.tableColumnStartIndexes[tableIndex];
			
			// Initialize buckets
			List<List<Set<String>>> buckets = new ArrayList<List<Set<String>>>(numTableColumns);
			for (int columnNumber = 0; columnNumber < numTableColumns; columnNumber++) {
				List<Set<String>> attributeBuckets = new ArrayList<Set<String>>();
				for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++)
					attributeBuckets.add(new HashSet<String>());
				buckets.add(attributeBuckets);
			}
			
			// Initialize value counters
			int numValuesSinceLastMemoryCheck = 0;
			int[] numValuesInColumn = new int[this.numColumns];
			for (int columnNumber = 0; columnNumber < numTableColumns; columnNumber++)
				numValuesInColumn[columnNumber] = 0;

			// Load data
			InputIterator inputIterator = null;
			try {
				if (this.databaseConnectionGenerator != null)
					inputIterator = new SqlInputIterator(this.databaseConnectionGenerator, this.dao, tableName, this.inputRowLimit);
				else
					inputIterator = new FileInputIterator(this.fileInputGenerator[tableIndex], this.inputRowLimit);
				
				while (inputIterator.next()) {
					for (int columnNumber = 0; columnNumber < numTableColumns; columnNumber++) {
						String value = inputIterator.getValue(columnNumber);
						
						//value = new StringBuilder(value).reverse().toString(); // This is an optimization if urls with long, common prefixes are used to later improve the comparison values
						
						if (value == null) {
							this.nullValueColumns.set(startTableColumnIndex + columnNumber);
							continue;
						}
						
						// Bucketize
						int bucketNumber = this.calculateBucketFor(value);
						if (buckets.get(columnNumber).get(bucketNumber).add(value)) {
							numValuesSinceLastMemoryCheck++;
							numValuesInColumn[columnNumber] = numValuesInColumn[columnNumber] + 1;
						}
						//this.pruningStatistics.addValue(startTableColumnIndex + columnNumber, bucketNumber, value); // TODO: Remove?
						
						// Occasionally check the memory consumption
						if (numValuesSinceLastMemoryCheck >= this.memoryCheckFrequency) {
							numValuesSinceLastMemoryCheck = 0;
							
							// Spill to disk if necessary
							while (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() > this.maxMemoryUsage) {								
								// Identify largest buffer
								int largestColumnNumber = 0;
								int largestColumnSize = numValuesInColumn[largestColumnNumber];
								for (int otherColumnNumber = 1; otherColumnNumber < numTableColumns; otherColumnNumber++) {
									if (largestColumnSize < numValuesInColumn[otherColumnNumber]) {
										largestColumnNumber = otherColumnNumber;
										largestColumnSize = numValuesInColumn[otherColumnNumber];
									}
								}
								
								// Write buckets from largest column to disk and empty written buckets
								int globalLargestColumnIndex = startTableColumnIndex + largestColumnNumber;
								for (int largeBucketNumber = 0; largeBucketNumber < this.numBucketsPerColumn; largeBucketNumber++) {
									this.writeBucket(globalLargestColumnIndex, largeBucketNumber, -1, buckets.get(largestColumnNumber).get(largeBucketNumber));
									buckets.get(largestColumnNumber).set(largeBucketNumber, new HashSet<String>());
								}
								numValuesInColumn[largestColumnNumber] = 0;
								
								this.spillCounts[globalLargestColumnIndex] = this.spillCounts[globalLargestColumnIndex] + 1;
								
								System.gc();
							}
						}
					}
				}
			}
			finally {
				FileUtils.close(inputIterator);
			}
			
			// Write buckets to disk
			for (int columnNumber = 0; columnNumber < numTableColumns; columnNumber++) {
				int globalColumnIndex = startTableColumnIndex + columnNumber;
				if (this.spillCounts[globalColumnIndex] == 0) { // if a column was spilled to disk, we do not count empty buckets for this column, because the partitioning distributes the values evenly and hence all buckets should have been populated
					for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++) {
						Set<String> bucket = buckets.get(columnNumber).get(bucketNumber);
						if (bucket.size() != 0)
							this.writeBucket(globalColumnIndex, bucketNumber, -1, bucket);
						else
							emptyBuckets[bucketNumber] = emptyBuckets[bucketNumber] + 1;
					}
				}
				else {
					for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++) {
						Set<String> bucket = buckets.get(columnNumber).get(bucketNumber);
						if (bucket.size() != 0)
							this.writeBucket(globalColumnIndex, bucketNumber, -1, bucket);
					}
				}
			}
		}
		
		// Calculate the bucket comparison order from the emptyBuckets to minimize the influence of sparse-attribute-issue
		this.calculateBucketComparisonOrder(emptyBuckets);
		
		System.out.println();
	}
		
	private void checkViaHashing() throws IOException {
		/////////////////////////////////////////////////////////
		// Phase 2.1: Pruning (Dismiss first candidates early) //
		/////////////////////////////////////////////////////////

		// Setup the initial INDs using the first buckets
		int[] refCounts = new int[this.numColumns];
		this.dep2ref = new Int2ObjectOpenHashMap<IntSingleLinkedList>(this.numColumns);
		Int2ObjectOpenHashMap<Set<String>> column2bucket = new Int2ObjectOpenHashMap<Set<String>>(this.numColumns);
		
		for (int globalColumnIndex = 0; globalColumnIndex < this.numColumns; globalColumnIndex++) {
			refCounts[globalColumnIndex] = 0;
			this.dep2ref.put(globalColumnIndex, new IntSingleLinkedList());
		}
		
		for (int c1 = 0; c1 < this.numColumns; c1++) {
			for (int c2 = c1 + 1; c2 < this.numColumns; c2++) {
				// Columns of different type cannot be included in each other
				if (!DatabaseUtils.matchSameDataTypeClass(this.columnTypes.get(c1), this.columnTypes.get(c2)))
					continue;
				
				// c1 > c2 ?
				if (this.pruningStatistics.isValid(c2, c1)) {
					refCounts[c1] = refCounts[c1] + 1;
					this.dep2ref.get(c2).add(c1);
				}
				
				// c2 > c1 ?
				if (this.pruningStatistics.isValid(c1, c2)) {
					refCounts[c2] = refCounts[c2] + 1;
					this.dep2ref.get(c1).add(c2);
				}
			}
		}
		
		for (int column = 0; column < this.numColumns; column++)
			if (!(this.dep2ref.containsKey(column)) && (refCounts[column] == 0))
				column2bucket.remove(column);
		
		//////////////////////////////////////////////////////////////
		// Phase 2.2: Validation (Successively check all candidates //
		//////////////////////////////////////////////////////////////

		// Iterate the buckets for all remaining INDs until the end is reached or no more INDs exist)
		BitSet activeAttributes = new BitSet(this.numColumns);
		activeAttributes.set(0, this.numColumns);
		for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++) {
			// Refine the current bucket level if it does not fit into memory at once
			int[] subBucketNumbers = this.refineBucketLevel(activeAttributes, 0, bucketNumber);
			for (int subBucketNumber : subBucketNumbers) {
				if (column2bucket.keySet().isEmpty())
					break;
				
				// Load next bucket level
				for (int globalColumnIndex : column2bucket.keySet())
					column2bucket.put(globalColumnIndex, this.readBucketAsSet(globalColumnIndex, bucketNumber, subBucketNumber)); // Reading buckets into Sets eliminates all duplicates within these buckets
				
				// Check INDs
				IntList deps = new IntArrayList(this.dep2ref.keySet());
				for (int dep : deps) {
					Set<String> depBucket = column2bucket.get(dep);
					
					ElementIterator refIterator = this.dep2ref.get(dep).elementIterator();
					while (refIterator.hasNext()) {
						int ref = refIterator.next();
						Set<String> refBucket = column2bucket.get(ref);
						
						if ((depBucket.size() > refBucket.size()) || (!refBucket.containsAll(depBucket))) {
							refCounts[ref] = refCounts[ref] - 1;
							refIterator.remove();
						}
					}
					
					if (this.dep2ref.get(dep).isEmpty())
						this.dep2ref.remove(dep);
				}
				
				for (int column = 0; column < this.numColumns; column++)
					if (!(this.dep2ref.containsKey(column)) && (refCounts[column] == 0))
						column2bucket.remove(column);
			}
		}
	}
	
	private void checkViaSorting() throws IOException {
		/////////////////////////////////////
		// Phase 2: Pruning and Validation //
		/////////////////////////////////////
		
		// Setup the initial INDs
		Int2ObjectOpenHashMap<Attribute> attributeId2attributeObject = new Int2ObjectOpenHashMap<Attribute>(this.numColumns);
		PriorityQueue<Attribute> attributeObjectQueue = new PriorityQueue<Attribute>(this.numColumns);
		IntArrayList activeAttributes = new IntArrayList(this.numColumns);
		
		for (int globalColumnIndex = 0; globalColumnIndex < this.numColumns; globalColumnIndex++) {
			Attribute attribute = new Attribute(globalColumnIndex, this.columnTypes, this.pruningStatistics);
			attributeId2attributeObject.put(globalColumnIndex, attribute);
			
			if (!attribute.isPruneable())
				activeAttributes.add(globalColumnIndex);
		}
		
		// Validate INDs
		for (int bucketNumber : this.bucketComparisonOrder) {
			// Refine the current bucket level if it does not fit into memory at once
			int[] subBucketNumbers = this.refineBucketLevel(activeAttributes, bucketNumber);
			for (int subBucketNumber : subBucketNumbers) {
				this.activeAttributesPerBucketLevel.add(activeAttributes.size());
				if (activeAttributes.isEmpty())
					break;
				
				// Load next bucket layer
				for (int globalColumnIndex : activeAttributes) {
					Attribute attribute = attributeId2attributeObject.get(globalColumnIndex);
					attribute.setValues(this.readBucketAsList(globalColumnIndex, bucketNumber, subBucketNumber));
					
					if (!attribute.hasFinished())
						attributeObjectQueue.add(attribute);
				}
				
				// Validate INDs on current bucket layer
				IntArrayList topAttributes = new IntArrayList(this.numColumns);
				while (!attributeObjectQueue.isEmpty()) {
					Attribute topAttribute = attributeObjectQueue.remove();
					topAttributes.add(topAttribute.getAttributeId());
					while ((!attributeObjectQueue.isEmpty()) && topAttribute.getCurrentValue().equals(attributeObjectQueue.peek().getCurrentValue()))
						topAttributes.add(attributeObjectQueue.remove().getAttributeId());
					
					for (int attribute : topAttributes)
						attributeId2attributeObject.get(attribute).intersectReferenced(topAttributes, attributeId2attributeObject);
					
					for (int attribute : topAttributes) {
						topAttribute = attributeId2attributeObject.get(attribute);
						topAttribute.nextValue();
						
						if (topAttribute.isPruneable()) {
							activeAttributes.rem(topAttribute.getAttributeId());
							continue;
						}
						
						if (!topAttribute.hasFinished())
							attributeObjectQueue.add(topAttribute);
					}
					
					topAttributes.clear();
				}
			}
		}
		
		// Format the results
		this.dep2ref = new Int2ObjectOpenHashMap<IntSingleLinkedList>(this.numColumns);
		for (Attribute attribute : attributeId2attributeObject.values())
			if (!attribute.getReferenced().isEmpty())
				this.dep2ref.put(attribute.getAttributeId(), new IntSingleLinkedList(attribute.getReferenced()));
	}
	
	private void checkViaTwoStageIndexAndBitSets() throws IOException {
		/////////////////////////////////////////////////////////
		// Phase 2.1: Pruning (Dismiss first candidates early) //
		/////////////////////////////////////////////////////////
		
		// Setup the initial INDs
		BitSet allAttributes = new BitSet(this.numColumns);
		allAttributes.set(0, this.numColumns);
		
		Int2ObjectOpenHashMap<BitSet> attribute2Refs = new Int2ObjectOpenHashMap<BitSet>(this.numColumns);
		for (int column = 0; column < this.numColumns; column++) {
			BitSet refs = (BitSet)allAttributes.clone();
			refs.clear(column);
			attribute2Refs.put(column, refs);
		}
		
		// Apply data type pruning
		BitSet strings = new BitSet(this.numColumns);
		BitSet numerics = new BitSet(this.numColumns);
		BitSet temporals = new BitSet(this.numColumns);
		BitSet unknown = new BitSet(this.numColumns);
		for (int column = 0; column < this.numColumns; column++) {
			if (DatabaseUtils.isString(this.columnTypes.get(column)))
				strings.set(column);
			else if (DatabaseUtils.isNumeric(this.columnTypes.get(column)))
				numerics.set(column);
			else if (DatabaseUtils.isTemporal(this.columnTypes.get(column)))
				temporals.set(column);
			else
				unknown.set(column);
		}
		this.prune(attribute2Refs, strings);
		this.prune(attribute2Refs, numerics);
		this.prune(attribute2Refs, temporals);
		this.prune(attribute2Refs, unknown);
		
		// Apply statistical pruning
		// TODO ...
		
		///////////////////////////////////////////////////////////////
		// Phase 2.2: Validation (Successively check all candidates) //
		///////////////////////////////////////////////////////////////
		
		// Iterate the buckets for all remaining INDs until the end is reached or no more INDs exist
		BitSet activeAttributes = (BitSet)allAttributes.clone();
		levelloop : for (int bucketNumber : this.bucketComparisonOrder) { // TODO: Externalize this code into a method and use return instead of break
			// Refine the current bucket level if it does not fit into memory at once
			int[] subBucketNumbers = this.refineBucketLevel(activeAttributes, 0, bucketNumber);
			for (int subBucketNumber : subBucketNumbers) {
				// Identify all currently active attributes
				activeAttributes = this.getActiveAttributesFromBitSets(activeAttributes, attribute2Refs);
				this.activeAttributesPerBucketLevel.add(activeAttributes.cardinality());
				if (activeAttributes.isEmpty())
					break levelloop;
				
				// Load next bucket level as two stage index
				Int2ObjectOpenHashMap<List<String>> attribute2Bucket = new Int2ObjectOpenHashMap<List<String>>(this.numColumns);
				Map<String, BitSet> invertedIndex = new HashMap<String, BitSet>();
				for (int attribute = activeAttributes.nextSetBit(0); attribute >= 0; attribute = activeAttributes.nextSetBit(attribute + 1)) {
					// Build the index
					List<String> bucket = this.readBucketAsList(attribute, bucketNumber, subBucketNumber);
					attribute2Bucket.put(attribute, bucket);
					// Build the inverted index
					for (String value : bucket) {
						if (!invertedIndex.containsKey(value))
							invertedIndex.put(value, new BitSet(this.numColumns));
						invertedIndex.get(value).set(attribute);
					}
				}
				
				// Check INDs
				for (int attribute = activeAttributes.nextSetBit(0); attribute >= 0; attribute = activeAttributes.nextSetBit(attribute + 1)) {
					for (String value : attribute2Bucket.get(attribute)) {
						// Break if the attribute does not reference any other attribute
						if (attribute2Refs.get(attribute).isEmpty())
							break;
						
						// Continue if the current value has already been handled
						if (!invertedIndex.containsKey(value))
							continue;
						
						// Prune using the group of attributes containing the current value
						BitSet sameValueGroup = invertedIndex.get(value);
						this.prune(attribute2Refs, sameValueGroup);
						
						// Remove the current value from the index as it has now been handled
						invertedIndex.remove(value);
					}
				}
			}
		}
		
		// Format the results
		this.dep2ref = new Int2ObjectOpenHashMap<IntSingleLinkedList>(this.numColumns);
		for (int dep = 0; dep < this.numColumns; dep++) {
			if (attribute2Refs.get(dep).isEmpty())
				continue;
			
			IntSingleLinkedList refs = new IntSingleLinkedList();
			for (int ref = attribute2Refs.get(dep).nextSetBit(0); ref >= 0; ref = attribute2Refs.get(dep).nextSetBit(ref + 1))
				refs.add(ref);
			
			this.dep2ref.put(dep, refs);
		}
	}
	
	private void checkViaTwoStageIndexAndLists() throws IOException {
		System.out.println("Checking ...");
		
		/////////////////////////////////////////////////////////
		// Phase 2.1: Pruning (Dismiss first candidates early) //
		/////////////////////////////////////////////////////////
		
		// Setup the initial INDs using type information
		IntArrayList strings = new IntArrayList(this.numColumns / 2);
		IntArrayList numerics = new IntArrayList(this.numColumns / 2);
		IntArrayList temporals = new IntArrayList();
		IntArrayList unknown = new IntArrayList();
		for (int column = 0; column < this.numColumns; column++) {
			if (DatabaseUtils.isString(this.columnTypes.get(column)))
				strings.add(column);
			else if (DatabaseUtils.isNumeric(this.columnTypes.get(column)))
				numerics.add(column);
			else if (DatabaseUtils.isTemporal(this.columnTypes.get(column)))
				temporals.add(column);
			else
				unknown.add(column);
		}
		
		// Empty attributes can directly be placed in the output as they are contained in everything else; no empty attribute needs to be checked
		Int2ObjectOpenHashMap<IntSingleLinkedList> dep2refFinal = new Int2ObjectOpenHashMap<IntSingleLinkedList>(this.numColumns);
		Int2ObjectOpenHashMap<IntSingleLinkedList> attribute2Refs = new Int2ObjectOpenHashMap<IntSingleLinkedList>(this.numColumns);
		this.fetchCandidates(strings, attribute2Refs, dep2refFinal);
		this.fetchCandidates(numerics, attribute2Refs, dep2refFinal);
		this.fetchCandidates(temporals, attribute2Refs, dep2refFinal);
		this.fetchCandidates(unknown, attribute2Refs, dep2refFinal);
		
		// Apply statistical pruning
		// TODO ...
		
		///////////////////////////////////////////////////////////////
		// Phase 2.2: Validation (Successively check all candidates) //
		///////////////////////////////////////////////////////////////
		
		// The initially active attributes are all non-empty attributes
		BitSet activeAttributes = new BitSet(this.numColumns);
		for (int column = 0; column < this.numColumns; column++)
			if (this.columnSizes.getLong(column) > 0)
				activeAttributes.set(column);
		
		// Iterate the buckets for all remaining INDs until the end is reached or no more INDs exist
		levelloop : for (int bucketNumber : this.bucketComparisonOrder) { // TODO: Externalize this code into a method and use return instead of break
			// Refine the current bucket level if it does not fit into memory at once
			int[] subBucketNumbers = this.refineBucketLevel(activeAttributes, 0, bucketNumber);
			for (int subBucketNumber : subBucketNumbers) {
				// Identify all currently active attributes
				activeAttributes = this.getActiveAttributesFromLists(activeAttributes, attribute2Refs);
				this.activeAttributesPerBucketLevel.add(activeAttributes.cardinality());
				if (activeAttributes.isEmpty())
					break levelloop;
				
				// Load next bucket level as two stage index
				Int2ObjectOpenHashMap<List<String>> attribute2Bucket = new Int2ObjectOpenHashMap<List<String>>(this.numColumns);
				Map<String, IntArrayList> invertedIndex = new HashMap<String, IntArrayList>();
				for (int attribute = activeAttributes.nextSetBit(0); attribute >= 0; attribute = activeAttributes.nextSetBit(attribute + 1)) {
					// Build the index
					List<String> bucket = this.readBucketAsList(attribute, bucketNumber, subBucketNumber);
					attribute2Bucket.put(attribute, bucket);
					// Build the inverted index
					for (String value : bucket) {
						if (!invertedIndex.containsKey(value))
							invertedIndex.put(value, new IntArrayList(2));
						invertedIndex.get(value).add(attribute);
					}
				}
				
				// Check INDs
				for (int attribute = activeAttributes.nextSetBit(0); attribute >= 0; attribute = activeAttributes.nextSetBit(attribute + 1)) {
					for (String value : attribute2Bucket.get(attribute)) {
						// Break if the attribute does not reference any other attribute
						if (attribute2Refs.get(attribute).isEmpty())
							break;
						
						// Continue if the current value has already been handled
						if (!invertedIndex.containsKey(value))
							continue;
						
						// Prune using the group of attributes containing the current value
						IntArrayList sameValueGroup = invertedIndex.get(value);
						this.prune(attribute2Refs, sameValueGroup);
						
						// Remove the current value from the index as it has now been handled
						invertedIndex.remove(value);
					}
				}
			}
		}
		
		// Remove deps that have no refs
		IntIterator depIterator = attribute2Refs.keySet().iterator();
		while (depIterator.hasNext()) {
			if (attribute2Refs.get(depIterator.nextInt()).isEmpty())
				depIterator.remove();
		}
		this.dep2ref = attribute2Refs;
		this.dep2ref.putAll(dep2refFinal);
	}
	
	private void fetchCandidates(IntArrayList columns, Int2ObjectOpenHashMap<IntSingleLinkedList> dep2refToCheck, Int2ObjectOpenHashMap<IntSingleLinkedList> dep2refFinal) {
		IntArrayList nonEmptyColumns = new IntArrayList(columns.size());
		for (int column : columns)
			if (this.columnSizes.getLong(column) > 0)
				nonEmptyColumns.add(column);
		
		if (this.filterKeyForeignkeys) {
			for (int dep : columns) {
				// Empty columns are no foreign keys
				if (this.columnSizes.getLong(dep) == 0)
					continue;
				
				// Referenced columns must not have null values and must come from different tables
				IntArrayList seed = nonEmptyColumns.clone();
				IntListIterator iterator = seed.iterator();
				while (iterator.hasNext()) {
					int ref = iterator.nextInt();
					if ((this.column2table[dep] == this.column2table[ref]) || this.nullValueColumns.get(ref))
						iterator.remove();
				}
				
				dep2refToCheck.put(dep, new IntSingleLinkedList(seed, dep));
			}
		}
		else {
			for (int dep : columns) {
				if (this.columnSizes.getLong(dep) == 0)
					dep2refFinal.put(dep, new IntSingleLinkedList(columns, dep));
				else
					dep2refToCheck.put(dep, new IntSingleLinkedList(nonEmptyColumns, dep));
			}
		}
	}
	
	private void prune(Int2ObjectOpenHashMap<BitSet> attribute2Refs, BitSet attributeGroup) {
		for (int attribute = attributeGroup.nextSetBit(0); attribute >= 0; attribute = attributeGroup.nextSetBit(attribute + 1))
			attribute2Refs.get(attribute).and(attributeGroup);
	}

	private void prune(Int2ObjectOpenHashMap<IntSingleLinkedList> attribute2Refs, IntArrayList attributeGroup) {
		for (int attribute : attributeGroup)
			attribute2Refs.get(attribute).retainAll(attributeGroup);
	}
	
	private BitSet getActiveAttributesFromBitSets(BitSet previouslyActiveAttributes, Int2ObjectOpenHashMap<BitSet> attribute2Refs) {
		BitSet activeAttributes = new BitSet(this.numColumns);
		for (int attribute = previouslyActiveAttributes.nextSetBit(0); attribute >= 0; attribute = previouslyActiveAttributes.nextSetBit(attribute + 1)) {
			// All attributes referenced by this attribute are active
			activeAttributes.or(attribute2Refs.get(attribute));
			// This attribute is active if it references any other attribute
			if (!attribute2Refs.get(attribute).isEmpty())
				activeAttributes.set(attribute);
		}
		return activeAttributes;
	}
	
	private BitSet getActiveAttributesFromLists(BitSet previouslyActiveAttributes, Int2ObjectOpenHashMap<IntSingleLinkedList> attribute2Refs) {
		BitSet activeAttributes = new BitSet(this.numColumns);
		for (int attribute = previouslyActiveAttributes.nextSetBit(0); attribute >= 0; attribute = previouslyActiveAttributes.nextSetBit(attribute + 1)) {
			// All attributes referenced by this attribute are active
			attribute2Refs.get(attribute).setOwnValuesIn(activeAttributes);
			// This attribute is active if it references any other attribute
			if (!attribute2Refs.get(attribute).isEmpty())
				activeAttributes.set(attribute);
		}
		return activeAttributes;
	}
	
	private int calculateBucketFor(String value) {
		return Math.abs(value.hashCode() % this.numBucketsPerColumn); // range partitioning
	}

	private int calculateBucketFor(String value, int bucketNumber, int numSubBuckets) {
		return ((Math.abs(value.hashCode() % (this.numBucketsPerColumn * numSubBuckets)) - bucketNumber) / this.numBucketsPerColumn); // range partitioning
	}
	
	private void calculateBucketComparisonOrder(int[] emptyBuckets) {
		List<Level> levels = new ArrayList<Level>(this.numColumns);
		for (int level = 0; level < this.numBucketsPerColumn; level++)
			levels.add(new Level(level, emptyBuckets[level]));
		Collections.sort(levels);
		
		this.bucketComparisonOrder = new int[this.numBucketsPerColumn];
		for (int rank = 0; rank < this.numBucketsPerColumn; rank++)
			this.bucketComparisonOrder[rank] = levels.get(rank).getNumber();
	}
	
	private void writeBucket(int attributeNumber, int bucketNumber, int subBucketNumber, Collection<String> values) throws IOException {
		// Write the values
		String bucketFilePath = this.getBucketFilePath(attributeNumber, bucketNumber, subBucketNumber);
		this.writeToDisk(bucketFilePath, values);
		
		// Add the size of the written written values to the size of the current attribute
		long size = this.columnSizes.getLong(attributeNumber);
		for (String value : values)
			size = size + MeasurementUtils.sizeOf64(value) + this.overheadPerValueForIndexes;
		this.columnSizes.set(attributeNumber, size);
	}
	
	private void writeToDisk(String bucketFilePath, Collection<String> values) throws IOException {
		if ((values == null) || (values.isEmpty()))
			return;
		
		File file = new File(bucketFilePath);
		boolean append = file.exists();
		
		BufferedWriter writer = null;
		try {
			writer = FileUtils.buildFileWriter(bucketFilePath, true);
			if (append)
				writer.newLine();
			
			Iterator<String> valueIterator = values.iterator();
			while (valueIterator.hasNext()) {
				writer.write(valueIterator.next());
				if (valueIterator.hasNext())
					writer.newLine();
			}
			writer.flush();
		}
		finally {
			FileUtils.close(writer);
		}
	}
	
	private Set<String> readBucketAsSet(int attributeNumber, int bucketNumber, int subBucketNumber) throws IOException {
		if ((this.attribute2subBucketsCache != null) && (this.attribute2subBucketsCache.containsKey(attributeNumber)))
			return new HashSet<String>(this.attribute2subBucketsCache.get(attributeNumber).get(subBucketNumber));
		
		Set<String> bucket = new HashSet<String>(); // Reading buckets into Sets eliminates all duplicates within these buckets
		String bucketFilePath = this.getBucketFilePath(attributeNumber, bucketNumber, subBucketNumber);
		this.readFromDisk(bucketFilePath, bucket);
		return bucket;
	}

	private List<String> readBucketAsList(int attributeNumber, int bucketNumber, int subBucketNumber) throws IOException {
		if ((this.attribute2subBucketsCache != null) && (this.attribute2subBucketsCache.containsKey(attributeNumber)))
			return this.attribute2subBucketsCache.get(attributeNumber).get(subBucketNumber);
		
		List<String> bucket = new ArrayList<String>(); // Reading buckets into Lists keeps duplicates within these buckets
		String bucketFilePath = this.getBucketFilePath(attributeNumber, bucketNumber, subBucketNumber);
		this.readFromDisk(bucketFilePath, bucket);
		return bucket;
	}

	private void readFromDisk(String bucketFilePath, Collection<String> values) throws IOException {
		File file = new File(bucketFilePath);
		if (!file.exists())
			return;
		
		BufferedReader reader = null;
		String value = null;
		try {
			reader = FileUtils.buildFileReader(bucketFilePath);
			while ((value = reader.readLine()) != null)
				values.add(value);
		}
		finally {
			FileUtils.close(reader);
		}
	}

	private BufferedReader getBucketReader(int attributeNumber, int bucketNumber, int subBucketNumber) throws IOException {
		String bucketFilePath = this.getBucketFilePath(attributeNumber, bucketNumber, subBucketNumber);
		
		File file = new File(bucketFilePath);
		if (!file.exists())
			return null;
		
		return FileUtils.buildFileReader(bucketFilePath);
	}
	
	private String getBucketFilePath(int attributeNumber, int bucketNumber, int subBucketNumber) {
		if (subBucketNumber >= 0)
			return this.tempFolder.getPath() + File.separator + attributeNumber + File.separator + bucketNumber + "_" + subBucketNumber;
		return this.tempFolder.getPath() + File.separator + attributeNumber + File.separator + bucketNumber;
	}
	
	private int[] refineBucketLevel(IntArrayList activeAttributes, int level) throws IOException {
		BitSet activeAttributesBits = new BitSet(this.numColumns);
		for (int attribute : activeAttributes)
			activeAttributesBits.set(attribute);
		return this.refineBucketLevel(activeAttributesBits, 0, level);
	}
	
	private int[] refineBucketLevel(BitSet activeAttributes, int attributeOffset, int level) throws IOException { // The offset is used for n-ary INDs, because their buckets are placed behind the unary buckets on disk, which is important if the unary buckets have not been deleted before
		// Empty sub bucket cache, because it will be refilled in the following
		this.attribute2subBucketsCache = null;
		
		// Give a hint to the gc
		System.gc();
		
		// Measure the size of the level and find the attribute with the largest bucket
		int numAttributes = 0;
		long levelSize = 0;
		for (int attribute = activeAttributes.nextSetBit(0); attribute >= 0; attribute = activeAttributes.nextSetBit(attribute + 1)) {
			numAttributes++;
			int attributeIndex = attribute + attributeOffset;
			long bucketSize = this.columnSizes.getLong(attributeIndex) / this.numBucketsPerColumn;
			levelSize = levelSize + bucketSize;
		}
		
		// If there are no active attributes, no refinement is needed
		if (numAttributes == 0) {
			int[] subBucketNumbers = new int[1];
			subBucketNumbers[0] = -1;
			return subBucketNumbers;
		}
		
		// Define the number of sub buckets
		long maxBucketSize = this.maxMemoryUsage / numAttributes;
		int numSubBuckets = (int)(levelSize / this.maxMemoryUsage) + 1;

		int[] subBucketNumbers = new int[numSubBuckets];
		
		// If the current level fits into memory, no refinement is needed
		if (numSubBuckets == 1) {
			subBucketNumbers[0] = -1;
			return subBucketNumbers;
		}
		
		for (int subBucketNumber = 0; subBucketNumber < numSubBuckets; subBucketNumber++)
			subBucketNumbers[subBucketNumber] = subBucketNumber;
		
		if (attributeOffset == 0)
			this.refinements[level] = numSubBuckets;
		else
			this.naryRefinements.get(this.naryRefinements.size() - 1)[level] = numSubBuckets;
		
		this.attribute2subBucketsCache = new Int2ObjectOpenHashMap<List<List<String>>>(numSubBuckets);
		
		// Refine
		for (int attribute = activeAttributes.nextSetBit(0); attribute >= 0; attribute = activeAttributes.nextSetBit(attribute + 1)) {
			int attributeIndex = attribute + attributeOffset;
			
			List<List<String>> subBuckets = new ArrayList<List<String>>(numSubBuckets);
			//int expectedNewBucketSize = (int)(bucket.size() * (1.2f / numSubBuckets)); // The expected size is bucket.size()/subBuckets and we add 20% to it to avoid resizing
			//int expectedNewBucketSize = (int)(this.columnSizes.getLong(attributeIndex) / this.numBucketsPerColumn / 80); // We estimate an average String size of 8 chars, hence 64+2*8=80 byte
			for (int subBucket = 0; subBucket < numSubBuckets; subBucket++)
				subBuckets.add(new ArrayList<String>());
			
			BufferedReader reader = null;
			String value = null;
			boolean spilled = false;
			try {
				reader = this.getBucketReader(attributeIndex, level, -1);
				
				if (reader != null) {
					int numValuesSinceLastMemoryCheck = 0;
					
					while ((value = reader.readLine()) != null) {
						int bucketNumber = this.calculateBucketFor(value, level, numSubBuckets);
						subBuckets.get(bucketNumber).add(value);
						numValuesSinceLastMemoryCheck++;
						
						// Occasionally check the memory consumption
						if (numValuesSinceLastMemoryCheck >= this.memoryCheckFrequency) {
							numValuesSinceLastMemoryCheck = 0;
							
							// Spill to disk if necessary
							if (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() > this.maxMemoryUsage) {								
								for (int subBucket = 0; subBucket < numSubBuckets; subBucket++) {
									this.writeBucket(attributeIndex, level, subBucket, subBuckets.get(subBucket));
									subBuckets.set(subBucket, new ArrayList<String>());
								}
								
								spilled = true;
								System.gc();
							}
						}
					}
				}
			}
			finally {
				FileUtils.close(reader);
			}
			
			// Large sub bucketings need to be written to disk; small sub bucketings can stay in memory
			if ((this.columnSizes.getLong(attributeIndex) / this.numBucketsPerColumn > maxBucketSize) || spilled)
				for (int subBucket = 0; subBucket < numSubBuckets; subBucket++)
					this.writeBucket(attributeIndex, level, subBucket, subBuckets.get(subBucket));
			else
				this.attribute2subBucketsCache.put(attributeIndex, subBuckets);
		}
		
		return subBucketNumbers;
	}
	
	private void detectNaryViaBucketing() throws InputGenerationException, InputIterationException, IOException, AlgorithmConfigurationException {
		System.out.print("N-ary IND detection ...");
		
		// Clean temp
		if (this.cleanTemp)
			FileUtils.cleanDirectory(this.tempFolder);
		
		// N-ary column combinations are enumerated following the enumeration of the attributes
		int naryOffset = this.numColumns;

		// Initialize counters
		this.naryActiveAttributesPerBucketLevel = new IntArrayList();
		this.narySpillCounts = new ArrayList<int[]>();
		this.naryRefinements = new ArrayList<int[]>();
		
		// Initialize nPlusOneAryDep2ref with unary dep2ref
		Map<AttributeCombination, List<AttributeCombination>> nPlusOneAryDep2ref = new HashMap<AttributeCombination, List<AttributeCombination>>();
		for (int dep : this.dep2ref.keySet()) {
			AttributeCombination depAttributeCombination = new AttributeCombination(this.column2table[dep], dep);
			List<AttributeCombination> refAttributeCombinations = new LinkedList<AttributeCombination>();
			
			ElementIterator refIterator = this.dep2ref.get(dep).elementIterator();
			while (refIterator.hasNext()) {
				int ref = refIterator.next();
				refAttributeCombinations.add(new AttributeCombination(this.column2table[ref], ref));
			}
			nPlusOneAryDep2ref.put(depAttributeCombination, refAttributeCombinations);
		}

		int naryLevel = 1;
		
		// Generate, bucketize and test the n-ary INDs level-wise
		this.naryDep2ref = new HashMap<AttributeCombination, List<AttributeCombination>>();
		this.naryGenerationTime = new LongArrayList();
		this.naryLoadTime = new LongArrayList();
		this.naryCompareTime = new LongArrayList();
		while (++naryLevel <= this.maxNaryLevel || this.maxNaryLevel <= 0) {
			System.out.print(" L" + naryLevel);
			
			// Generate (n+1)-ary IND candidates from the already identified unary and n-ary IND candidates
			final long naryGenerationTimeCurrent = System.currentTimeMillis();
			
			nPlusOneAryDep2ref = this.generateNPlusOneAryCandidates(nPlusOneAryDep2ref);
			if (nPlusOneAryDep2ref.isEmpty())
				break;
			
			// Collect all attribute combinations of the current level that are possible refs or deps and enumerate them
			Set<AttributeCombination> attributeCombinationSet = new HashSet<AttributeCombination>();
			attributeCombinationSet.addAll(nPlusOneAryDep2ref.keySet());
			for (List<AttributeCombination> columnCombination : nPlusOneAryDep2ref.values())
				attributeCombinationSet.addAll(columnCombination);
			List<AttributeCombination> attributeCombinations = new ArrayList<AttributeCombination>(attributeCombinationSet);
			
			// Extend the columnSize array
			for (int i = 0; i < attributeCombinations.size(); i++)
				this.columnSizes.add(0);
			
			int[] currentNarySpillCounts = new int[attributeCombinations.size()];
			for (int attributeCombinationNumber = 0; attributeCombinationNumber < attributeCombinations.size(); attributeCombinationNumber++)
				currentNarySpillCounts[attributeCombinationNumber] = 0;
			this.narySpillCounts.add(currentNarySpillCounts);
			
			int[] currentNaryRefinements = new int[this.numBucketsPerColumn];
			for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++)
				currentNaryRefinements[bucketNumber] = 0;
			this.naryRefinements.add(currentNaryRefinements);
			
			this.naryGenerationTime.add(System.currentTimeMillis() - naryGenerationTimeCurrent);
			
			// Read the input dataset again and bucketize all attribute combinations that are refs or deps
			long naryLoadTimeCurrent = System.currentTimeMillis();
			this.naryBucketize(attributeCombinations, naryOffset, currentNarySpillCounts);
			this.naryLoadTime.add(System.currentTimeMillis() - naryLoadTimeCurrent);
			
			// Check the n-ary IND candidates
			long naryCompareTimeCurrent = System.currentTimeMillis();
			this.naryCheckViaTwoStageIndexAndLists(nPlusOneAryDep2ref, attributeCombinations, naryOffset);
			
			this.naryDep2ref.putAll(nPlusOneAryDep2ref);
			
			// Add the number of created buckets for n-ary INDs of this level to the naryOffset
			naryOffset = naryOffset + attributeCombinations.size();

			this.naryCompareTime.add(System.currentTimeMillis() - naryCompareTimeCurrent);
			System.out.print("(" + (System.currentTimeMillis() - naryGenerationTimeCurrent) + " ms)");
		}
		System.out.println();
	}
	
	private void detectNaryViaSingleChecks() throws InputGenerationException, AlgorithmConfigurationException {
		if (this.databaseConnectionGenerator == null)
			throw new InputGenerationException("n-ary IND detection using De Marchi's MIND algorithm only possible on databases");
		
		// Clean temp
		if (this.cleanTemp)
			FileUtils.cleanDirectory(this.tempFolder);
		
		// Initialize nPlusOneAryDep2ref with unary dep2ref
		Map<AttributeCombination, List<AttributeCombination>> nPlusOneAryDep2ref = new HashMap<AttributeCombination, List<AttributeCombination>>();
		for (int dep : this.dep2ref.keySet()) {
			AttributeCombination depAttributeCombination = new AttributeCombination(this.column2table[dep], dep);
			List<AttributeCombination> refAttributeCombinations = new LinkedList<AttributeCombination>();
			
			ElementIterator refIterator = this.dep2ref.get(dep).elementIterator();
			while (refIterator.hasNext()) {
				int ref = refIterator.next();
				refAttributeCombinations.add(new AttributeCombination(this.column2table[ref], ref));
			}
			nPlusOneAryDep2ref.put(depAttributeCombination, refAttributeCombinations);
		}
		
		// Generate, bucketize and test the n-ary INDs level-wise
		this.naryDep2ref = new HashMap<AttributeCombination, List<AttributeCombination>>();
		this.naryGenerationTime = new LongArrayList();
		this.naryCompareTime = new LongArrayList();
		while (true) {
			// Generate (n+1)-ary IND candidates from the already identified unary and n-ary IND candidates
			long naryGenerationTimeCurrent = System.currentTimeMillis();
			nPlusOneAryDep2ref = this.generateNPlusOneAryCandidates(nPlusOneAryDep2ref);
			if (nPlusOneAryDep2ref.isEmpty())
				break;
			this.naryGenerationTime.add(System.currentTimeMillis() - naryGenerationTimeCurrent);
			
			// Check the n-ary IND candidates
			long naryCompareTimeCurrent = System.currentTimeMillis();
			
			Iterator<AttributeCombination> depIterator = nPlusOneAryDep2ref.keySet().iterator();
			while (depIterator.hasNext()) {
				AttributeCombination dep = depIterator.next();
				
				List<AttributeCombination> refs = nPlusOneAryDep2ref.get(dep);
				
				Iterator<AttributeCombination> refIterator = refs.iterator();
				while (refIterator.hasNext()) {
					AttributeCombination ref = refIterator.next();
					
					String depTableName = this.tableNames[dep.getTable()];
					String[] depAttributeNames = dep.getAttributes(this.columnNames);
					String refTableName = this.tableNames[ref.getTable()];
					String[] refAttributeNames = ref.getAttributes(this.columnNames);
					
					String query = this.dao.buildSelectColumnCombinationNotInColumnCombinationQuery(depTableName, depAttributeNames, refTableName, refAttributeNames, 2);
					
					ResultSet resultSet = null;
					try {
						resultSet = this.databaseConnectionGenerator.generateResultSetFromSql(query);
						
						// Check if there is a non-NULL value in the dep attribute combination
						if (resultSet.next())
							if ((resultSet.getString(1) != null) || resultSet.next())
								refIterator.remove();
					}
					catch (InputGenerationException e) {
						e.getCause().printStackTrace();
						throw new InputGenerationException(e.getMessage() + "\nThe failed query was:\n" + query, e);
					}
					catch (SQLException e) {
						e.printStackTrace();
						throw new InputGenerationException(e.getMessage() + "\nThe failed query was:\n" + query, e);
					}
					finally {
						try {
							if (resultSet != null) {
								Statement statement = resultSet.getStatement();
								DatabaseUtils.close(resultSet);
								DatabaseUtils.close(statement);
							}
						}
						catch (SQLException e) {
						}
					}
				}
				
				if (nPlusOneAryDep2ref.get(dep).isEmpty())
					depIterator.remove();
			}
			
			this.naryDep2ref.putAll(nPlusOneAryDep2ref);
			
			this.naryCompareTime.add(System.currentTimeMillis() - naryCompareTimeCurrent);
		}
	}
/**/
	private Map<AttributeCombination, List<AttributeCombination>> generateNPlusOneAryCandidates(Map<AttributeCombination, List<AttributeCombination>> naryDep2ref) {
		Map<AttributeCombination, List<AttributeCombination>> nPlusOneAryDep2ref = new HashMap<AttributeCombination, List<AttributeCombination>>();
		
		if ((naryDep2ref == null) || (naryDep2ref.isEmpty()))
			return nPlusOneAryDep2ref;
		
		int previousSize = naryDep2ref.keySet().iterator().next().size();
		
//if (previousSize >= 3)
//	return nPlusOneAryDep2ref;
//System.out.println("apriori-gen level: " + (previousSize + 1));
		
		List<AttributeCombination> deps = new ArrayList<>(naryDep2ref.keySet());
		for (int i = 0; i < deps.size() - 1; i++) {
			AttributeCombination depPivot = deps.get(i);
			for (int j = i + 1; j < deps.size(); j++) { // if INDs of the form AA<CD should be discovered as well, remove + 1
				AttributeCombination depExtension = deps.get(j);
				
				// Ensure same tables
				if (depPivot.getTable() != depExtension.getTable())
					continue;
				
				// Ensure same prefix
				if (!this.samePrefix(depPivot, depExtension))
					continue;
				
				int depPivotAttr = depPivot.getAttributes()[previousSize - 1];
				int depExtensionAttr = depExtension.getAttributes()[previousSize - 1];
				
				// Ensure non-empty attribute extension
				if ((previousSize == 1) && ((this.columnSizes.getLong(depPivotAttr) == 0) || (this.columnSizes.getLong(depExtensionAttr) == 0)))
					continue;
				
				for (AttributeCombination refPivot : naryDep2ref.get(depPivot)) {
					for (AttributeCombination refExtension : naryDep2ref.get(depExtension)) {
						
						// Ensure same tables
						if (refPivot.getTable() != refExtension.getTable())
							continue;
						
						// Ensure same prefix
						if (!this.samePrefix(refPivot, refExtension))
							continue;
						
						int refPivotAttr = refPivot.getAttributes()[previousSize - 1];
						int refExtensionAttr = refExtension.getAttributes()[previousSize - 1];
						
						// Ensure that the extension attribute is different from the pivot attribute; remove check if INDs of the form AB<CC should be discovered as well
						if (refPivotAttr == refExtensionAttr)
							continue;
						
						// We want the lhs and rhs to be disjunct, because INDs with non-disjunct sides usually don't have practical relevance; remove this check if INDs with overlapping sides are of interest
						if ((depPivotAttr == refExtensionAttr) || (depExtensionAttr == refPivotAttr))
							continue;
						//if (nPlusOneDep.contains(nPlusOneRef.getAttributes()[previousSize - 1]) ||
						//	nPlusOneRef.contains(nPlusOneDep.getAttributes()[previousSize - 1]))
						//	continue;
						
						// The new candidate was created with two lhs and their rhs that share the same prefix; but other subsets of the lhs and rhs must also exist if the new candidate is larger than two attributes
						// TODO: Test if the other subsets exist as well (because this test is expensive, same prefix of two INDs might be a strong enough filter for now)

						// Merge the dep attributes and ref attributes, respectively
						AttributeCombination nPlusOneDep = new AttributeCombination(depPivot.getTable(), depPivot.getAttributes(), depExtensionAttr);
						AttributeCombination nPlusOneRef = new AttributeCombination(refPivot.getTable(), refPivot.getAttributes(), refExtensionAttr);
						
						// Store the new candidate
						if (!nPlusOneAryDep2ref.containsKey(nPlusOneDep))
							nPlusOneAryDep2ref.put(nPlusOneDep, new LinkedList<AttributeCombination>());
						nPlusOneAryDep2ref.get(nPlusOneDep).add(nPlusOneRef);
						
//System.out.println(CollectionUtils.concat(nPlusOneDep.getAttributes(), ",") + "c" + CollectionUtils.concat(nPlusOneRef.getAttributes(), ","));
					}
				}
			}
		}
		return nPlusOneAryDep2ref;
	}

	private boolean samePrefix(AttributeCombination combination1, AttributeCombination combination2) {
		for (int i = 0; i < combination1.size() - 1; i++)
			if (combination1.getAttributes()[i] != combination2.getAttributes()[i])
				return false;
		return true;
	}
/**//*
	private Map<AttributeCombination, List<AttributeCombination>> generateNPlusOneAryCandidates(Map<AttributeCombination, List<AttributeCombination>> naryDep2ref) {
		Map<AttributeCombination, List<AttributeCombination>> nPlusOneAryDep2ref = new HashMap<AttributeCombination, List<AttributeCombination>>();
		
		if ((naryDep2ref == null) || (naryDep2ref.isEmpty()))
			return nPlusOneAryDep2ref;
		
//System.out.println("Level: " + (naryDep2ref.keySet().iterator().next().getAttributes().length + 1));
//if (naryDep2ref.keySet().iterator().next().getAttributes().length >= 3)
//	return nPlusOneAryDep2ref;
		
		for (AttributeCombination depAttributeCombination : naryDep2ref.keySet()) {
			if (!this.validAttributeCombinationForNaryCandidates(depAttributeCombination))
				continue;
			
			for (int dep : this.dep2ref.keySet()) {
				if (!this.validAttributeForNaryCandidates(dep))
					continue;
				
				if (!this.isCombineable(depAttributeCombination, dep))
					continue;
				
				AttributeCombination nPlusOneDep = new AttributeCombination(this.column2table[dep], depAttributeCombination.getAttributes(), dep);
				
				for (AttributeCombination refAttributeCombination : naryDep2ref.get(depAttributeCombination)) {
					if (!this.validAttributeCombinationForNaryCandidates(refAttributeCombination))
						continue;
					
					// The chosen extension of the dependent attribute group must not be included in the referenced attribute group
					if (refAttributeCombination.contains(dep))
						continue;
					
					ElementIterator refIterator = this.dep2ref.get(dep).elementIterator();
					while (refIterator.hasNext()) {
						int ref = refIterator.next();
						
						if (!this.validAttributeForNaryCandidates(ref))
							continue;
						
						if (!this.isCombineable(depAttributeCombination, refAttributeCombination, ref))
							continue;
						
						// If we reach here, we found a new n-ary IND candidate!
						AttributeCombination nPlusOneRef = new AttributeCombination(this.column2table[ref], refAttributeCombination.getAttributes(), ref);
						
						if (!nPlusOneAryDep2ref.containsKey(nPlusOneDep))
							nPlusOneAryDep2ref.put(nPlusOneDep, new LinkedList<AttributeCombination>());
						
						nPlusOneAryDep2ref.get(nPlusOneDep).add(nPlusOneRef);
						
//System.out.println(CollectionUtils.concat(nPlusOneDep.getAttributes(), ",") + "c" + CollectionUtils.concat(nPlusOneRef.getAttributes(), ","));
					}
				}
			}
		}
		return nPlusOneAryDep2ref;
	}
/**/
	private boolean validAttributeForNaryCandidates(int attribute) {
		// Do not use empty attributes
		if (this.columnSizes.getLong(attribute) == 0)
			return false;
		
		// Do not use CLOB or BLOB types; BINDER can handle this, but MIND cannot due to the use of SQL-join-checks
		if (DatabaseUtils.isLargeObject(this.columnTypes.get(attribute)))
			return false;
		
		return true;
	}
	
	private boolean validAttributeCombinationForNaryCandidates(AttributeCombination attributeCombination) {
		// Attribute combinations of size > 1 are always valid for further extensions; their attributes have been checked before
		if (attributeCombination.getAttributes().length > 1)
			return true;
		
		int depInCombination = attributeCombination.getAttributes()[0];
		return this.validAttributeForNaryCandidates(depInCombination);
	}
	
	private boolean isCombineable(AttributeCombination depAttributeCombination, int dep) {
		// Do not combine attributes from different tables
		if (depAttributeCombination.getTable() != this.column2table[dep])
			return false;
		
		// Do not use already contained or smaller attributes
		for (int combinationAttribute : depAttributeCombination.getAttributes())
			if (combinationAttribute >= dep)
				return false;
		
		return true;
	}
	
	private boolean isCombineable(AttributeCombination depAttributeCombination, AttributeCombination refAttributeCombination, int ref) {
		// Do not combine attributes from different tables
		if (refAttributeCombination.getTable() != this.column2table[ref])
			return false;
		
		// Do not use already contained attributes
		if (refAttributeCombination.contains(ref) || depAttributeCombination.contains(ref))
			return false;
		
		return true;
	}

	private void naryBucketize(List<AttributeCombination> attributeCombinations, int naryOffset, int[] narySpillCounts) throws InputGenerationException, InputIterationException, IOException, AlgorithmConfigurationException {
		// Identify the relevant attribute combinations for the different tables
		List<IntArrayList> table2attributeCombinationNumbers = new ArrayList<IntArrayList>(this.tableNames.length);
		for (int tableNumber = 0; tableNumber < this.tableNames.length; tableNumber++)
			table2attributeCombinationNumbers.add(new IntArrayList());
		for (int attributeCombinationNumber = 0; attributeCombinationNumber < attributeCombinations.size(); attributeCombinationNumber++)
			table2attributeCombinationNumbers.get(attributeCombinations.get(attributeCombinationNumber).getTable()).add(attributeCombinationNumber);
		
		// Count the empty buckets per attribute to identify sparse buckets and promising bucket levels for comparison
		int[] emptyBuckets = new int[this.numBucketsPerColumn];
		for (int levelNumber = 0; levelNumber < this.numBucketsPerColumn; levelNumber++)
			emptyBuckets[levelNumber] = 0;
		
		for (int tableIndex = 0; tableIndex < this.tableNames.length; tableIndex++) {
			String tableName = this.tableNames[tableIndex];
			int numTableAttributeCombinations = table2attributeCombinationNumbers.get(tableIndex).size();
			int startTableColumnIndex = this.tableColumnStartIndexes[tableIndex];
			
			if (numTableAttributeCombinations == 0)
				continue;
			
			// Initialize buckets
			Int2ObjectOpenHashMap<List<Set<String>>> buckets = new Int2ObjectOpenHashMap<List<Set<String>>>(numTableAttributeCombinations);
			for (int attributeCombinationNumber : table2attributeCombinationNumbers.get(tableIndex)) {
				List<Set<String>> attributeCombinationBuckets = new ArrayList<Set<String>>();
				for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++)
					attributeCombinationBuckets.add(new HashSet<String>());
				buckets.put(attributeCombinationNumber, attributeCombinationBuckets);
			}

			// Initialize value counters
			int numValuesSinceLastMemoryCheck = 0;
			int[] numValuesInAttributeCombination = new int[attributeCombinations.size()];
			for (int attributeCombinationNumber = 0; attributeCombinationNumber < attributeCombinations.size(); attributeCombinationNumber++)
				numValuesInAttributeCombination[attributeCombinationNumber] = 0;

			// Load data
			InputIterator inputIterator = null;
			try {
				if (this.databaseConnectionGenerator != null)
					inputIterator = new SqlInputIterator(this.databaseConnectionGenerator, this.dao, tableName, this.inputRowLimit);
				else
					inputIterator = new FileInputIterator(this.fileInputGenerator[tableIndex], this.inputRowLimit);
				
				while (inputIterator.next()) {
					List<String> values = inputIterator.getValues();
					
					for (int attributeCombinationNumber : table2attributeCombinationNumbers.get(tableIndex)) {
						AttributeCombination attributeCombination = attributeCombinations.get(attributeCombinationNumber);
						
						boolean anyNull = false;
						List<String> attributeCombinationValues = new ArrayList<String>(attributeCombination.getAttributes().length);
						for (int attribute : attributeCombination.getAttributes()) {
							String attributeValue = values.get(attribute - startTableColumnIndex);
							if (anyNull = attributeValue == null) break;
							attributeCombinationValues.add(attributeValue);
						}
						if (anyNull) continue;
						
						String value = CollectionUtils.concat(attributeCombinationValues, this.valueSeparator);
						
						// Bucketize
						int bucketNumber = this.calculateBucketFor(value);
						if (buckets.get(attributeCombinationNumber).get(bucketNumber).add(value)) {
							numValuesSinceLastMemoryCheck++;
							numValuesInAttributeCombination[attributeCombinationNumber] = numValuesInAttributeCombination[attributeCombinationNumber] + 1;
						}
						//this.pruningStatistics.addValue(naryOffset + attributeCombinationNumber, bucketNumber, value); // TODO: Remove?
						
						// Occasionally check the memory consumption
						if (numValuesSinceLastMemoryCheck >= this.memoryCheckFrequency) {
							numValuesSinceLastMemoryCheck = 0;
							
							// Spill to disk if necessary
							while (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() > this.maxMemoryUsage) {								
								// Identify largest buffer
								int largestAttributeCombinationNumber = 0;
								int largestAttributeCombinationSize = numValuesInAttributeCombination[largestAttributeCombinationNumber];
								for (int otherAttributeCombinationNumber = 1; otherAttributeCombinationNumber < numValuesInAttributeCombination.length; otherAttributeCombinationNumber++) {
									if (largestAttributeCombinationSize < numValuesInAttributeCombination[otherAttributeCombinationNumber]) {
										largestAttributeCombinationNumber = otherAttributeCombinationNumber;
										largestAttributeCombinationSize = numValuesInAttributeCombination[otherAttributeCombinationNumber];
									}
								}
								
								// Write buckets from largest column to disk and empty written buckets
								for (int largeBucketNumber = 0; largeBucketNumber < this.numBucketsPerColumn; largeBucketNumber++) {
									this.writeBucket(naryOffset + largestAttributeCombinationNumber, largeBucketNumber, -1, buckets.get(largestAttributeCombinationNumber).get(largeBucketNumber));
									buckets.get(largestAttributeCombinationNumber).set(largeBucketNumber, new HashSet<String>());
								}
								
								numValuesInAttributeCombination[largestAttributeCombinationNumber] = 0;
								
								narySpillCounts[largestAttributeCombinationNumber] = narySpillCounts[largestAttributeCombinationNumber] + 1;
								
								System.gc();
							}
						}
					}
				}
			}
			finally {
				FileUtils.close(inputIterator);
			}
			
			// Write buckets to disk
			for (int attributeCombinationNumber : table2attributeCombinationNumbers.get(tableIndex)) {
				if (narySpillCounts[attributeCombinationNumber] == 0) { // if a attribute combination was spilled to disk, we do not count empty buckets for this attribute combination, because the partitioning distributes the values evenly and hence all buckets should have been populated
					for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++) {
						Set<String> bucket = buckets.get(attributeCombinationNumber).get(bucketNumber);
						if (bucket.size() != 0)
							this.writeBucket(naryOffset + attributeCombinationNumber, bucketNumber, -1, bucket);
						else
							emptyBuckets[bucketNumber] = emptyBuckets[bucketNumber] + 1;
					}
				}
				else {
					for (int bucketNumber = 0; bucketNumber < this.numBucketsPerColumn; bucketNumber++) {
						Set<String> bucket = buckets.get(attributeCombinationNumber).get(bucketNumber);
						if (bucket.size() != 0)
							this.writeBucket(naryOffset + attributeCombinationNumber, bucketNumber, -1, bucket);
					}
				}
			}
		}
		
		// Calculate the bucket comparison order from the emptyBuckets to minimize the influence of sparse-attribute-issue
		this.calculateBucketComparisonOrder(emptyBuckets);
	}

	private void naryCheckViaTwoStageIndexAndLists(Map<AttributeCombination, List<AttributeCombination>> naryDep2ref, List<AttributeCombination> attributeCombinations, int naryOffset) throws IOException {
		////////////////////////////////////////////////////
		// Validation (Successively check all candidates) //
		////////////////////////////////////////////////////

		// Apply statistical pruning
		// TODO ...
		
		// Iterate the buckets for all remaining INDs until the end is reached or no more INDs exist
		BitSet activeAttributeCombinations = new BitSet(attributeCombinations.size());
		activeAttributeCombinations.set(0, attributeCombinations.size());
		levelloop : for (int bucketNumber : this.bucketComparisonOrder) { // TODO: Externalize this code into a method and use return instead of break
			// Refine the current bucket level if it does not fit into memory at once
			int[] subBucketNumbers = this.refineBucketLevel(activeAttributeCombinations, naryOffset, bucketNumber);
			for (int subBucketNumber : subBucketNumbers) {
				// Identify all currently active attributes
				activeAttributeCombinations = this.getActiveAttributeCombinations(activeAttributeCombinations, naryDep2ref, attributeCombinations);
				this.naryActiveAttributesPerBucketLevel.add(activeAttributeCombinations.cardinality());
				if (activeAttributeCombinations.isEmpty())
					break levelloop;
				
				// Load next bucket level as two stage index
				Int2ObjectOpenHashMap<List<String>> attributeCombination2Bucket = new Int2ObjectOpenHashMap<List<String>>();
				Map<String, IntArrayList> invertedIndex = new HashMap<String, IntArrayList>();
				for (int attributeCombination = activeAttributeCombinations.nextSetBit(0); attributeCombination >= 0; attributeCombination = activeAttributeCombinations.nextSetBit(attributeCombination + 1)) {
					// Build the index
					List<String> bucket = this.readBucketAsList(naryOffset + attributeCombination, bucketNumber, subBucketNumber);
					attributeCombination2Bucket.put(attributeCombination, bucket);
					// Build the inverted index
					for (String value : bucket) {
						if (!invertedIndex.containsKey(value))
							invertedIndex.put(value, new IntArrayList(2));
						invertedIndex.get(value).add(attributeCombination);
					}
				}
				
				// Check INDs
				for (int attributeCombination = activeAttributeCombinations.nextSetBit(0); attributeCombination >= 0; attributeCombination = activeAttributeCombinations.nextSetBit(attributeCombination + 1)) {
					for (String value : attributeCombination2Bucket.get(attributeCombination)) {
						// Break if the attribute combination does not reference any other attribute combination
						if (!naryDep2ref.containsKey(attributeCombinations.get(attributeCombination)) || (naryDep2ref.get(attributeCombinations.get(attributeCombination)).isEmpty()))
							break;
						
						// Continue if the current value has already been handled
						if (!invertedIndex.containsKey(value))
							continue;
						
						// Prune using the group of attributes containing the current value
						IntArrayList sameValueGroup = invertedIndex.get(value);
						this.prune(naryDep2ref, sameValueGroup, attributeCombinations);
						
						// Remove the current value from the index as it has now been handled
						invertedIndex.remove(value);
					}
				}
			}
		}
		
		// Format the results
		Iterator<AttributeCombination> depIterator = naryDep2ref.keySet().iterator();
		while (depIterator.hasNext()) {
			if (naryDep2ref.get(depIterator.next()).isEmpty())
				depIterator.remove();
		}
	}
	
	private BitSet getActiveAttributeCombinations(BitSet previouslyActiveAttributeCombinations, Map<AttributeCombination, List<AttributeCombination>> naryDep2ref, List<AttributeCombination> attributeCombinations) {
		BitSet activeAttributeCombinations = new BitSet(attributeCombinations.size());
		for (int attribute = previouslyActiveAttributeCombinations.nextSetBit(0); attribute >= 0; attribute = previouslyActiveAttributeCombinations.nextSetBit(attribute + 1)) {
			AttributeCombination attributeCombination = attributeCombinations.get(attribute);
			if (naryDep2ref.containsKey(attributeCombination)) {
				// All attribute combinations referenced by this attribute are active
				for (AttributeCombination refAttributeCombination : naryDep2ref.get(attributeCombination))
					activeAttributeCombinations.set(attributeCombinations.indexOf(refAttributeCombination));
				// This attribute combination is active if it references any other attribute
				if (!naryDep2ref.get(attributeCombination).isEmpty())
					activeAttributeCombinations.set(attribute);
			}
		}
		return activeAttributeCombinations;
	}
	
	private void prune(Map<AttributeCombination, List<AttributeCombination>> naryDep2ref, IntArrayList attributeCombinationGroupIndexes, List<AttributeCombination> attributeCombinations) {
		List<AttributeCombination> attributeCombinationGroup = new ArrayList<AttributeCombination>(attributeCombinationGroupIndexes.size());
		for (int attributeCombinationIndex : attributeCombinationGroupIndexes)
			attributeCombinationGroup.add(attributeCombinations.get(attributeCombinationIndex));
		
		for (AttributeCombination attributeCombination : attributeCombinationGroup)
			if (naryDep2ref.containsKey(attributeCombination))
				naryDep2ref.get(attributeCombination).retainAll(attributeCombinationGroup);
	}
	
	private void output() throws CouldNotReceiveResultException, ColumnNameMismatchException {
		System.out.println("Generating output ...");
		
		// Output unary INDs
		for (int dep : this.dep2ref.keySet()) {
			String depTableName = this.getTableNameFor(dep, this.tableColumnStartIndexes);
			String depColumnName = this.columnNames.get(dep);
			
			ElementIterator refIterator = this.dep2ref.get(dep).elementIterator();
			while (refIterator.hasNext()) {
				int ref = refIterator.next();
				
				String refTableName = this.getTableNameFor(ref, this.tableColumnStartIndexes);
				String refColumnName = this.columnNames.get(ref);
				
				this.resultReceiver.receiveResult(new InclusionDependency(new ColumnPermutation(new ColumnIdentifier(depTableName, depColumnName)), new ColumnPermutation(new ColumnIdentifier(refTableName, refColumnName))));
				this.numUnaryINDs++;
			}
		}
		
		// Output n-ary INDs
		if (this.naryDep2ref == null)
			return;
		for (AttributeCombination depAttributeCombination : this.naryDep2ref.keySet()) {
			ColumnPermutation dep = this.buildColumnPermutationFor(depAttributeCombination);
			
			for (AttributeCombination refAttributeCombination : this.naryDep2ref.get(depAttributeCombination)) {
				ColumnPermutation ref = this.buildColumnPermutationFor(refAttributeCombination);
				
				this.resultReceiver.receiveResult(new InclusionDependency(dep, ref));
				this.numNaryINDs++;
			}
		}
	}
	
	private String getTableNameFor(int column, int[] tableColumnStartIndexes) {
		for (int i = 1; i < tableColumnStartIndexes.length; i++)
			if (tableColumnStartIndexes[i] > column)
				return this.tableNames[i - 1];
		return this.tableNames[this.tableNames.length - 1];
	}
	
	private ColumnPermutation buildColumnPermutationFor(AttributeCombination attributeCombination) {
		String tableName = this.tableNames[attributeCombination.getTable()];
		
		List<ColumnIdentifier> columnIdentifiers = new ArrayList<ColumnIdentifier>(attributeCombination.getAttributes().length);
		for (int attributeIndex : attributeCombination.getAttributes())
			columnIdentifiers.add(new ColumnIdentifier(tableName, this.columnNames.get(attributeIndex)));
		
		return new ColumnPermutation(columnIdentifiers.toArray(new ColumnIdentifier[0]));
	}

}
