package fdiscovery.general;

import java.io.File;

public class Miner {

	public static final String input = "data/pdb/t015.tsv";
	
	public static final String COLUMN_FILE_PATH = "columns" + File.separator;
	public static final String RESULT_FILE_PATH = "results" + File.separator;
	public static final String BENCHMARK_FILE_REGEX = "c(\\d+)r(\\d+)(.+?)";
	public static final int STATUS_OK = 0;
	public static final int STATUS_OOT = 1;
	public static final int STATUS_OOM = 2;
	
	public static final int SEGMENT_SIZE = 10000;

	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	protected static final void createColumDirectory() {
		File columnDir = new File(Miner.COLUMN_FILE_PATH);
		if (!columnDir.exists()) {
			columnDir.mkdir();
		}
	}
	
	protected static final void createResultDirectory() {
		File resultDir = new File(Miner.RESULT_FILE_PATH);
		if (!resultDir.exists()) {
			resultDir.mkdir();
		}
	}
}	
