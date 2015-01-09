package fdiscovery.preprocessing;

import fdiscovery.general.ColumnFiles;
import fdiscovery.general.Miner;
import gnu.trove.map.hash.TCharIntHashMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


public class SVFileProcessor extends InputFileProcessor {

	private char delimiter;
	private int numberOfColumns;
	private int numberOfRows;
	private ColumnFiles columnFiles;
	
	public SVFileProcessor(File source) throws FileNotFoundException, IOException {
		super(source);
	}

	public SVFileProcessor(String sourceFilename) throws FileNotFoundException, IOException {
		super(sourceFilename);
	}

	public void init() {
		this.setDelimiter();
		this.setNumberOfRows();
		this.setNumberOfColumns();
	}

	public void init(char delimiter, int numberOfColumns, int numberOfRows) throws IOException {
		this.delimiter = delimiter;
		this.numberOfColumns = numberOfColumns;
		this.numberOfRows = numberOfRows;
	}

	public void init(char delimiter) throws IOException {
		this.delimiter = delimiter;
		this.setNumberOfRows();
		this.setNumberOfColumns();
	}
	
	public final void createColumnFiles() {
		
		// if the column files already were created don't do it again
		if (this.columnFiles != null) {
			return;
		}

		this.resetReader();
		// create temporary column directory
		File temporaryColumnDirectory = this.getColumnDirectory();
		this.columnFiles = new ColumnFiles(temporaryColumnDirectory, this.numberOfColumns);
		// open column file writers
		try {
			this.columnFiles.initializeWriters();
		} catch (IOException e) {
			System.out.println("ColumnFileWriters couldn't be initialized.");
		}
		
		// get the column segments and write them out
		ArrayList<ArrayList<String>> contentOfSegments;
		while ((contentOfSegments = this.getNextColumnSegments()) != null) {
			for (int columnIndex = 0; columnIndex < contentOfSegments.size(); columnIndex++) {
				try {
					this.columnFiles.get(columnIndex).writeColumnSegment(contentOfSegments.get(columnIndex));
				} catch (IOException e) {
					System.out.println("Couldn't write column segment.");
				}
			}
		}
		
		// close the column file writers
		try {
			this.columnFiles.closeWriters();
		} catch (IOException e) {
			System.out.println("ColumnFileWriters couldn't be closed.");
		}
	}

	public void freeColumns() {
		this.columnFiles.clear();
	}
	
	public char getDelimiter() {
		return this.delimiter;
	}

	public int getNumberOfColumns() {
		return this.numberOfColumns;
	}

	public int getNumberOfRows() {
		return this.numberOfRows;
	}

	public ColumnFiles getColumnFiles() {
		return this.columnFiles;
	}

	public final void setDelimiter(char delimiter) {
		this.delimiter = delimiter;
	}

	public final void setNumberOfRows(int numberOfRows) {
		this.numberOfRows = numberOfRows;
	}

	public final void setNumberOfColumns(int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
	}

	private final ArrayList<ArrayList<String>> getNextColumnSegments() {
		
		// initialize the data structure that holds the column segments
		ArrayList<ArrayList<String>> contentOfSegments = new ArrayList<ArrayList<String>>();
		for (int currentColumn = 0; currentColumn < this.numberOfColumns; currentColumn++) {
			contentOfSegments.add(new ArrayList<String>(Miner.SEGMENT_SIZE));
		}
		
		// iterate over the input file line by line
		// split the line at the delimiter and add each value to its dedicated column
		// return the segment when it's full or the end of the input file is reached
		String line = null;
		int currentSegmentSize = 0;
		try {
			while (((line = this.inputFileReader.readLine()) != null) || currentSegmentSize != 0) {
				currentSegmentSize++;
				int columnIndex = 0;
				if (line != null) {
					for (String columnOfLine : line.split(String.valueOf(this.delimiter), this.numberOfColumns)) {
						contentOfSegments.get(columnIndex).add(columnOfLine);
						columnIndex++;
					}
				}
	
				if (currentSegmentSize == Miner.SEGMENT_SIZE || line == null) {
					return contentOfSegments;
				}
			}
		} catch (IOException e) {
			System.out.println("Error while reading input file.");
		}
	
		return null;
	}

	private final File getColumnDirectory() {
		// take the extension less input file name as the directory name
		String directoryName = Miner.COLUMN_FILE_PATH + this.source.getName().split("\\.")[0];
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdirs();
		}
		
		return directory;
	}
	
	public final String getColumnDirectoryName() {
		// take the extension less input file name as the directory name
		String directoryName = Miner.COLUMN_FILE_PATH + this.source.getName().split("\\.")[0];
		File directory = new File(directoryName);
		return directory.getAbsolutePath();
	}

	private final void setDelimiter() {
		this.resetReader();
	
		String currentLine = null;
		String referenceLine = null;
		TCharIntHashMap currentCountMap = new TCharIntHashMap();
		TCharIntHashMap referenceCountMap = new TCharIntHashMap();
	
		// determine the value delimiter by scanning the first line and counting the occurrence of all characters
		// match all following lines with the first line
		// all characters with a different occurrence count cannot be valid delimiters
		// terminate when there is only one possible delimiter left
		try {
			referenceLine = this.inputFileReader.readLine();
			for (char c : referenceLine.toCharArray()) {
				referenceCountMap.adjustOrPutValue(c, 1, 1);
			}
	
			while ((currentLine = this.inputFileReader.readLine()) != null && referenceCountMap.size() > 1) {
				currentCountMap.clear();
				for (char c : currentLine.toCharArray()) {
					currentCountMap.adjustOrPutValue(c, 1, 1);
				}
				for (char c : referenceCountMap.keys()) {
					if (referenceCountMap.get(c) != currentCountMap.get(c)) {
						referenceCountMap.remove(c);
					}
				}
			}
		} catch (IOException e) {
			System.out.println("The input file could not be processed.");
		}
		if (referenceCountMap.size() > 1) {
			if (referenceCountMap.keySet().contains('\t')) {
				this.delimiter = '\t';
			} else if (referenceCountMap.keySet().contains(',')) {
				this.delimiter = ',';
			} else if (referenceCountMap.keySet().contains(';')) {
				this.delimiter = ';';
			}
		} else {
			this.delimiter = referenceCountMap.keys()[0];
		}
	}

	private final void setNumberOfRows() {
		this.resetReader();
	
		try {
			while (this.inputFileReader.readLine() != null) {
				this.numberOfRows++;
			}
		} catch (IOException e) {
			System.out.println("The input file could not be processed.");
		}
	}

	private final void setNumberOfColumns() {
		this.resetReader();
	
		// determine the number of columns given by the delimiter
		try {
			String line = null;
			if ((line = this.inputFileReader.readLine()) != null) {
				for (char c : line.toCharArray()) {
					if (this.delimiter == c) {
						this.numberOfColumns++;
					}
				}
				// behind the last column there is no delimiter
				this.numberOfColumns++;
			}
		} catch (IOException e) {
			System.out.println("The input file could not be processed.");
		}
	}
}
