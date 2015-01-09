package fdiscovery.general;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class ColumnFile extends File {
	
	private static final long serialVersionUID = 8010572343178891121L;
	
	private BufferedWriter writer;
	private int numberOfValues;
	private int columnIndex;
	
	public static final String extension = ".col";
	
	public ColumnFile(File columnFile, int columnIndex) {
		super(columnFile.getAbsolutePath());
		this.columnIndex = columnIndex;
	}
	
	public ColumnFile(File directory, String fileName, int columnIndex) {
		super(directory, fileName);
		this.columnIndex = columnIndex;
	}
	
	public ColumnFile(File directory, String fileName, int columnIndex, int numberOfRows) {
		super(directory, fileName);
		this.columnIndex = columnIndex;
		this.numberOfValues = numberOfRows;
	}
	
	public final void writeColumnSegment(ArrayList<String> columnSegment) throws IOException {
		
		for (String value : columnSegment) {
			this.writer.write(value);
			this.writer.newLine();
			this.numberOfValues++;
		}
	}
	
	public final void initializeWriter() throws IOException {
		this.numberOfValues = 0;
		this.writer = new BufferedWriter(new FileWriter(this));
	}
	
	public final void closeWriter() throws IOException {
		this.writer.close();
	}
	
	public final String[] getContent() {
		String[] fileContent = new String[this.numberOfValues];
		try {
			BufferedReader reader = new BufferedReader(new FileReader(this));
			String line = null;
			int lineIndex = 0;
			while ((line = reader.readLine()) != null) {
				fileContent[lineIndex++] = line;
			}
			reader.close();

		} catch (FileNotFoundException e) {
			System.out.println("Could not find input column file.");
		} catch (IOException e) {
			System.out.println("Error while creating output file.");
		}
		
		return fileContent;
	}
	
	public int getColumnIndex() {
		return this.columnIndex;
	}
} 
