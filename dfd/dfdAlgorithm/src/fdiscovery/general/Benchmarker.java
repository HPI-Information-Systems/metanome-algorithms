package fdiscovery.general;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;

import fdiscovery.preprocessing.SVFileProcessor;
import gnu.trove.map.hash.THashMap;

public class Benchmarker {
	
	protected static File[] getBenchmarkFilesWithPattern(File benchmarkDirectory) {
		File[] benchmarkFiles = benchmarkDirectory.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.matches(Miner.BENCHMARK_FILE_REGEX);
			}
		});
		return benchmarkFiles;
	}
	
	protected static final String getResultFileName(String inputDirectory, String miner) {
		String[] splitInputDirectory = inputDirectory.split("\\" + File.separator);
		if (splitInputDirectory.length >= 2) {
			String staticComponent = splitInputDirectory[splitInputDirectory.length-1];
			String source = splitInputDirectory[splitInputDirectory.length-2];
			return String.format("%s%s-%s-%s.dat", Miner.RESULT_FILE_PATH, miner, staticComponent, source);
		} 
		return new String();
	}
	
	protected static final void writeErrorCode(File resultFile, int exitCode) {
		try {
			BufferedWriter resultFileWriter = new BufferedWriter(new FileWriter(resultFile, true));
			if (exitCode == Miner.STATUS_OOT) {
				resultFileWriter.write("#OOT");
			} else if (exitCode == Miner.STATUS_OOM) {
				resultFileWriter.write("#OOM");
			}
			resultFileWriter.close();
		} catch (IOException e) {
			System.out.println("Couldn't write meta data.");
		}
	}
	
	protected static final void writeMetaData(File resultFile, THashMap<String, String> cmdLine) {
		StringBuilder metaDataLineBuilder = new StringBuilder();
		for (String optionKey : cmdLine.keySet()) {
			if (cmdLine.get(optionKey) != null) {
				metaDataLineBuilder.append(String.format("# %s :\t%s\n", optionKey, cmdLine.get(optionKey)));
				System.out.print(String.format("# %s :\t%s\n", optionKey, cmdLine.get(optionKey)));
			} else {
				metaDataLineBuilder.append(String.format("# %s :\t%s\n", optionKey, "true"));
				System.out.print(String.format("# %s :\t%s\n", optionKey, "true"));
			}
		}
		metaDataLineBuilder.append("#Filename\t#Rows\t#Columns\tTime\t#Deps\t#<2Deps\t#<3Deps\t#<4Deps\t#<5Deps\t#<6Deps\t#>5Deps\t#Partitions\n");
		System.out.println("#Filename\t#Rows\t#Columns\tTime\t#Deps\t#<2Deps\t#<3Deps\t#<4Deps\t#<5Deps\t#<6Deps\t#>5Deps\t#Partitions\n");
		try {
			BufferedWriter resultFileWriter = new BufferedWriter(new FileWriter(resultFile));
			resultFileWriter.write(metaDataLineBuilder.toString());
			resultFileWriter.close();
		} catch (IOException e) {
			System.out.println("Couldn't write meta data.");
		}
	}
	
	public static void main(String[] args) {
		CLIParserBenchmarker parser = new CLIParserBenchmarker();
		THashMap<String, String> cmdLine = parser.parse(args);
		String inputDirectoryName = new String();
		String miner = new String();
		char delimiter = '\t';
		String xmx = new String();
		int timeout = -1;
		boolean allFiles = false;
		
		if (cmdLine.contains("input")) {
			inputDirectoryName = cmdLine.get("input");
		}
		if (cmdLine.contains("miner")) {
			miner = cmdLine.get("miner");
		}
		if (cmdLine.contains("delimiter")) {
			delimiter = (cmdLine.get("delimiter")).charAt(0);
		}
		if (cmdLine.contains("xmx")) {
			xmx = cmdLine.get("xmx");
		}
		if (cmdLine.contains("timeout")) {
			System.out.println(String.format("Timeout:%s", cmdLine.get("timeout")));
			timeout = Integer.valueOf(cmdLine.get("timeout"));
		}
		if (cmdLine.containsKey("all")) {
			System.out.println("Use all files.");
			allFiles = true;
		}
		File executable = null;
		if (miner.equals("tane")) {
			executable = new File("tane.jar");
		} else if (miner.equals("fastfds")) {
			executable = new File("fastfds.jar");
		} else if (miner.equals("dfd")) {
			executable = new File("dfd.jar");
		} 
		else {
			System.out.println(String.format("No valid miner:\t%s", miner));
			System.exit(1);
		}
		
		File inputDirectory = new File(inputDirectoryName);
		if (!inputDirectory.exists()) {
			System.out.println("Input directory doesn't exist.");
			System.exit(1);
		}
		
		File[] benchmarkFiles = new File[0];
		if (allFiles) {
			benchmarkFiles = inputDirectory.listFiles();
		} else {
			benchmarkFiles = getBenchmarkFilesWithPattern(inputDirectory);
		}
		Arrays.sort(benchmarkFiles);
		
		if (benchmarkFiles.length != 0) {
			Miner.createColumDirectory();
			Miner.createResultDirectory();
			String resultFilename = getResultFileName(inputDirectory.getAbsolutePath(), miner);
			File resultFile = new File(resultFilename);
			writeMetaData(resultFile, cmdLine);
			boolean errors = false;
			for (File benchmarkFile : benchmarkFiles) {
				if (!errors) {
					try {
						// create columns files and collect meta data
						SVFileProcessor fileProcessor = new SVFileProcessor(benchmarkFile);
						fileProcessor.init(delimiter);
						fileProcessor.createColumnFiles();

						// build command line with parameters
						CommandLine processCmdLine = new CommandLine("java");
						processCmdLine.addArgument("-d64");
						processCmdLine.addArgument("-XX:GCTimeLimit=90");
						processCmdLine.addArgument("-XX:GCHeapFreeLimit=10");
						processCmdLine.addArgument("-XX:+UseSerialGC");
						processCmdLine.addArgument(String.format("-Xmx%s", xmx));
						processCmdLine.addArgument("-jar");
						processCmdLine.addArgument(executable.getName());
						processCmdLine.addArgument("-file");
						processCmdLine.addArgument(String.valueOf(benchmarkFile.getName()));
						processCmdLine.addArgument("-columns");
						processCmdLine.addArgument(String.valueOf(fileProcessor.getNumberOfColumns()));
						processCmdLine.addArgument("-rows");
						processCmdLine.addArgument(String.valueOf(fileProcessor.getNumberOfRows()));
						processCmdLine.addArgument("-result");
						processCmdLine.addArgument(resultFile.getAbsolutePath());
						processCmdLine.addArgument("-input");
						processCmdLine.addArgument(fileProcessor.getColumnDirectoryName());

						// build process with watchdog
						DefaultExecutor executor = new DefaultExecutor();
						ExecuteWatchdog watchdog = new ExecuteWatchdog(timeout);
						executor.setWatchdog(watchdog);

						// handle results
						DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
						PumpStreamHandler streamHandler = new PumpStreamHandler();
						executor.setStreamHandler(streamHandler);
						long timeStart = System.currentTimeMillis();
						executor.execute(processCmdLine, resultHandler);
						resultHandler.waitFor(timeout);

						long timeEnd = System.currentTimeMillis();
						System.out.println(String.format("Time:%.1f", (double)(timeEnd - timeStart)/1000));

						int exitCode = 0;
						if (resultHandler.hasResult()) {
							exitCode = resultHandler.getExitValue();
						} else {
							exitCode = Miner.STATUS_OOT;
							executor.getWatchdog().destroyProcess();
						}
							
						if (watchdog.killedProcess()) {
							exitCode = Miner.STATUS_OOT;
							executor.getWatchdog().destroyProcess();
						} else {
						}
						System.out.println(String.format("ExitCode %d", exitCode));
						if (exitCode == Miner.STATUS_OK) {

						} else if (exitCode == Miner.STATUS_OOT || exitCode == Miner.STATUS_OOM) {
							writeErrorCode(resultFile, exitCode);
							errors = true;
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
			}
		}
		
	}
}
