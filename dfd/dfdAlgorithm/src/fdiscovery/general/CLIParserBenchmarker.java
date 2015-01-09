package fdiscovery.general;

import gnu.trove.map.hash.THashMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CLIParserBenchmarker extends GnuParser {

	@SuppressWarnings("static-access")
	
	public CLIParserBenchmarker() {
		Options options = new Options();
		Option inputDirectory = OptionBuilder.withArgName("input")
				.hasArg()
				.withDescription("Benchmark files directory.")
				.create("input");
		Option miner = OptionBuilder.withArgName("miner")
				.hasArg()
				.withDescription("Miner: tane, fastfds, fdiscminer.")
				.create("miner");
		Option delimiter = OptionBuilder.withArgName("delimiter")
				.hasArg()
				.withDescription("Delimiter of input files.")
				.create("delimiter");
		Option xmxForMiner = OptionBuilder.withArgName("xmx")
				.hasArg()
				.withDescription("Maximum heap space for miner.")
				.create("xmx");
		Option timeout = OptionBuilder.withArgName("timeout")
				.hasArg()
				.withDescription("Maximum calculation time for miner.")
				.create("timeout");
		
		Option allFiles = new Option("all", false, "Use all files in directory.");
		
		options.addOption(inputDirectory);
		options.addOption(miner);
		options.addOption(delimiter);
		options.addOption(xmxForMiner);
		options.addOption(timeout);
		options.addOption(allFiles);
		
		this.setOptions(options); 
	}
	
	public THashMap<String, String> parse(String[] input) {
		THashMap<String, String> result = new THashMap<>();
		try {
			CommandLine cmdLine = this.parse(this.getOptions(), input);
			for (Option option : cmdLine.getOptions()) {
				result.put(option.getOpt(), option.getValue());
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return result;
	}

}
