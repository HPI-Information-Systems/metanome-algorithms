package de.metanome.algorithms.tireless;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TirelessAlgorithmLocal extends TirelessAlgorithm {

    @Override
    protected List<Map<String, Integer>> initialize() {

        final String sourceFile = "C:\\Users\\TobiasNiedling\\Downloads\\starwis\\combined";
        final char delimiter = '\t';

        try {
            Reader in = new FileReader(sourceFile);
            List<CSVRecord> records;
            records = CSVFormat
                    .DEFAULT
                    .withFirstRecordAsHeader()
                    .withTrim()
                    .withNullString("")
                    .withEscape('\\')
                    .withDelimiter(delimiter)
                    .parse(in)
                    .getRecords();
            List<Map<String, Integer>> values = new ArrayList<>();
            int numRecords = records.get(0).size();
            for (int i = 0; i < numRecords; i++)
                values.add(new HashMap<>());
            for (CSVRecord record : records)
                for (int i = 0; i < numRecords; i++)
                    values.get(i).put(record.get(i), values.get(i).getOrDefault(record.get(i), 0) + 1);

            this.relationName = "local test";
            this.columnNames = new ArrayList<>() {{
                for(int i = 0; i < numRecords; i++)
                    add("#" + i);
            }};

            maximumElementCount = 10000000;
            minimalSpecialCharacterOccurrence = 0.2;
            disjunctionMergingThreshold = 10;
            maximumLengthDeviationFactor = 2;
            charClassGeneralizationThreshold = 4;
            quantifierGeneralizationThreshold = 5;
            outlierThreshold = 0.01;

            return values;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void emitResult(int columnIndex, String expression) {
        System.out.println(expression);
    }
}
