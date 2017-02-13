package de.hpi.mpss2015n.approxind.utils;

import com.google.common.base.Verify;
import de.metanome.algorithm_integration.input.FileInputGenerator;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores the columns of a relational table and a row-wise sample.
 */
public final class HashedColumnStore extends AbstractColumnStore {

    /**
     * The files that contain the hashes of the stored columns.
     */
    private final File[] columnFiles;

    /**
     * Whether to reuse column files from earlier runs.
     */
    private final boolean isReuseColumnFiles;

    /**
     * Whether a certain column file has/had to be created.
     */
    private boolean[] isNew;


    /**
     * Creates a new instance but does not load it.
     *
     * @param numColumns         the number of columns to be hosted in this instance
     * @param sampleGoal         see {@link #sampleGoal}
     * @param isReuseColumnFiles whether to reuse column files from earlier runs
     * @see #load(String, int, RelationalInput)
     */
    HashedColumnStore(int numColumns, int sampleGoal, boolean isReuseColumnFiles) {
        super(numColumns, sampleGoal);

        // Do some initialization work.
        this.columnFiles = new File[numColumns];
        this.isNew = new boolean[columnFiles.length];
        this.isReuseColumnFiles = isReuseColumnFiles;
    }

    @Override
    protected Path prepareDirectory(String dataset, int table, RelationalInput input) {
        Path dir = super.prepareDirectory(dataset, table, input);

        // Create the column files if necessary.
        int i = 0;
        for (String column : input.columnNames()) {
            File columnFile = new File(dir.toFile(), "" + table + "_" + column + ".bin");
            columnFiles[i] = columnFile;
            isNew[i++] = !isReuseColumnFiles || !columnFile.exists();
        }

        return dir;
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
        return new HashedColumnStore.ColumnIterator(in);
    }

    @Override
    protected void writeColumnsAndSample(RelationalInput input) throws InputIterationException, IOException {
        boolean isWritingAnyColumn = false;
        FileOutputStream[] out = new FileOutputStream[columnFiles.length];
        FileChannel[] channel = new FileChannel[columnFiles.length];
        ByteBuffer[] bb = new ByteBuffer[columnFiles.length];
        for (int i = 0; i < columnFiles.length; i++) {
            if (!isNew[i]) continue;
            out[i] = new FileOutputStream(columnFiles[i]);
            channel[i] = out[i].getChannel();
            bb[i] = ByteBuffer.allocateDirect(BUFFERSIZE);
            isWritingAnyColumn = true;
        }

        List<List<String>> alternativeSamples = new ArrayList<>();
        final List<LongSet> sampledColumnValues = new ArrayList<>();
        for (int i = 0; i < columnFiles.length; i++) {
            sampledColumnValues.add(new LongOpenHashSet(this.sampleGoal < 0 ? 1000 : this.sampleGoal));
        }
        Long[] firstColumnValues = new Long[columnFiles.length];

        int rowCounter = 0;
        DebugCounter counter = new DebugCounter();
        while (input.hasNext()) {
            List<String> row = input.next();
            boolean isSampleCompleted = true;
            boolean rowHasUnseenValue = false;
            for (int i = 0; i < columnFiles.length; i++) {
                // Write the hash to the column.
                String str = row.get(i);
                long hash = getHash(str, i);
                if (isNew[i]) {
                    if (bb[i].remaining() == 0) {
                        bb[i].flip();
                        channel[i].write(bb[i]);
                        bb[i].clear();
                    }
                    bb[i].putLong(hash);
                }

                // Keep track of the first column value and delete it if multiple values are observed.
                if (rowCounter == 0) firstColumnValues[i] = hash;
                else if (firstColumnValues[i] != null && firstColumnValues[i] != hash) firstColumnValues[i] = null;

                // Check if the value requests to put the row into the sample.
                if (hash != NULLHASH) {
                    final LongSet sampledValues = sampledColumnValues.get(i);
                    boolean shouldSample = this.sampleGoal < 0 || sampledValues.size() < this.sampleGoal;
                    isSampleCompleted &= !shouldSample;
                    if (shouldSample && sampledValues.add(hash)) {
                        rowHasUnseenValue = true;
                    }
                }
            }

            if (rowHasUnseenValue) {
                alternativeSamples.add(row);
            }

            counter.countUp();
            rowCounter++;

            if (!isWritingAnyColumn && isSampleCompleted) break;
        }
        counter.done();
        writeSample(alternativeSamples);

        // Check for constant and null columns.
        for (int i = 0; i < firstColumnValues.length; i++) {
            this.isNullColumn[i] = firstColumnValues[i] != null && firstColumnValues[i] == NULLHASH;
            this.isConstantColumn[i] = firstColumnValues[i] != null && firstColumnValues[i] != NULLHASH;
        }

        for (int i = 0; i < columnFiles.length; i++) {
            if (!isNew[i]) continue;
            bb[i].flip();
            channel[i].write(bb[i]);
            out[i].close();
        }
    }

    /**
     * Create a columns store for each fileInputGenerators
     *
     * @param fileInputGenerators input
     * @param isReuseColumnFiles  whether existing column vectors can be reused
     * @return column stores
     */
    public static HashedColumnStore[] create(RelationalInputGenerator[] fileInputGenerators, int sampleGoal, boolean isReuseColumnFiles) {
        HashedColumnStore[] stores = new HashedColumnStore[fileInputGenerators.length];

        for (int tableNumber = 0; tableNumber < fileInputGenerators.length; tableNumber++) {
            RelationalInputGenerator generator = fileInputGenerators[tableNumber];
            try (RelationalInput input = generator.generateNewCopy()) {
                String datasetDir;
                if (generator instanceof FileInputGenerator) {
                    datasetDir = ((FileInputGenerator) generator).getInputFile().getParentFile().getName();
                } else {
                    datasetDir = "unknown";
                }
                stores[tableNumber] = new HashedColumnStore(input.numberOfColumns(), sampleGoal, isReuseColumnFiles);
                stores[tableNumber].load(datasetDir, tableNumber, input);
                input.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return stores;
    }

    /**
     * Provides the contents of a column file.
     */
    private final class ColumnIterator implements de.hpi.mpss2015n.approxind.utils.ColumnIterator {

        private final FileInputStream[] in;
        private final FileChannel[] channel;
        private final ByteBuffer[] bb;
        private final long[] data;
        private boolean hasMore = true;

        ColumnIterator(FileInputStream[] in) {
            this.in = in;
            channel = new FileChannel[in.length];
            bb = new ByteBuffer[in.length];
            //ipArr = new long[in.length][];
            for (int i = 0; i < in.length; i++) {
                channel[i] = in[i].getChannel();
                bb[i] = ByteBuffer.allocateDirect(HashedColumnStore.BUFFERSIZE);
            }
            readNext();
            this.data = new long[in.length];
        }

        @Override
        public boolean hasNext() {
            return bb[0].remaining() > 0 || hasMore && readNext();
        }

        @Override
        public long[] next() {
            for (int i = 0; i < in.length; i++) {
                data[i] = bb[i].getLong();
            }
            return data;
        }

        private boolean readNext() {
            Verify.verify(hasMore, "invalid call");
            try {
                for (int i = 0; i < in.length; i++) {
                    int len = 0;
                    bb[i].clear();
                    hasMore &= ((len = channel[i].read(bb[i])) != -1);
                    bb[i].flip();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return hasMore;
        }

        @Override
        public void close() {
            try {
                for (FileInputStream input : in) {
                    input.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

