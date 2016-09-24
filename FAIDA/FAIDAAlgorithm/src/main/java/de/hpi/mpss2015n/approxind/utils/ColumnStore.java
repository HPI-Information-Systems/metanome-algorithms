package de.hpi.mpss2015n.approxind.utils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores the columns of a relational table and a row-wise sample.
 */
public final class ColumnStore {

    private static final Logger logger = LoggerFactory.getLogger(ColumnStore.class);

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
    private static final int CACHE_THRESHOLD = 10;

    private final File[] columns;
    private final File sample;
    private final ArrayList<AOCacheMap<String, Long>> hashCaches = new ArrayList<>();
    /**
     * The minimum number of distinct values of each column to include in the sample (unless there are less values).
     **/
    private final int sampleGoal;
    private boolean[] isConstantColumn;
    private Set<SimpleColumnCombination> nullColumns;
    private File constantColumnsFile;
    private Long[] constantColumnValues;
    private final List<LongSet> sampledColumnValues;


    /**
     * Creates a new instance, thereby creating all relevant files.
     *
     * @param sampleGoal see {@link #sampleGoal}
     */
    ColumnStore(String dataset, int table, RelationalInput input, int sampleGoal) {
        // Do some initialization work.
        this.sampleGoal = sampleGoal;
        this.columns = new File[input.numberOfColumns()];
        isConstantColumn = new boolean[columns.length];
        Arrays.fill(isConstantColumn, true);
        sampledColumnValues = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            sampledColumnValues.add(new LongOpenHashSet(this.sampleGoal));
        }

        constantColumnValues = new Long[columns.length];
        //Arrays.fill(constantColumnValues, null);

        nullColumns = new HashSet<>();

        // Prepare the working directory.
        String tableName = com.google.common.io.Files.getNameWithoutExtension(input.relationName());
        Path dir = Paths.get(DIRECTORY, dataset, tableName);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.info("writing table {} to {}", table, dir.toAbsolutePath());

        int i = 0;
        for (String column : input.columnNames()) {
            columns[i++] = new File(dir.toFile(), "" + table + "_" + column + ".bin");
        }

        sample = new File(dir.toFile(), "" + table + "-sample.csv");
        constantColumnsFile = new File(dir.toFile(), "" + table + "_" + "constantColumns" + ".csv");

        File processingIndicator = new File(dir.toFile(), "" + table + "_PROCESSING");
        try {
            processingIndicator.createNewFile();
            Stopwatch sw = Stopwatch.createStarted();
            writeColumns(input);

            if (constantColumnsFile.exists()) {
                constantColumnsFile.delete();
            }
            constantColumnsFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(constantColumnsFile);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
            for (int j = 0; j < columns.length; j++) {
                bw.write(Long.toString(constantColumnValues[j]));
                bw.newLine();
            }
            bw.close();

            logger.info("{}", sw);
            processingIndicator.delete();
        } catch (InputIterationException | IOException e) {
            throw new RuntimeException(e);
        }

