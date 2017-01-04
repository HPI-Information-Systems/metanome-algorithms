package de.metanome.algorithms.spider.structures;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.DatabaseConnectionGenerator;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.spider.sorting.TPMMS;
import de.uni_potsdam.hpi.dao.DataAccessObject;
import de.uni_potsdam.hpi.utils.DatabaseUtils;
import de.uni_potsdam.hpi.utils.FileUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntListIterator;

public class Attribute implements Comparable<Attribute>, Closeable {

	protected int attributeId;
	protected String currentValue;
	protected IntLinkedOpenHashSet referenced;
	protected IntLinkedOpenHashSet dependents;

	protected BufferedReader valueReader;
	
	public Attribute(int attributeId, List<String> attributeTypes, DatabaseConnectionGenerator inputGenerator, int inputRowLimit, DataAccessObject dao, String tableName, String attributeName, File tempFolder) throws AlgorithmExecutionException {
		this.attributeId = attributeId;
		
		int numAttributes = attributeTypes.size();
		this.referenced = new IntLinkedOpenHashSet(numAttributes);
		this.dependents = new IntLinkedOpenHashSet(numAttributes);
		for (int i = 0; i < numAttributes; i++) {
			if ((i != this.attributeId) && (DatabaseUtils.matchSameDataTypeClass(attributeTypes.get(i), attributeTypes.get(this.attributeId)))) {
				this.referenced.add(i);
				this.dependents.add(i);
			}
		}
		
		ResultSet resultSet = null;
		BufferedWriter writer = null;
		try {
			// Read values sorted and write them to disk
			resultSet = inputGenerator.generateResultSetFromSql(dao.buildSelectDistinctSortedColumnQuery(tableName, attributeName, attributeTypes.get(this.attributeId), inputRowLimit));			
			writer = FileUtils.buildFileWriter(tempFolder.getPath() + File.separator + attributeId, false); // TODO: Use Metanome temp file functionality here
			boolean isFirstValues = true;
			while (resultSet.next()) {
				String value = resultSet.getString(1);
				if (value == null)
					continue;
				
				// Replace line breaks with the zero-character, because these line breaks would otherwise split values when later written to plane-text files
				value = value.replaceAll("\n", "\0");
				
				if (!isFirstValues)
					writer.newLine();
				writer.write(value);
				isFirstValues = false;
			}
			writer.flush();
			
			// Open reader on written values
			this.valueReader = FileUtils.buildFileReader(tempFolder.getPath() + File.separator + attributeId); // TODO: Use Metanome temp file functionality here
			this.currentValue = ""; // currentValue must not be null, otherwise no nextValue will be read!
			this.nextValue();
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
			FileUtils.close(writer);
			DatabaseUtils.close(resultSet);
			try {
				DatabaseUtils.close(resultSet.getStatement());
			}
			catch (SQLException e) {
			}
		}
	}

	public Attribute(int attributeId, List<String> attributeTypes, RelationalInputGenerator inputGenerator, int inputRowLimit, int relativeAttributeIndex, File tempFolder, long maxMemoryUsage, int memoryCheckFrequency) throws AlgorithmExecutionException {
		this.attributeId = attributeId;
		
		int numAttributes = attributeTypes.size();
		this.referenced = new IntLinkedOpenHashSet(numAttributes);
		this.dependents = new IntLinkedOpenHashSet(numAttributes);
		for (int i = 0; i < numAttributes; i++) {
			if ((i != this.attributeId) && (DatabaseUtils.matchSameDataTypeClass(attributeTypes.get(i), attributeTypes.get(this.attributeId)))) {
				this.referenced.add(i);
				this.dependents.add(i);
			}
		}
		
		try {
			// Read, sort and write attribute
			TPMMS.sortToDisk(inputGenerator, inputRowLimit, tempFolder.getPath() + File.separator + attributeId, relativeAttributeIndex, maxMemoryUsage, memoryCheckFrequency);
			
			// Open reader on written values
			this.valueReader = FileUtils.buildFileReader(tempFolder.getPath() + File.separator + attributeId); // TODO: Use Metanome temp file functionality here
			this.currentValue = ""; // currentValue must not be null, otherwise no nextValue will be read!
			this.nextValue();
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
	}
	
	public int getAttributeId() {
		return this.attributeId;
	}

	public String getCurrentValue() {
		return this.currentValue;
	}

	public IntLinkedOpenHashSet getReferenced() {
		return this.referenced;
	}

	public IntLinkedOpenHashSet getDependents() {
		return this.dependents;
	}
	
	public void nextValue() throws IOException {
		if (this.currentValue == null)
			return;
		
		this.currentValue = this.valueReader.readLine();
		
		if (this.currentValue == null)
			this.close();
	}
	
	public void intersectReferenced(IntArrayList referencedAttributes, Int2ObjectOpenHashMap<Attribute> attributeMap) {
		IntListIterator referencedIterator = this.referenced.iterator();
		while (referencedIterator.hasNext()) {
			int referenced = referencedIterator.nextInt();
			
			if (referencedAttributes.contains(referenced))
				continue;
			
			referencedIterator.remove();
			attributeMap.get(referenced).removeDependent(this.attributeId);
		}
	}
	
	public void removeDependent(int dependent) {
		this.dependents.rem(dependent);
	}
	
	public boolean hasFinished() {
		if (this.currentValue == null)
			return true;
		
		if (this.referenced.isEmpty() && this.dependents.isEmpty())
			return true;
		
		return false;
	}

	@Override
	public int hashCode() {
		return this.attributeId;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Attribute))
			return false;
		Attribute other = (Attribute) obj;
		return this.compareTo(other) == 0;
	}

	@Override
	public String toString() {
		return "(" + this.attributeId + " - \"" + this.currentValue + "\")";
	}

	@Override
	public int compareTo(Attribute other) {
		if ((this.getCurrentValue() == null) && (other.getCurrentValue() == null)) {
			if (this.getAttributeId() > other.getAttributeId())
				return 1;
			if (this.getAttributeId() < other.getAttributeId())
				return -1;
			return 0;
		}
		
		if (this.getCurrentValue() == null)
			return 1;
		if (other.getCurrentValue() == null)
			return -1;
		
		int order = this.getCurrentValue().compareTo(other.getCurrentValue());
		if (order == 0) {
			if (this.getAttributeId() > other.getAttributeId())
				return 1;
			if (this.getAttributeId() < other.getAttributeId())
				return -1;
			return 0;
		}
		return order;
	}

	@Override
	public void close() {
		try {
			this.valueReader.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
