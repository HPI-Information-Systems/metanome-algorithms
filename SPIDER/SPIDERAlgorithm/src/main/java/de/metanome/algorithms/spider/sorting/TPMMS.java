package de.metanome.algorithms.spider.sorting;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.uni_potsdam.hpi.utils.FileUtils;

public class TPMMS {
	
	public static void sortToDisk(RelationalInputGenerator inputGenerator, int inputRowLimit, String filePath, int relativeAttributeIndex, long maxMemoryUsage, int memoryCheckFrequency) throws AlgorithmExecutionException {
		RelationalInput relationalInput = null;
		int numValuesSinceLastMemoryCheck = 0;
		List<String> spilledFiles = new ArrayList<String>();
		
		try {
			relationalInput = inputGenerator.generateNewCopy();
			
			// Read values sorted
			Set<String> values = new TreeSet<String>();
			while (relationalInput.hasNext() && (inputRowLimit != 0)) {
				String value = relationalInput.next().get(relativeAttributeIndex);
				inputRowLimit--;
				
				if (value == null)
					continue;
				
				// Replace line breaks with the zero-character, because these line breaks would otherwise split values when later written to plane-text files
				value = value.replaceAll("\n", "\0");
				
				values.add(value);
				numValuesSinceLastMemoryCheck++;
				
				// Occasionally check the memory consumption
				if (numValuesSinceLastMemoryCheck >= memoryCheckFrequency) {
					numValuesSinceLastMemoryCheck = 0;
					
					// Spill to disk if necessary
					if (ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() > maxMemoryUsage) {								
						String spillFilePath = filePath + "#" + spilledFiles.size();
						write(spillFilePath, values);
						spilledFiles.add(spillFilePath);
						values = new TreeSet<String>();
						
						System.gc();
					}
				}
			}
			
			if (spilledFiles.isEmpty()) {
				// Write sorted values to disk
				write(filePath, values);
			}
			else {
				// Write last file
				if (!values.isEmpty()) {
					String spillFilePath = filePath + "#" + spilledFiles.size();
					write(spillFilePath, values);
					spilledFiles.add(spillFilePath);
					values = new TreeSet<String>();
					
					System.gc();
				}
				
				// Read, merge and write
				merge(filePath, spilledFiles);
			}
		}
		finally {
			try {
				relationalInput.close();
			}
			catch (Exception e) {
			}
		}
	}
	
	private static void write(String filePath, Set<String> values) throws AlgorithmExecutionException {
		File file = new File(filePath);
		boolean append = file.exists();
		
		BufferedWriter writer = null;
		try {
			writer = FileUtils.buildFileWriter(filePath, false); // TODO: Use Metanome temp file functionality here
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
		catch (IOException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		finally {
			FileUtils.close(writer);
		}
	}
	
	private static void merge(String filePath, List<String> spilledFiles) throws AlgorithmExecutionException {
		BufferedReader[] readers = new BufferedReader[spilledFiles.size()];
		PriorityQueue<TPMMSTuple> values = new PriorityQueue<TPMMSTuple>(spilledFiles.size());
		BufferedWriter writer = null;
		
		try {
			for (int readerNumber = 0; readerNumber < spilledFiles.size(); readerNumber++) {
				BufferedReader reader = FileUtils.buildFileReader(spilledFiles.get(readerNumber));
				readers[readerNumber] = reader;
				String value = reader.readLine();
				
				if (value != null)
					values.add(new TPMMSTuple(value, readerNumber));
			}
			
			writer = FileUtils.buildFileWriter(filePath, false);
			String previousValue = null;
			while (!values.isEmpty()) {
				TPMMSTuple tuple = values.remove();
				if ((previousValue == null) || (!previousValue.equals(tuple.value)))
					writer.write(tuple.value);
				
				previousValue = tuple.value;
				tuple.value = readers[tuple.readerNumber].readLine();
				
				if (tuple.value != null)
					values.add(tuple);
				
				if (!values.isEmpty())
					writer.newLine();
			}
			writer.flush();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new AlgorithmExecutionException(e.getMessage());
		}
		finally {
			FileUtils.close(writer);
			for (BufferedReader reader : readers)
				FileUtils.close(reader);
		}
	}

}