        for (int j = 0; j < columns.length; j++) {
            if (isConstantColumn[j] && constantColumnValues[j] == NULLHASH) {
                nullColumns.add(SimpleColumnCombination.create(table, j));
            }
        }
    }

    /**
     * @return Iterator for all columns
     */
    public ColumnIterator getRows() {
        int[] ids = new int[columns.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = i;
        }
        return getRows(new SimpleColumnCombination(0, ids));
    }

    public List<long[]> getSample() {
        return readSample();
    }

    /**
     * @param activeColumns columns that should be read
     * @return iterator for selected columns
     */
    public ColumnIterator getRows(SimpleColumnCombination activeColumns) {
        FileInputStream[] in = new FileInputStream[activeColumns.getColumns().length];
        int i = 0;
        for (int col : activeColumns.getColumns()) {
            try {
                in[i++] = new FileInputStream(columns[col]);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new ColumnIterator(in);
    }

    private void writeColumns(RelationalInput input) throws InputIterationException, IOException {

        FileOutputStream[] out = new FileOutputStream[columns.length];
        FileChannel[] channel = new FileChannel[columns.length];
        ByteBuffer[] bb = new ByteBuffer[columns.length];
        for (int i = 0; i < columns.length; i++) {
            out[i] = new FileOutputStream(columns[i]);
            channel[i] = out[i].getChannel();
            bb[i] = ByteBuffer.allocateDirect(BUFFERSIZE);
            hashCaches.add(new AOCacheMap<>(CACHE_THRESHOLD));
        }

        List<List<String>> alternativeSamples = new ArrayList<>();

        int rowCounter = 0;
        DebugCounter counter = new DebugCounter();
        while (input.hasNext()) {
            if (bb[0].remaining() == 0) {
                for (int i = 0; i < columns.length; i++) {
                    bb[i].flip();
                    channel[i].write(bb[i]);
                    bb[i].clear();
                }
            }
            List<String> row = input.next();

            boolean newValue = false;

            for (int i = 0; i < columns.length; i++) {
                String str = row.get(i);
                long hash = getHash(str, i);
                bb[i].putLong(hash);

                if (hash != NULLHASH) {
                    final LongSet sampledColumnValues = this.sampledColumnValues.get(i);
                    if (sampledColumnValues.size() < this.sampleGoal && sampledColumnValues.add(hash)) {
                        newValue = true;
                    }
                }

                if (isConstantColumn[i]) {
                    if (rowCounter == 0) {
                        constantColumnValues[i] = hash;
                    } else if (hash != constantColumnValues[i]) {
                        constantColumnValues[i] = 0L;
                        isConstantColumn[i] = false;
                    }
                }
            }

            if (newValue) {
                alternativeSamples.add(row);
            }

            counter.countUp();
            rowCounter++;
        }
        counter.done();
        writeSample(alternativeSamples);

        for (int i = 0; i < columns.length; i++) {
            bb[i].flip();
            channel[i].write(bb[i]);
            out[i].close();
        }
    }

    private void writeSample(List<List<String>> rows) {
        try {
            BufferedWriter writer = com.google.common.io.Files.newWriter(sample, Charsets.UTF_8);
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

    private List<long[]> readSample() {
        List<long[]> result = new ArrayList<>();
        try {
            BufferedReader reader = com.google.common.io.Files.newReader(sample, Charsets.UTF_8);

            List<long[]> hashSample = reader.lines().map(row -> {
                long[] hashes = new long[columns.length];
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

    private long getHash(String string, int column) {
        AOCacheMap<String, Long> hashCache = hashCaches.get(column);
        if (string == null) {
            return NULLHASH;
        } else {
            Long hash = hashCache.get(string);
            if (hash == null) {
                long h = HASH_FUNCTION.hashString(string, Charsets.UTF_8).asLong();
                if (hashCache.size() < CACHE_THRESHOLD) {
                    hashCache.put(string, h);
                }
                return h;
            } else
                return hash;
        }
    }

    /**
     * Create a columns store for each fileInputGenerators
     *
     * @param fileInputGenerators input
     * @return column stores
     */
    public static ColumnStore[] create(RelationalInputGenerator[] fileInputGenerators, int sampleGoal) {
        ColumnStore[] stores = new ColumnStore[fileInputGenerators.length];

        for (int i = 0; i < fileInputGenerators.length; i++) {
            RelationalInputGenerator generator = fileInputGenerators[i];
            try (RelationalInput input = generator.generateNewCopy();) {
                String datasetDir;
                if (generator instanceof FileInputGenerator) {
                    datasetDir = ((FileInputGenerator) generator).getInputFile().getParentFile().getName();
                } else {
                    datasetDir = "unknown";
                }
                stores[i] = new ColumnStore(datasetDir, i, input, sampleGoal);
                input.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return stores;
    }

    public Set<SimpleColumnCombination> getNullColumns() {
        return nullColumns;
    }

    public int getNumberOfColumns() {
        return columns.length;
    }

    public boolean[] getIsConstantColumn() {
        return isConstantColumn;
    }

    public Long[] getConstantColumnValues() {
        return constantColumnValues;
    }

}

