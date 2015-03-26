package de.metanome.algorithms.anelosimus.driver;

import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.Parameter;

public class AnelosimusParameters {

    @Parameter(names = "-databaseName", description = "name of the database to use")
    String databaseName;

    @Parameter(names = "-inputFileEnding", description = "input file ending")
    String inputFileEnding;

    @Parameter(names = "-inputFolderPath", description = "path to the database")
    String inputFolderPath;

    @Parameter(names = { "-hasHeader" }, description = "input files have header", arity = 1)
    boolean hasHeader = true;

    @Parameter(names = { "-seperator" }, description = "seperator char")
    String seperator = ",";

    @Parameter(names = { "-quote" }, description = "quote char")
    String quote = "\"";

    @Parameter(names = { "-escape" }, description = "escape (use 0 for \0)")
    String escape = "\\";

    @Parameter(names = "-outputFile", description = "output file")
    String outputFile;

    @Parameter(names = { "-limit" }, description = "max table count, -1 for no limit")
    int limit = -1;

    @Parameter(names = { "-rowLimit" }, description = "max row count, -1 for no limit")
    int inputRowLimit = -1;

    @Parameter(names = "-nEstStrategy", description = "strategy for n estimation")
    String nEstStrategy;

    @Parameter(names = { "-p" }, description = "p in promille")
    int p = 0;

    @Parameter(names = { "-m" }, description = "bloom filter size")
    int m = 512;
    @Parameter(names = { "-k" }, description = "hash function count")
    int k = 3;
    @Parameter(names = { "-passes" }, description = "passes count")
    int passes = 1;
    @Parameter(names = "-nullValues", description = "The host")
    List<String> nullValues = new ArrayList<String>();
    @Parameter(names = { "-dop" }, description = "degree of parallelism")
    int dop = 1;
    @Parameter(names = { "-refCoverageMinPercentage" }, description = "minimum ref value coverage")
    int refCoverageMinPercentage;
    @Parameter(names = { "-verify" }, description = "verify INDs", arity = 1)
    boolean verify = true;
    @Parameter(names = { "-output" }, description = "output INDs", arity = 1)
    boolean output = true;
    @Parameter(names = { "-filterNonUniqueRefs" }, description = "filter non unique refs", arity = 1)
    boolean filterNonUniqueRefs = false;
    @Parameter(names = { "-filterNullCols" }, description = "filter null cols", arity = 1)
    boolean filterNullCols = false;
    @Parameter(names = { "-filterNumericAndShortCols" }, description = "filter numeric and short cols", arity = 1)
    boolean filterNumericAndShortCols = false;
    @Parameter(names = { "-strategyRef2Deps" }, description = "use ref2deps strategy", arity = 1)
    boolean strategyRef2Deps = false;
    @Parameter(names = { "-filterDependentRefs" }, description = "filter dependent refs", arity = 1)
    boolean filterDependentRefs = false;
    @Parameter(names = { "-useFastVector" }, description = "use fast (hierarchical) vector implementation", arity = 1)
    boolean useFastVector = false;
    @Parameter(names = { "-condenseMatrix" }, description = "condense the matrix and filter out inactive columns", arity = 1)
    boolean condenseMatrix;

}
