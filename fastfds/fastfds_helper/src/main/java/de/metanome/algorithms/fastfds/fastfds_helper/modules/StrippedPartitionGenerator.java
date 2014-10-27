package de.metanome.algorithms.fastfds.fastfds_helper.modules;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

import de.metanome.algorithm_integration.AlgorithmExecutionException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithms.fastfds.fastfds_helper.modules.container.StrippedPartition;

public class StrippedPartitionGenerator extends Algorithm_Group2_Modul {

    // TODO: besser null Wert Ersatz?
    public static String nullValue = "null#" + Math.random();

    private List<StrippedPartition> returnValue;
    private Int2ObjectMap<Map<String, LongList>> translationMaps = new Int2ObjectOpenHashMap<Map<String, LongList>>();

    public StrippedPartitionGenerator(int numberOfThreads) {

        super(numberOfThreads, "StripptedPartitionGen");

    }

    // TODO: Parallelisieren
    public List<StrippedPartition> execute(RelationalInput input) throws AlgorithmExecutionException {

        if (this.timeMesurement) {
            this.startTime();
        }

        int lineNumber = 0;

        // Daten auslesen und in TranslationMaps stopfen
        while (input.hasNext()) {

            List<String> line = input.next();

            for (int spalte = 0; spalte < line.size(); spalte++) {

                String content = line.get(spalte);
                if (null == content) {
                    content = StrippedPartitionGenerator.nullValue;
                }

                Map<String, LongList> translationMap;
                if ((translationMap = this.translationMaps.get(spalte)) == null) {
                    translationMap = new HashMap<String, LongList>();
                    this.translationMaps.put(spalte, translationMap);
                }
                LongList element;
                if ((element = translationMap.get(content)) == null) {
                    element = new LongArrayList();
                    translationMap.put(content, element);
                }
                element.add(lineNumber);

            }

            lineNumber++;
        }

        // Auslesen der Listen und Erzeugung der Stripped Partitions
        if (this.optimize()) {
            this.returnValue = new CopyOnWriteArrayList<StrippedPartition>();
            ExecutorService exec = this.getExecuter();
            for (int i : this.translationMaps.keySet()) {

                exec.execute(new StrippedPartitionGenerationTASK(i));
            }
            this.awaitExecuter(exec);
        } else {
            this.returnValue = new LinkedList<StrippedPartition>();
            for (int i : this.translationMaps.keySet()) {
                executeStrippedPartitionGenerationTask(i);
            }
        }
        // Aufräumen
        this.translationMaps.clear();

        if (this.timeMesurement) {
            this.stopTime();
        }

        return this.returnValue;

    }

    private void executeStrippedPartitionGenerationTask(int i) {

        StrippedPartition sp = new StrippedPartition(i);
        this.returnValue.add(sp);

        Map<String, LongList> toItterate = this.translationMaps.get(i);

        for (LongList it : toItterate.values()) {

            if (it.size() > 1) {
                sp.addElement(it);
            }

        }

        // Nach getaner Arbeit aufräumen
        this.translationMaps.get(i).clear();
    }

    private class StrippedPartitionGenerationTASK implements Runnable {

        private int task;

        public StrippedPartitionGenerationTASK(int i) {

            this.task = i;

        }

        @Override
        public void run() {

            executeStrippedPartitionGenerationTask(this.task);

        }
    }
}