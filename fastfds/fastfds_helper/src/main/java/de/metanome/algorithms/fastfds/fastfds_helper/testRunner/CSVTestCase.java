package de.metanome.algorithms.fastfds.fastfds_helper.testRunner;

import com.google.common.collect.ImmutableList;
import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithm_integration.result_receiver.CouldNotReceiveResultException;
import de.metanome.algorithm_integration.result_receiver.FunctionalDependencyResultReceiver;
import de.metanome.algorithm_integration.results.FunctionalDependency;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class CSVTestCase implements RelationalInput, RelationalInputGenerator, FunctionalDependencyResultReceiver {

    private static String filePath = "D:\\Workspaces\\dirttesMastersemester\\algorithm_group2\\Data\\";
    private static String defaultFileName = "dbtesmaData.c100000.r10";
    private static boolean defaultHasHeader = false;
    private static BufferedWriter bw;
    private BufferedReader br;
    private boolean hasHeader;
    private String fileName;
    private String nextLine;
    private int numberOfColumns;
    private ImmutableList<String> names;
    private String delimiter;

    public CSVTestCase() throws IOException {

        this(CSVTestCase.defaultFileName, CSVTestCase.defaultHasHeader);
    }

    public CSVTestCase(String fileName, boolean hasHeader) throws IOException {

        this.fileName = fileName;
        this.hasHeader = hasHeader;

        this.br = new BufferedReader(new FileReader(new File(CSVTestCase.filePath + fileName)));
        this.nextLine = this.br.readLine();

        if (this.nextLine.split(",").length > this.nextLine.split(";").length) {
            this.delimiter = ",";
        } else {
            this.delimiter = ";";
        }

        this.calcNumbers();
        this.getNames();

    }

    public static List<String> getAllFileNames() {

        File[] fa = new File(CSVTestCase.filePath).listFiles();

        List<String> result = new LinkedList<String>();
        for (File f : fa) {

            if (f.getName().contains(".csv")) {
                result.add(f.getName());
            }

        }

        return result;

    }

    public static void writeToResultFile(String s) throws IOException {

        bw.write(s);
        bw.newLine();
        bw.flush();
    }

    public static void init() throws IOException {

        CSVTestCase.bw = new BufferedWriter(new FileWriter("Result" + System.currentTimeMillis() + ".csv"));
        bw.write("file;time;mem");
        bw.newLine();
        bw.flush();
    }

    public void close() throws IOException {

        CSVTestCase.bw.close();
    }

    private void getNames() throws IOException {

        ImmutableList.Builder<String> builder = new ImmutableList.Builder<String>();

        if (this.hasHeader) {

            for (String s : this.nextLine.split(this.delimiter)) {
                builder.add(s);
            }
            this.nextLine = this.br.readLine();

        } else {

            for (int i = 0; i < this.numberOfColumns; i++) {

                builder.add(this.fileName + ":" + i);
            }
        }
        this.names = builder.build();

    }

    private void calcNumbers() {

        this.numberOfColumns = this.nextLine.split(this.delimiter).length;
    }

    @Override
    public ImmutableList<String> columnNames() {

        return this.names;
    }

    @Override
    public boolean hasNext() throws InputIterationException {

        return (this.nextLine != null);
    }

    @Override
    public ImmutableList<String> next() throws InputIterationException {

        if (this.hasNext()) {
            ImmutableList<String> result = this.getList(this.nextLine);
            try {
                this.nextLine = this.br.readLine();
            } catch (IOException e) {
                this.nextLine = null;
            }
            return result;
        } else {
            throw new InputIterationException("nix mehr da");
        }
    }

    private ImmutableList<String> getList(String nextLine2) {

        String[] splitted = this.nextLine.split(",");

        ImmutableList.Builder<String> builder = new ImmutableList.Builder<String>();

        for (String aSplitted : splitted) {

            String t = aSplitted;
            if (t == "") {
                t = null;
            } else {
                t = t.replaceAll("\"", "");
            }

            builder.add(t);
        }

        return builder.build();
    }

    @Override
    public int numberOfColumns() {

        return this.numberOfColumns;
    }

    @Override
    public String relationName() {

        return this.fileName;
    }

    @Override
    public RelationalInput generateNewCopy() throws InputGenerationException {

        return this;
    }

    @Override
    public void receiveResult(FunctionalDependency fd) throws CouldNotReceiveResultException {

        // System.out.println(fd.getDeterminant() + "-->" + fd.getDependant());

    }
}
