package de.metanome.algorithms.normalize.aspects;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.ColumnIdentifier;
import de.uni_potsdam.hpi.utils.CollectionUtils;
import de.uni_potsdam.hpi.utils.FileUtils;

public class NormiPersistence {

	private List<ColumnIdentifier> columnIdentifiers;
	
	public NormiPersistence(List<ColumnIdentifier> columnIdentifiers) {
		this.columnIdentifiers = columnIdentifiers;
	}

	public Map<BitSet, BitSet> read(String resultsFilePath) {
		File resultsFile =  new File(resultsFilePath);
		if (!resultsFile.exists())
			return null;
		
		System.out.print("Read FDs from result file ... ");
		Map<BitSet, BitSet> results = new HashMap<>();
		
		Map<String, Integer> columnNamesMap = new HashMap<>();
		for (int i = 0; i < this.columnIdentifiers.size(); i++)
			columnNamesMap.put(this.columnIdentifiers.get(i).toString(), new Integer(i));

		String tablePrefix = this.columnIdentifiers.get(0).getTableIdentifier() + ".";
		boolean hasTablePrefix = true;
		BufferedReader reader = null;
		String line = null;
		try {
			reader = FileUtils.buildFileReader(resultsFile.getPath());
			if ((line = reader.readLine()) != null)
				hasTablePrefix = line.split("] --> ")[1].startsWith(tablePrefix);
			FileUtils.close(reader);
			
			int numResultsRead = 0;
			reader = FileUtils.buildFileReader(resultsFile.getPath());
			while ((line = reader.readLine()) != null) {
				String[] split = line.split("] --> ");
				String lhsLabels = split[0];
				String rhsLabels = split[1];
				
				lhsLabels = lhsLabels.replaceAll(Pattern.quote("["), "");
				BitSet lhs = new BitSet(this.columnIdentifiers.size());
				String[] lhsLabelsSplit = lhsLabels.split(", ");
				for (String label : lhsLabelsSplit) {
					if (label.equals(""))
						break;
					
					if (hasTablePrefix)
						lhs.set(columnNamesMap.get(label).intValue());
					else
						lhs.set(columnNamesMap.get(tablePrefix + label).intValue());
				}
				
				BitSet rhs = new BitSet(this.columnIdentifiers.size());
				String[] rhsLabelsSplit = rhsLabels.split(", ");
				for (String label : rhsLabelsSplit) {
					if (hasTablePrefix)
						rhs.set(columnNamesMap.get(label).intValue());
					else
						rhs.set(columnNamesMap.get(tablePrefix + label).intValue());
				}
				
				results.put(lhs, rhs);
				numResultsRead += rhsLabelsSplit.length;
			}
			System.out.println(numResultsRead + " FDs read!");
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		} 
		finally {
			FileUtils.close(reader);
		}
		return results;
	}
	
	public void write(Map<BitSet, BitSet> results, String resultsFilePath, boolean writeTableNamePrefix) throws AlgorithmExecutionException {
		System.out.println("Write FDs to result file ...");
		BufferedWriter writer = null;
		try {
			writer = FileUtils.buildFileWriter(resultsFilePath, false);
			for (Entry<BitSet, BitSet> entry : results.entrySet()) {
				BitSet lhs = entry.getKey();
				BitSet rhs = entry.getValue();
				
				List<String> lhsIdentifier = new ArrayList<>();
				for (int i = lhs.nextSetBit(0); i >= 0; i = lhs.nextSetBit(i + 1)) {
					if (writeTableNamePrefix)
						lhsIdentifier.add(this.columnIdentifiers.get(i).toString());
					else
						lhsIdentifier.add(this.columnIdentifiers.get(i).getColumnIdentifier());
				}
				Collections.sort(lhsIdentifier);
				String lhsString = "[" + CollectionUtils.concat(lhsIdentifier, ", ") + "]";
				
				List<String> rhsIdentifier = new ArrayList<>();
				for (int i = rhs.nextSetBit(0); i >= 0; i = rhs.nextSetBit(i + 1)) {
					if (writeTableNamePrefix)
						rhsIdentifier.add(this.columnIdentifiers.get(i).toString());
					else
						rhsIdentifier.add(this.columnIdentifiers.get(i).getColumnIdentifier());
				}
				Collections.sort(rhsIdentifier);
				String rhsString = CollectionUtils.concat(rhsIdentifier, ", ");
				
				writer.write(lhsString + " --> " + rhsString + "\r\n");
			}
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage(), e);
		} 
		catch (IOException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage(), e);
		} 
		finally {
			FileUtils.close(writer);
		}
	}
}
