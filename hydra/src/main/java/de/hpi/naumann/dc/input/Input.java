package de.hpi.naumann.dc.input;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hpi.naumann.dc.helpers.IndexProvider;
import de.metanome.algorithm_integration.input.InputIterationException;
import de.metanome.algorithm_integration.input.RelationalInput;

public class Input {
	private final int lineCount;
	private final List<ParsedColumn<?>> parsedColumns;
	private final String name;

	public Input(RelationalInput relationalInput, int rowLimit) throws InputIterationException {
		final int columnCount = relationalInput.numberOfColumns();
		Column[] columns = new Column[columnCount];
		for (int i = 0; i < columnCount; ++i) {
			columns[i] = new Column(relationalInput.relationName(), relationalInput.columnNames().get(i));
		}

		int lineCount = 0;
		while (relationalInput.hasNext()) {
			List<String> line = relationalInput.next();
			for (int i = 0; i < columnCount; ++i) {
				columns[i].addLine(line.get(i));
			}
			++lineCount;
			if (rowLimit > 0 && lineCount >= rowLimit)
				break;
		}
		this.lineCount = lineCount;

		parsedColumns = new ArrayList<>(columns.length);
		createParsedColumns(relationalInput, columns);

		name = relationalInput.relationName();
	}

	private void createParsedColumns(RelationalInput relationalInput, Column[] columns) {
		int i = 0;
		for (Column c : columns) {
			switch (c.getType()) {
			case LONG: {
				ParsedColumn<Long> parsedColumn = new ParsedColumn<Long>(relationalInput.relationName(), c.getName(),
						Long.class, i);

				for (int l = 0; l < lineCount; ++l) {
					parsedColumn.addLine(c.getLong(l));
				}
				parsedColumns.add(parsedColumn);
			}
				break;
			case NUMERIC: {
				ParsedColumn<Double> parsedColumn = new ParsedColumn<Double>(relationalInput.relationName(),
						c.getName(), Double.class, i);

				for (int l = 0; l < lineCount; ++l) {
					parsedColumn.addLine(c.getDouble(l));
				}
				parsedColumns.add(parsedColumn);
			}
				break;
			case STRING: {
				ParsedColumn<String> parsedColumn = new ParsedColumn<String>(relationalInput.relationName(),
						c.getName(), String.class, i);

				for (int l = 0; l < lineCount; ++l) {
					parsedColumn.addLine(c.getString(l));
				}
				parsedColumns.add(parsedColumn);
			}
				break;
			default:
				break;
			}

			++i;
		}
	}

	public int getLineCount() {
		return lineCount;
	}

	public ParsedColumn<?>[] getColumns() {
		return parsedColumns.toArray(new ParsedColumn[0]);
	}

	public String getName() {
		return name;
	}

	public Input(RelationalInput relationalInput) throws InputIterationException {
		this(relationalInput, -1);
	}

	public int[][] getInts() {
		final int COLUMN_COUNT = parsedColumns.size();
		final int ROW_COUNT = getLineCount();

		long time = System.currentTimeMillis();
		int[][] input2s = new int[ROW_COUNT][COLUMN_COUNT];
		IndexProvider<String> providerS = new IndexProvider<>();
		IndexProvider<Long> providerL = new IndexProvider<>();
		IndexProvider<Double> providerD = new IndexProvider<>();
		for (int col = 0; col < COLUMN_COUNT; ++col) {

			if (parsedColumns.get(col).getType() == String.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerS.getIndex((String) parsedColumns.get(col).getValue(line)).intValue();
				}
			} else if (parsedColumns.get(col).getType() == Double.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerD.getIndex((Double) parsedColumns.get(col).getValue(line)).intValue();

				}
			} else if (parsedColumns.get(col).getType() == Long.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerL.getIndex((Long) parsedColumns.get(col).getValue(line)).intValue();
				}
			} else {
				log.error("Wrong type! " + parsedColumns.get(col).getValue(0).getClass().getName());
			}
		}
		providerS = IndexProvider.getSorted(providerS);
		providerL = IndexProvider.getSorted(providerL);
		providerD = IndexProvider.getSorted(providerD);
		for (int col = 0; col < COLUMN_COUNT; ++col) {
			if (parsedColumns.get(col).getType() == String.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerS.getIndex((String) parsedColumns.get(col).getValue(line)).intValue();
				}
			} else if (parsedColumns.get(col).getType() == Double.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerD.getIndex((Double) parsedColumns.get(col).getValue(line)).intValue();

				}
			} else if (parsedColumns.get(col).getType() == Long.class) {
				for (int line = 0; line < ROW_COUNT; ++line) {
					input2s[line][col] = providerL.getIndex((Long) parsedColumns.get(col).getValue(line)).intValue();
				}
			} else {
				log.error("Wrong type!");
			}
		}

		log.info("rebuild: " + (System.currentTimeMillis() - time));
		return input2s;
	}

	private static Logger log = LoggerFactory.getLogger(Input.class);

}
