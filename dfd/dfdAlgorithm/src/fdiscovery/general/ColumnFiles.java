package fdiscovery.general;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;

public class ColumnFiles extends ArrayList<ColumnFile> implements FileFilter {

	private static final long serialVersionUID = 3184846549251977445L;

	private int numberOfColumns;
	private File directory;
	private String formatString;
	
	public ColumnFiles(final File directory, final int numberOfColumns) {
		this.numberOfColumns = numberOfColumns;
		this.directory = directory;
		this.formatString = "%0" + ((int)Math.log10(this.numberOfColumns)+1) + "d" + ColumnFile.extension;
		for (int columnIndex = 0; columnIndex < this.numberOfColumns; columnIndex++) {
			String columnFileName = this.getColumnFileName(columnIndex);
			this.add(new ColumnFile(this.directory, columnFileName, columnIndex));
		}
	}
	
	public ColumnFiles(final File directory, final int numberOfColumns, int numberOfRows) {
		this.numberOfColumns = numberOfColumns;
		this.directory = directory;
		this.formatString = "%0" + ((int)Math.log10(this.numberOfColumns)+1) + "d" + ColumnFile.extension;
		for (int columnIndex = 0; columnIndex < this.numberOfColumns; columnIndex++) {
			String columnFileName = this.getColumnFileName(columnIndex);
			this.add(new ColumnFile(this.directory, columnFileName, columnIndex, numberOfRows));
		}
	}
	
	public int getNumberOfColumns() {
		return this.numberOfColumns;
	}
	
	public final void initializeWriters() throws IOException {
		for (ColumnFile file : this) {
			file.initializeWriter();
		}
	}
	
	public final void closeWriters() throws IOException {
		for (ColumnFile file : this) {
			file.closeWriter();
		}
	}
	
	@Override
	public boolean accept(File file) {
		if (file.getName().matches("^(\\d)+\\.col$")) {
			return true;
		}
		return false;
	}

	private final String getColumnFileName(final int columnIndex) {
		return String.format(this.formatString, Integer.valueOf(columnIndex));
	}
}
