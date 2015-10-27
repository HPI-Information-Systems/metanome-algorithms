package de.hpi.mpss2015n.approxind.mocks;

import com.google.common.base.Verify;

import de.metanome.algorithm_integration.input.RelationalInputGenerator;

import java.util.ArrayList;
import java.util.List;

public class RelationalInputBuilder {
    private final String name;
    private String[] header;
    private List<String[]> data;

    public RelationalInputBuilder(String name) {
        this.name = name;
        this.data = new ArrayList<>();
    }

    public RelationalInputBuilder setHeader(String... header) {
        this.header = header;
        return this;
    }

    public RelationalInputBuilder addRow(String... row) {
        Verify.verifyNotNull(header, "set header first!");
        Verify.verify(row.length == header.length);
        data.add(row);
        return this;
    }

    public RelationalInputGenerator build() {
        RelationalInputMock input = new RelationalInputMock(name, header, data.toArray(new String[0][0]));
        return new RelationalInputGeneratorMock(input);
    }

}