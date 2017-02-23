package de.hpi.mpss2015n.approxind.utils;

import de.metanome.algorithm_integration.input.*;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Does not actually store columns. Instead, it only creates the sample that is of major importance for FAIDA.
 * Other than that, it just exposes the original data store as a {@link AbstractColumnStore column store}.
 */
public final class VirtualColumnStore extends AbstractColumnStore {

    /**
     * {@link RelationalInputGenerator} for the backing relation. Will be used to serve rows.
     */
    private final RelationalInputGenerator inputGenerator;

    /**
     * Whether the {@link #inputGenerator} should be close after a {@link ColumnIterator} is closed.
     */
    private final boolean isCloseConnectionsRigorously;

    /**
     * Creates a new instance but does not load it.
     *
     * @param numColumns the number of columns to be hosted in this instance
     * @param sampleGoal see {@link #sampleGoal}
     * @see #load(String, int, RelationalInput)
     */
    VirtualColumnStore(int numColumns, int sampleGoal, RelationalInputGenerator inputGenerator, boolean isCloseConnectionsRigorously) {
        super(numColumns, sampleGoal);
        this.inputGenerator = inputGenerator;
        this.isCloseConnectionsRigorously = isCloseConnectionsRigorously;
    }


    /**
     * @param activeColumns columns that should be read
     * @return iterator for selected columns
     */
    public ColumnIterator getRows(SimpleColumnCombination activeColumns) {
        this.initHashCaches();

        try {
            return new VirtualColumnStore.ColumnIterator(this.inputGenerator.generateNewCopy(), activeColumns);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void writeColumnsAndSample(RelationalInput input) throws InputIterationException, IOException {
        List<List<String>> alternativeSamples = new ArrayList<>();
        final List<LongSet> sampledColumnValues = new ArrayList<>();
        for (int i = 0; i < this.getNumberOfColumns(); i++) {
            sampledColumnValues.add(new LongOpenHashSet(this.sampleGoal < 0 ? 1000 : this.sampleGoal));
        }
        // Keeps track of the first value in each column if it is the only value seen.
        Long[] firstColumnValues = new Long[this.getNumberOfColumns()];

        int rowCounter = 0;
        DebugCounter counter = new DebugCounter();
        while (input.hasNext()) {
            List<String> row = input.next();
            boolean isSampleCompleted = true;
            boolean rowHasUnseenValue = false;
            for (int i = 0; i < this.getNumberOfColumns(); i++) {
                // Write the hash to the column.
                String str = row.get(i);
                long hash = getHash(str, i);

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

            if (isSampleCompleted) break;
        }
        counter.done();
        writeSample(alternativeSamples);

        // Check for constant and null columns.
        for (int i = 0; i < firstColumnValues.length; i++) {
            this.isNullColumn[i] = firstColumnValues[i] != null && firstColumnValues[i] == NULLHASH;
            this.isConstantColumn[i] = firstColumnValues[i] != null && firstColumnValues[i] != NULLHASH;
        }
    }

    /**
     * Create a columns store for each fileInputGenerators
     *
     * @param fileInputGenerators input
     * @param isCloseConnectionsRigorously  whether connections for reading should be closed when currently not needed
     * @return column stores
     */
    public static VirtualColumnStore[] create(RelationalInputGenerator[] fileInputGenerators, int sampleGoal, boolean isCloseConnectionsRigorously) {
        VirtualColumnStore[] stores = new VirtualColumnStore[fileInputGenerators.length];

        for (int tableNumber = 0; tableNumber < fileInputGenerators.length; tableNumber++) {
            RelationalInputGenerator generator = fileInputGenerators[tableNumber];
            try (RelationalInput input = generator.generateNewCopy()) {
                String datasetDir;
                if (generator instanceof FileInputGenerator) {
                    datasetDir = ((FileInputGenerator) generator).getInputFile().getParentFile().getName();
                } else {
                    datasetDir = "unknown";
                }
                stores[tableNumber] = new VirtualColumnStore(input.numberOfColumns(), sampleGoal, generator, isCloseConnectionsRigorously);
                stores[tableNumber].load(datasetDir, tableNumber, input);
                input.close();
                if (isCloseConnectionsRigorously) {
                  generator.close();
                }
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

        private final RelationalInput relationalInput;
        private final long[] data;
        private final SimpleColumnCombination activeColumns;
        private final AOCacheMap<String, Long> hashCache;

        ColumnIterator(RelationalInput relationalInput, SimpleColumnCombination activeColumns) {
            this.relationalInput = relationalInput;
            this.activeColumns = activeColumns;
            this.data = new long[activeColumns.getColumns().length];
            this.hashCache = new AOCacheMap<>(CACHE_THRESHOLD * this.activeColumns.getColumns().length);
        }

        @Override
        public boolean hasNext() {
            try {
                return this.relationalInput.hasNext();
            } catch (InputIterationException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long[] next() {
            try {
                List<String> tuple = this.relationalInput.next();
                for (int i = 0; i < this.activeColumns.getColumns().length; i++) {
                    String value = tuple.get(this.activeColumns.getColumn(i));
                    long hash = hash(value, this.hashCache);
                    this.data[i] = hash;
                }
            } catch (InputIterationException e) {
                throw new RuntimeException(e);
            }
            return this.data;
        }

        @Override
        public void close() {
            try {
                this.relationalInput.close();
                if (VirtualColumnStore.this.isCloseConnectionsRigorously) {
                    VirtualColumnStore.this.inputGenerator.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}

