package de.hpi.mpss2015n.approxind.sampler;

import au.com.bytecode.opencsv.CSVReader;

import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.backend.results_db.FileInput;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomAccessSample implements RelationalInput {

  Iterator<List<String>> iterator;
  List<String> columnNames;
  String relationName;
  RelationalInput input;

  public RandomAccessSample(RelationalInput input, long numSamples){
    iterator = getSample((FileInput) input, numSamples).iterator();
    columnNames = input.columnNames();
    relationName = input.relationName();
    this.input = input;
  }

  public static List<List<String>> getSample(FileInput input, long numSamples){
    input.getFileName();
    File file = new File(input.getFileName());
    long length = file.length();
    Set<List<String>> set = new HashSet<>();
    Random rnd = new Random();

    while (set.size() < numSamples) {
      try {
        RandomAccessFile rndFile = new RandomAccessFile(file, "r");
        long randomPosition = (long)(rnd.nextDouble()*(length));
        rndFile.seek(randomPosition);
        rndFile.readLine();
        String line = rndFile.readLine();
        if(line == null) continue;
        Reader in = new StringReader(line);
        CSVReader csvReader = new CSVReader(in,
                                            input.getSeparatorAsChar(),
                                            input.getQuoteCharAsChar(),
                                            input.getEscapeCharAsChar(),
                                            input.getSkipLines(),
                                            input.isStrictQuotes(),
                                            input.isIgnoreLeadingWhiteSpace());
        String[] values = csvReader.readNext();
        List<String> valueList = new ArrayList<>();
        for (String val : values) {
          if (val.equals("")) {
            valueList.add(null);
          } else {
            valueList.add(val);
          }
          if (!set.contains(valueList))
            set.add(valueList);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    //Todo: return value - + wrapper for interface compatibility
    return new ArrayList<>(set);
  }

  @Override
  public boolean hasNext() throws InputIterationException {
    return iterator.hasNext();
  }

  @Override
  public List<String> next() throws InputIterationException {
    return iterator.next();
  }

  @Override
  public int numberOfColumns() {
    return columnNames.size();
  }

  @Override
  public String relationName() {
    return relationName;
  }

  @Override
  public List<String> columnNames() {
    return columnNames;
  }

  @Override
  public void close() throws Exception {
    input.close();

  }
}
