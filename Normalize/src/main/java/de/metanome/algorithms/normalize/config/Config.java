package de.metanome.algorithms.normalize.config;

import java.io.File;

public class Config {
	
	public enum Dataset {
		ABALONE, ADULT, BALANCE, BREAST, BRIDGES, CHESS, ECHODIAGRAM, FD15, FD30, FLIGHT, HEPATITIS, HORSE, IRIS, LETTER, NURSERY, PETS, 
		NCVOTER_1K, NCVOTER_2K, NCVOTER_4K, NCVOTER_8K, NCVOTER_16K, NCVOTER_32K, NCVOTER_64K, NCVOTER_128K, NCVOTER_256K, NCVOTER_512K, NCVOTER_1024K,
		NCVOTER_STATEWIDE, NCVOTER_STATEWIDE_10K, NCVOTER_STATEWIDE_100K,
		PLISTA, PLISTA_10, PLISTA_20, PLISTA_30, PLISTA_40, PLISTA_50, PLISTA_60,
		UNIPROD_1K, UNIPROD_1K_10, UNIPROD_1K_20, UNIPROD_1K_30, UNIPROD_1K_40, UNIPROD_1K_50, UNIPROD_1K_60, UNIPROD_1K_80,
		TPCH, AMALGAM, MUSICBRAINZ, TEST
	}
	
	private Dataset defaultDataset = Dataset.NCVOTER_STATEWIDE_10K;//NCVOTER_1K;//MUSICBRAINZ;//Dataset.HORSE;//TPCH;//AMALGAM;//TEST;//HEPATITIS;
	
	public boolean isHumanInTheLoop = true;
	
	public String inputDatasetName = "";
	public String inputFolderPath = ".." + File.separator + "HyFDTestRunner" + File.separator + "data" + File.separator;
//	public String inputFolderPath = "." + File.separator + "data" + File.separator;
	public String inputFileEnding = ".csv";
	public char inputFileSeparator = ';';
	public char inputFileQuotechar = '\"';
	public char inputFileEscape = '\\';
	public int inputFileSkipLines = 0;
	public boolean inputFileStrictQuotes = false;
	public boolean inputFileIgnoreLeadingWhiteSpace = true;
	public boolean inputFileHasHeader = false;
	public boolean inputFileSkipDifferingLines = true;
	public String inputFileNullString = "";
	
	public String measurementsFolderPath = "io" + File.separator + "measurements" + File.separator;
	
	public String statisticsFileName = "statistics.txt";
	public String resultFileName = "results.txt";
	
	public boolean writeResults = true;
	
	public boolean nullEqualsNull = true;

	public Config() {
		this.setDataset(this.defaultDataset);
	}
	
	public Config(Config.Dataset dataset) {
		this.setDataset(dataset);
	}

	public void setDataset(String dataset) {
		for (Config.Dataset enumEntry : Config.Dataset.values()) {
			if (enumEntry.toString().equals(dataset)) {
				this.setDataset(enumEntry);
				return;
			}
		}
		throw new RuntimeException("Unknown dataset: ’" + dataset + "'");
	}

