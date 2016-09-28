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
import java.util.ArrayList;
import java.util.List;
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

    private final File[] columnFiles;
    private final File sampleFile;
    private final ArrayList<AOCacheMap<String, Long>> hashCaches = new ArrayList<>();
    /**
     * The minimum number of distinct values of each column to include in the sample (unless there are less values).
     **/
    private final int sampleGoal;

    private boolean[] isConstantColumn, isNullColumn;


    /**
     * Creates a new instance, thereby creating all relevant files.
     *
     * @param sampleGoal see {@link #sampleGoal}
     */
    ColumnStore(String dataset, int table, RelationalInput input, int sampleGoal) {
        // Do some initialization work.
        this.sampleGoal = sampleGoal;
        this.columnFiles = new File[input.numberOfColumns()];
        isConstantColumn = new boolean[columnFiles.length];
        isNullColumn = new boolean[columnFiles.length];

        // Prepare the working directory.
        String tableName = com.google.common.io.Files.getNameWithoutExtension(input.relationName());
        Path dir = Paths.get(DIRECTORY, dataset, tableName);
        logger.info("writing table {} to {}", table, dir.toAbsolutePath());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int i = 0;
        for (String column : input.columnNames()) {
            columnFiles[i++] = new File(dir.toFile(), "" + table + "_" + column + ".bin");
        }
        sampleFile = new File(dir.toFile(), "" + table + "-sample.csv");

        // Read the original data and update all deliverables.
        File processingIndicator = new File(dir.toFile(), "" + table + "_PROCESSING");
        try {
            if (!processingIndicator.createNewFile()) {
                logger.warn("Could not create processing indicator.");
            }
            Stopwatch sw = Stopwatch.createStarted();
            writeColumns(input);
            logger.info("{}", sw);
            if (!processingIndicator.delete()) {
                logger.warn("Could not delete processing indicator.");
            }
        } catch (InputIterationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return Iterator for all columns
     */
    public ColumnIterator getRows() {
        int[] ids = new int[columnFiles.length];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = i;
        }
        return getRows(new SimpleColumnCombination(0, ids));
    }

    public List<long[]> getSampleFile() {
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
                in[i++] = new FileInputStream(columnFiles[col]);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return new ColumnIterator(in);
    }

    private void writeColumns(RelationalInput input) throws InputIterationException, IOException {

        FileOutputStream[] out = new FileOutputStream[columnFiles.length];
        FileChannel[] channel = new FileChannel[columnFiles.length];
        ByteBuffer[] bb = new ByteBuffer[columnFiles.length];
        for (int i = 0; i < columnFiles.length; i++) {
            out[i] = new FileOutputStream(columnFiles[i]);
            channel[i] = out[i].getChannel();
            bb[i] = ByteBuffer.allocateDirect(BUFFERSIZE);
            hashCaches.add(new AOCacheMap<>(CACHE_THRESHOLD));
        }

        List<List<String>> alternativeSamples = new ArrayList<>();
        final List<LongSet> sampledColumnValues = new ArrayList<>();
        for (int i = 0; i < columnFiles.length; i++) {
            sampledColumnValues.add(new LongOpenHashSet(this.sampleGoal));
        }
        Long[] firstColumnValues = new Long[columnFiles.length];

        int rowCounter = 0;
        DebugCounter counter = new DebugCounter();
        while (input.hasNext()) {
            // Prepare the output buffers.
            if (bb[0].remaining() == 0) {
                for (int i = 0; i < columnFiles.length; i++) {
                    bb[i].flip();
                    channel[i].write(bb[i]);
                    bb[i].clear();
                }
            }

            List<String> row = input.next();
            boolean rowHasUnseenValue = false;
            for (int i = 0; i < columnFiles.length; i++) {
                // Write the hash to the column.
                String str = row.get(i);
                long hash = getHash(str, i);
                bb[i].putLong(hash);

                // Keep track of the first column value and delete it if multiple values are observed.
                if (rowCounter == 0) firstColumnValues[i] = hash;
                else if (firstColumnValues[i] != null && firstColumnValues[i] != hash) firstColumnValues[i] = null;

                // Check if the value requests to put the row into the sample.
                if (hash != NULLHASH) {
                    final LongSet sampledValues = sampledColumnValues.get(i);
                    if (sampledValues.size() < this.sampleGoal && sampledValues.add(hash)) {
                        rowHasUnseenValue = true;
                    }
                }
            }

            if (rowHasUnseenValue) {
                alternativeSamples.add(row);
            }

            counter.countUp();
            rowCounter++;
        }
        counter.done();
        writeSample(alternativeSamples);

        // Check for constant and null columns.
        for (int i = 0; i < firstColumnValues.length; i++) {
            isNullColumn[i] = firstColumnValues[i] != null && firstColumnValues[i] == NULLHASH;
            isConstantColumn[i] = firstColumnValues[i] != null && firstColumnValues[i] != NULLHASH;
        }

        for (int i = 0; i < columnFiles.length; i++) {
            bb[i].flip();
            channel[i].write(bb[i]);
            out[i].close();
        }
    }

    private void writeSample(List<List<String>> rows) {
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

    private List<long[]> readSample() {
        try {
            BufferedReader reader = com.google.common.io.Files.newReader(sampleFile, Charsets.UTF_8);

            List<long[]> hashSample = reader.lines().map(row -> {
                long[] hashes = new long[columnFiles.length];
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

    public int getNumberOfColumns() {
        return columnFiles.length;
    }

    public boolean isConstantColumn(int columnIndex) {
        return isConstantColumn[columnIndex];
    }

    public boolean isNullColumn(int columnIndex) {
        return isNullColumn[columnIndex];
    }
}

