package fdiscovery.general;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CLIParserMiner extends GnuParser {

	@SuppressWarnings("static-access")
	
	public CLIParserMiner() {
		Options options = new Options();
		Option inputFileName = OptionBuilder.withArgName("file")
				.hasArg()
				.withDescription("Input file name.")
				.create("file");
		Option inputDirectory = OptionBuilder.withArgName("input")
				.hasArg()
				.withDescription("Column files directory.")
				.create("input");
		Option resultFile = OptionBuilder.withArgName("result")
				.hasArg()
				.withDescription("Result file.")
				.create("result");
		Option numberOfColumns = OptionBuilder.withArgName("columns")
				.hasArg()
				.withDescription("Number of columns.")
				.create("columns");
		Option numberOfRows = OptionBuilder.withArgName("rows")
				.hasArg()
				.withDescription("Number of rows.")
				.create("rows");
		
		options.addOption(inputFileName);
		options.addOption(inputDirectory);
		options.addOption(resultFile);
		options.addOption(numberOfColumns);
		options.addOption(numberOfRows);
		
		this.setOptions(options); 
	}
	
	public CommandLine parse(String[] cli) {
		CommandLine result = null;
		try {
			result = this.parse(this.getOptions(), cli);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

}
