package fdiscovery.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class InputFileProcessor {
	
	protected File source;
	protected BufferedReader inputFileReader;
	
	public InputFileProcessor(String sourceFilename) throws FileNotFoundException, IOException {
		this.source = new File(sourceFilename);
		this.inputFileReader = new BufferedReader(new FileReader(this.source));
	}
	
	public InputFileProcessor(File source) throws FileNotFoundException, IOException {
		this.source = source;
		this.inputFileReader = new BufferedReader(new FileReader(this.source));
	}
	
	protected void resetReader() {
		try {
			this.inputFileReader = new BufferedReader(new FileReader(this.source));
		} catch (FileNotFoundException e) {
			System.out.println("The reader could not be reset.");
			e.printStackTrace();
		}
	}
	
}