	public void setDataset(Config.Dataset dataset) {
		switch (dataset) {
			case ABALONE:
				this.inputDatasetName = "abalone";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case ADULT:
				this.inputDatasetName = "adult";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case BALANCE:
				this.inputDatasetName = "balance-scale";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case BREAST:
				this.inputDatasetName = "breast-cancer-wisconsin";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case BRIDGES:
				this.inputDatasetName = "bridges";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case CHESS:
				this.inputDatasetName = "chess";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case ECHODIAGRAM:
				this.inputDatasetName = "echocardiogram";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case FD15:
				this.inputDatasetName = "fd_reduced_15";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case FD30:
				this.inputDatasetName = "fd-reduced-30";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case FLIGHT:
				this.inputDatasetName = "flight_1k";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = true;
				break;
			case HEPATITIS:
				this.inputDatasetName = "hepatitis";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case HORSE:
				this.inputDatasetName = "horse";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case IRIS:
				this.inputDatasetName = "iris";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case LETTER:
				this.inputDatasetName = "letter";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case NURSERY:
				this.inputDatasetName = "nursery";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = false;
				break;
			case PETS:
				this.inputDatasetName = "pets";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_1K:
				this.inputDatasetName = "ncvoter_1001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_2K:
				this.inputDatasetName = "ncvoter_2001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_4K:
				this.inputDatasetName = "ncvoter_4001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_8K:
				this.inputDatasetName = "ncvoter_8001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_16K:
				this.inputDatasetName = "ncvoter_16001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_32K:
				this.inputDatasetName = "ncvoter_32001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_64K:
				this.inputDatasetName = "ncvoter_64001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_128K:
				this.inputDatasetName = "ncvoter_128001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_256K:
				this.inputDatasetName = "ncvoter_256001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_512K:
				this.inputDatasetName = "ncvoter_512001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_1024K:
				this.inputDatasetName = "ncvoter_1024001r_19c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case PLISTA:
				this.inputDatasetName = "plista_1k";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case PLISTA_10:
				this.inputDatasetName = "plista_1k_1001r_10c";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case PLISTA_20:
				this.inputDatasetName = "plista_1k_1001r_20c";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case PLISTA_30:
				this.inputDatasetName = "plista_1k_1001r_30c";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case PLISTA_40:
				this.inputDatasetName = "plista_1k_1001r_40c";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case PLISTA_50:
				this.inputDatasetName = "plista_1k_1001r_50c";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case PLISTA_60:
				this.inputDatasetName = "plista_1k_1001r_60c";
				this.inputFileSeparator = ';';
				this.inputFileHasHeader = false;
				break;
			case UNIPROD_1K:
				this.inputDatasetName = "uniprot_1001r_223c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case UNIPROD_1K_10:
				this.inputDatasetName = "uniprot_1001r_10c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case UNIPROD_1K_20:
				this.inputDatasetName = "uniprot_1001r_20c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case UNIPROD_1K_30:
				this.inputDatasetName = "uniprot_1001r_30c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case UNIPROD_1K_40:
				this.inputDatasetName = "uniprot_1001r_40c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case UNIPROD_1K_50:
				this.inputDatasetName = "uniprot_1001r_50c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case UNIPROD_1K_60:
				this.inputDatasetName = "uniprot_1001r_60c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case UNIPROD_1K_80:
				this.inputDatasetName = "uniprot_1001r_80c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_STATEWIDE:
				this.inputDatasetName = "ncvoter_Statewide";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_STATEWIDE_10K:
				this.inputDatasetName = "ncvoter_Statewide_10001r_71c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case NCVOTER_STATEWIDE_100K:
				this.inputDatasetName = "ncvoter_Statewide_100001r_71c";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case TPCH:
				this.inputDatasetName = "tpch_denormalized";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case AMALGAM:
				this.inputDatasetName = "amalgam1_denormalized";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case MUSICBRAINZ:
				this.inputDatasetName = "musicbrainz_denormalized";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
			case TEST:
				this.inputDatasetName = "test";
				this.inputFileSeparator = ',';
				this.inputFileHasHeader = true;
				break;
		}
	}
	
	@Override
	public String toString() {
		return "Config:\r\n\t" +
				"inputDatasetName: " + this.inputDatasetName + "\r\n\t" +
				"inputFolderPath: " + this.inputFolderPath + "\r\n\t" +
				"inputFileEnding: " + this.inputFileEnding + "\r\n\t" +
				"inputFileSeparator: " + this.inputFileSeparator + "\r\n\t" +
				"inputFileQuotechar: " + this.inputFileQuotechar + "\r\n\t" +
				"inputFileEscape: " + this.inputFileEscape + "\r\n\t" +
				"inputFileSkipLines: " + this.inputFileSkipLines + "\r\n\t" +
				"inputFileStrictQuotes: " + this.inputFileStrictQuotes + "\r\n\t" +
				"inputFileIgnoreLeadingWhiteSpace: " + this.inputFileIgnoreLeadingWhiteSpace + "\r\n\t" +
				"inputFileHasHeader: " + this.inputFileHasHeader + "\r\n\t" +
				"inputFileSkipDifferingLines: " + this.inputFileSkipDifferingLines + "\r\n\t" +
				"measurementsFolderPath: " + this.measurementsFolderPath + "\r\n\t" +
				"statisticsFileName: " + this.statisticsFileName + "\r\n\t" +
				"resultFileName: " + this.resultFileName + "\r\n\t" +
				"writeResults: " + this.writeResults + "\r\n\t" +
				"nullEqualsNull: " + this.nullEqualsNull;
	}
}
