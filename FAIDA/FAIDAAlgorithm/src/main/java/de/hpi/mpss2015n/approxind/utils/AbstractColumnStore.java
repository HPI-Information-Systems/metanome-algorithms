package de.hpi.mpss2015n.approxind.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores the columns of a relational table and a row-wise sample.
 */
public abstract class AbstractColumnStore {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Temporary directory for the created files.
     */
    public static final String DIRECTORY = "temp/";
    public static final int BUFFERSIZE = 1024 * 1024;
    public static final HashFunction HASH_FUNCTION = Hashing.murmur3_128();
    public static final long NULLHASH = HASH_FUNCTION.hashString("", Charsets.UTF_8).asLong();

    /**
     * Number of hashes to cache while converting the original data.
     */
    protected static final int CACHE_THRESHOLD = 10;

    protected final ArrayList<AOCacheMap<String, Long>> hashCaches = new ArrayList<>();

    private File sampleFile;

    /**
     * The minimum number of distinct values of each column to include in the sample (unless there are less values).
     **/
    protected final int sampleGoal;

    protected boolean[] isConstantColumn;
    protected boolean[] isNullColumn;


    /**
     * Creates a new instance, thereby creating all relevant files.
     *
     * @param numColumns number of columns to host
     * @param sampleGoal see {@link #sampleGoal}
     */
    AbstractColumnStore(int numColumns, int sampleGoal) {
        // Do some initialization work.
        this.sampleGoal = sampleGoal;
        this.isConstantColumn = new boolean[numColumns];
        this.isNullColumn = new boolean[numColumns];
    }

    /**
     * Load a table into this instance.
     *
     * @param dataset the name of the dataset that comprises the table to be loaded
     * @param table   the index of the table to be loaded w.r.t. the dataset
     * @param input   provides the tuples of the table to be loaded
     */
    protected void load(String dataset, int table, RelationalInput input) {
        Path dir = this.prepareDirectory(dataset, table, input);

        // Read the original data and update all deliverables.
        File processingIndicator = new File(dir.toFile(), "" + table + "_PROCESSING");
        try {
            if (!processingIndicator.createNewFile()) {
                logger.warn("Could not create processing indicator.");
            }
            Stopwatch sw = Stopwatch.createStarted();
            this.initHashCaches();
            this.writeColumnsAndSample(input);
            this.hashCaches.clear();
            logger.info("{}", sw);
            if (!processingIndicator.delete()) {
                logger.warn("Could not delete processing indicator.");
            }
        } catch (InputIterationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void initHashCaches() {
        for (int i = 0; i < this.getNumberOfColumns(); i++) {
            this.hashCaches.add(new AOCacheMap<>(CACHE_THRESHOLD));
        }
    }

    /**
     * Prepare the directory to host the column store.
     */
    protected Path prepareDirectory(String dataset, int table, RelationalInput input) {
        String tableName = makeFileName(input.relationName());
        Path dir = Paths.get(DIRECTORY, dataset, tableName);
        logger.info("writing table {} to {}", table, dir.toAbsolutePath());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.sampleFile = new File(dir.toFile(), "" + table + "-sample.csv");

        return dir;
    }

    /**
     * Replace any unusual characters with underscores.
     *
     * @param relationName the name to be freed from suspicious characters
     * @return the cleansed file name
     */
    private static String makeFileName(String relationName) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < relationName.length(); i++) {
            char c = relationName.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '-' && c != '.') {
                sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Create a new {@link ColumnIterator} for all columns of the table.
     *
     * @return {@link ColumnIterator} for all columns
     */
    public ColumnIterator getRows() {
        int[] ids = new int[this.getNumberOfColumns()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = i;
        }
        return getRows(new SimpleColumnCombination(0, ids));
    }

    /**
     * @param activeColumns columns that should be read
     * @return iterator for selected columns
     */
    abstract public ColumnIterator getRows(SimpleColumnCombination activeColumns);

    abstract protected void writeColumnsAndSample(RelationalInput input) throws InputIterationException, IOException;

    /**
     * Write the sampled rows to a the {@link #sampleFile}.
     *
     * @param rows the sample
     */
    protected void writeSample(List<List<String>> rows) {
        try {
            BufferedWriter writer = com.google.common.io.Files.newWriter(sampleFile, Charsets.UTF_8);
            for (List<String> row : rows) {
                Long[] hashes = new Long[row.size()];
                for (int i = 0; i < row.size(); i++) {
                    hashes[i] = getHash(row.get(i), i);
                }
                writer.write(Joiner.on(',').join(hashes));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the {@link #sampleFile}.
     *
     * @return the hashed tuples from that file
     */
    protected List<long[]> readSample() {
        try {
            BufferedReader reader = com.google.common.io.Files.newReader(sampleFile, Charsets.UTF_8);

            List<long[]> hashSample = reader.lines().map(row -> {
                long[] hashes = new long[this.getNumberOfColumns()];
                int i = 0;
                for (String hash : Splitter.on(',').split(row)) {
                    hashes[i++] = Long.parseLong(hash);
                }
                return hashes;
            }).collect(Collectors.toList());

            reader.close();
            return hashSample;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<long[]> getSampleFile() {
        return this.readSample();
    }

    /**
     * Retrieve the hash for a given value.
     *
     * @param string that should be hashed
     * @param column that contains the the {@code string}
     * @return the hash
     */
    protected long getHash(String string, int column) {
        return hash(string, this.hashCaches.get(column));
    }

    /**
     * Creates a hash for the given {@link String}.
     *
     * @param string    that should be hashed
     * @param hashCache to cache hashes or {@code null}
     * @return the hash
     */
    protected static long hash(String string, AOCacheMap<String, Long> hashCache) {
        if (string == null || string.isEmpty()) {
            return NULLHASH;
        } else {
            Long hash = hashCache == null ? null : hashCache.get(string);
            if (hash == null) {
                long h = HASH_FUNCTION.hashString(string, Charsets.UTF_8).asLong();
                if (h == NULLHASH)
                    h = NULLHASH + 1; // avoid this very hash collision as NULLHASH has specific implications
                if (hashCache != null && hashCache.size() < CACHE_THRESHOLD) {
                    hashCache.put(string, h);
                }
                return h;
            } else
                return hash;
        }
    }

    public int getNumberOfColumns() {
        return this.isConstantColumn.length;
    }

    public boolean isConstantColumn(int columnIndex) {
        return isConstantColumn[columnIndex];
    }

    public boolean isNullColumn(int columnIndex) {
        return isNullColumn[columnIndex];
    }
}

