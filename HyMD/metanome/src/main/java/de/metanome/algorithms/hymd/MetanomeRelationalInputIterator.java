package de.metanome.algorithms.hymd;

import static de.metanome.algorithms.hymd.MetanomeSchema.getSchema;

import de.hpi.is.md.relational.Column;
import de.hpi.is.md.relational.Row;
import de.hpi.is.md.relational.Schema;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class MetanomeRelationalInputIterator implements Iterator<Row> {

  private final RelationalInput input;
  private final Schema schema;

  private MetanomeRelationalInputIterator(RelationalInput input, Schema schema) {
    this.input = input;
    this.schema = schema;
  }

  static Iterator<Row> of(RelationalInput input) {
    Schema schema = getSchema(input);
    return new MetanomeRelationalInputIterator(input, schema);
  }

  @Override
  public boolean hasNext() {
    try {
      return input.hasNext();
    } catch (InputIterationException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Row next() {
    try {
      List<String> values = input.next();
      return toRow(values);
    } catch (InputIterationException e) {
      e.printStackTrace();
      return null;
    }
  }

  private Map<Column<?>, Object> toMap(Iterable<String> values) {
    List<Column<?>> columns = schema.getColumns();
//		return Seq.zip(columns, values)
//				.toMap(Tuple2::v1, Tuple2::v2);
    Map<Column<?>, Object> map = new HashMap<>();
    int i = 0;
    for (String value : values) {
      Column<?> column = columns.get(i++);
      map.put(column, value);
    }
    return map;
  }

  private Row toRow(Iterable<String> values) {
    Map<Column<?>, Object> map = toMap(values);
    return Row.create(schema, map);
  }
}
