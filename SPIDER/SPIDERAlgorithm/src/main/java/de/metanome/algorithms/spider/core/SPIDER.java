package de.metanome.algorithms.spider.core;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import de.metanome.algorithm_integration.AlgorithmConfigurationException;
import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.metanome.algorithm_integration.ColumnPermutation;
import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.ColumnNameMismatchException;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.InclusionDependencyResultReceiver;
import de.metanome.algorithm_integration.results.InclusionDependency;
import de.metanome.algorithms.spider.structures.Attribute;
import de.uni_potsdam.hpi.dao.DataAccessObject;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.DatabaseUtils;
import de.uni_potsdam.hpi.utils.FileUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

public class SPIDER {

	protected DatabaseConnectionGenerator databaseConnectionGenerator = null;
	protected RelationalInputGenerator[] fileInputGenerator = null; // one for each file specifying a table instance
	protected InclusionDependencyResultReceiver resultReceiver = null;
	protected DataAccessObject dao = null;
	protected String[] tableNames = null;
	protected String databaseName = null;
	protected String tempFolderPath = "SPIDER_temp"; // TODO: Use Metanome temp file functionality here (interface TampFileAlgorithm)
	protected boolean cleanTemp = true;
	protected int inputRowLimit = -1;
	protected int memoryCheckFrequency = 100;
	protected int maxMemoryUsagePercentage = 60;

	protected File tempFolder = null;
	
	protected int numColumns = -1;

	protected long availableMemory;
	protected long maxMemoryUsage;
	
	protected long statisticTime = -1;
	protected long loadTime = -1;
	protected long compareTime = -1;
	protected long outputTime = -1;
	
	protected int[] tableColumnStartIndexes = null;
	protected List<String> columnNames = null;
	protected List<String> columnTypes = null;
	
	protected Int2ObjectOpenHashMap<Attribute> attributeId2attributeObject = null;
	protected PriorityQueue<Attribute> attributeObjectQueue = null;
	protected int numUnaryINDs = 0;
	
	@Override
	public String toString() {
		String input = "-";
		if (this.databaseConnectionGenerator != null) 
			input = this.databaseConnectionGenerator.getClass().getName();
		else if (this.fileInputGenerator != null)
			input = this.fileInputGenerator[0].getClass().getName() + " (" + this.fileInputGenerator.length + ")";
		
		return "SPIDER: \r\n\t" +
				"input: " + input + "\r\n\t" +
				"inputRowLimit: " + this.inputRowLimit + "\r\n\t" +
				"resultReceiver: " + ((this.resultReceiver != null) ? this.resultReceiver.getClass().getName() : "-") + "\r\n\t" +
				"dao: " + ((this.dao != null) ? this.dao.getClass().getName() : "-") + "\r\n\t" +
				"tableNames: " + ((this.tableNames != null) ? CollectionUtils.concat(this.tableNames, ", ") : "-") + "\r\n\t" +
				"numColumns: " + this.numColumns + "\r\n\t" +
				"databaseName: " + this.databaseName + "\r\n\t" +
				"tempFolderPath: " + this.tempFolder.getPath() + "\r\n\t" +
				"memoryCheckFrequency: " + this.memoryCheckFrequency + "\r\n\t" +
				"maxMemoryUsagePercentage: " + this.maxMemoryUsagePercentage + "%\r\n\t" +
				"availableMemory: " + this.availableMemory + " byte (spilled when exeeding " + this.maxMemoryUsage + " byte)\r\n\t" +
				"memoryCheckFrequency: " + this.memoryCheckFrequency + "\r\n\t" +
				"cleanTemp: " + this.cleanTemp + "\r\n\t" +
				"numUnaryINDs: " + this.numUnaryINDs + "\r\n" +
			"statisticTime: " + this.statisticTime + "\r\n" +
			"loadTime: " + this.loadTime + "\r\n" +
			"compareTime: " + this.compareTime + "\r\n" +
			"outputTime: " + this.outputTime;
	}
	
	protected String getAuthorName() {
		return "Thorsten Papenbrock";
	}

	protected String getDescriptionText() {
		return "Sort-Merge-Join-based IND discovery";
	}
	
	public void execute() throws AlgorithmExecutionException {
		// Initialize temp folder
		this.tempFolder = new File(this.tempFolderPath + File.separator + "temp");
		
		// Clean temp if there are files from previous runs that may pollute this run
		FileUtils.cleanDirectory(this.tempFolder);
		
		try {
			//////////////////////////////
			// Collect basic statistics //
			//////////////////////////////
			this.statisticTime = System.currentTimeMillis();
			this.collectStatistics();
			this.statisticTime = System.currentTimeMillis() - this.statisticTime;
			
			///////////////////////////
			// Initialize attributes //
			///////////////////////////
			this.loadTime = System.currentTimeMillis();
			this.initializeAttributes();
			this.loadTime = System.currentTimeMillis() - this.loadTime;
			
			////////////////////////
			// Calculate the INDs //
			////////////////////////
			this.compareTime = System.currentTimeMillis();
			this.calculateINDs();
			this.compareTime = System.currentTimeMillis() - this.compareTime;
			
			////////////////////
			// Output results //
			////////////////////
			this.outputTime = System.currentTimeMillis();
			this.output();
			this.outputTime = System.currentTimeMillis() - this.outputTime;
			
			System.out.println(this.toString());
			System.out.println();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		finally {
			if (this.databaseConnectionGenerator != null)
				FileUtils.close(this.databaseConnectionGenerator);
			
			if (this.attributeId2attributeObject != null)
				for (Attribute spiderAttribute : this.attributeId2attributeObject.values())
					FileUtils.close(spiderAttribute);
			
			// Clean temp
			if (this.cleanTemp)
				FileUtils.cleanDirectory(this.tempFolder);
		}		
	}
	
	private void collectStatistics() throws InputGenerationException, AlgorithmConfigurationException {
		if ((this.databaseConnectionGenerator == null) && (this.fileInputGenerator == null))
			throw new InputGenerationException("No input generator specified!");
		
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
		
		// Initialize memory management
		this.availableMemory = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax();
		this.maxMemoryUsage = (long)(this.availableMemory * (this.maxMemoryUsagePercentage / 100.0f));
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

	private void collectStatisticsFrom(RelationalInputGenerator inputGenerator) throws InputGenerationException, AlgorithmConfigurationException {
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
	
	protected void initializeAttributes() throws AlgorithmExecutionException {
		this.numColumns = this.columnNames.size();
		
		this.attributeId2attributeObject = new Int2ObjectOpenHashMap<Attribute>(this.numColumns);
		this.attributeObjectQueue = new PriorityQueue<Attribute>(this.numColumns);
		
		for (int table = 0; table < this.tableNames.length; table++) {
			int firstAttribute = this.tableColumnStartIndexes[table];
			int lastAttribute = (table == this.tableNames.length - 1) ? this.numColumns : this.tableColumnStartIndexes[table + 1];
			
			for (int attribute = firstAttribute; attribute < lastAttribute; attribute++) {
				Attribute spiderAttribute;
				if (this.databaseConnectionGenerator != null)
					spiderAttribute = new Attribute(attribute, this.columnTypes, this.databaseConnectionGenerator, this.inputRowLimit, this.dao, this.tableNames[table], this.columnNames.get(attribute), this.tempFolder);
				else
					spiderAttribute = new Attribute(attribute, this.columnTypes, this.fileInputGenerator[table], this.inputRowLimit, attribute - firstAttribute, this.tempFolder, this.maxMemoryUsage, this.memoryCheckFrequency);
				this.attributeId2attributeObject.put(attribute, spiderAttribute);
				
				if (!spiderAttribute.hasFinished())
					this.attributeObjectQueue.add(spiderAttribute);
			}
		}
	}
	
	protected void calculateINDs() throws IOException {
		IntArrayList topAttributes = new IntArrayList(this.numColumns);
		while (!this.attributeObjectQueue.isEmpty()) {
			Attribute topAttribute = this.attributeObjectQueue.remove();
			topAttributes.add(topAttribute.getAttributeId());
			while ((!this.attributeObjectQueue.isEmpty()) && topAttribute.getCurrentValue().equals(this.attributeObjectQueue.peek().getCurrentValue()))
				topAttributes.add(this.attributeObjectQueue.remove().getAttributeId());
			
			for (int attribute : topAttributes)
				this.attributeId2attributeObject.get(attribute).intersectReferenced(topAttributes, this.attributeId2attributeObject);
			
			for (int attribute : topAttributes) {
				Attribute spiderAttribute = this.attributeId2attributeObject.get(attribute);
				spiderAttribute.nextValue();
				if (!spiderAttribute.hasFinished())
					this.attributeObjectQueue.add(spiderAttribute);
			}
			
			topAttributes.clear();
		}
	}
	
	private void output() throws CouldNotReceiveResultException, ColumnNameMismatchException {
		// Read the discovered INDs from the attributes
		Int2ObjectOpenHashMap<IntList> dep2ref = new Int2ObjectOpenHashMap<IntList>(this.numColumns);
		for (Attribute spiderAttribute : this.attributeId2attributeObject.values())
			if (!spiderAttribute.getReferenced().isEmpty())
				dep2ref.put(spiderAttribute.getAttributeId(), new IntArrayList(spiderAttribute.getReferenced()));
		
		// Write the result to the resultReceiver
		for (int dep : dep2ref.keySet()) {
			String depTableName = this.getTableNameFor(dep, this.tableColumnStartIndexes);
			String depColumnName = this.columnNames.get(dep);
			
			for (int ref : dep2ref.get(dep)) {
				String refTableName = this.getTableNameFor(ref, this.tableColumnStartIndexes);
				String refColumnName = this.columnNames.get(ref);
				
				this.resultReceiver.receiveResult(new InclusionDependency(new ColumnPermutation(new ColumnIdentifier(depTableName, depColumnName)), new ColumnPermutation(new ColumnIdentifier(refTableName, refColumnName))));
				this.numUnaryINDs++;
			}
		}
	}
	
	private String getTableNameFor(int column, int[] tableColumnStartIndexes) {
		for (int i = 1; i < tableColumnStartIndexes.length; i++)
			if (tableColumnStartIndexes[i] > column)
				return this.tableNames[i - 1];
		return this.tableNames[this.tableNames.length - 1];
	}
}
