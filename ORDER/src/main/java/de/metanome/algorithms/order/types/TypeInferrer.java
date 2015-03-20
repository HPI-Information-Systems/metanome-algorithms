package de.metanome.algorithms.order.types;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.google.common.collect.Ordering;

import de.metanome.algorithm_integration.input.InputGenerationException;
import de.metanome.algorithm_integration.input.RelationalInput;
import de.metanome.algorithm_integration.input.RelationalInputGenerator;
import de.metanome.algorithms.order.types.Datatype.type;

public class TypeInferrer {
  RelationalInputGenerator inputGenerator;
  final Ordering<Datatype.type> typeOrder = Ordering.explicit(Datatype.type.LONG,
      Datatype.type.DOUBLE, Datatype.type.DATE, Datatype.type.STRING);
  // TODO: support other date formats
  public static final String[] dateFormats = {"yyyy-MM-dd"};
  List<Datatype> types;

  public TypeInferrer(final RelationalInputGenerator inputGenerator) {
    this.inputGenerator = inputGenerator;
    this.types = new ArrayList<>();
  }

  public List<Datatype> inferTypes() throws InputGenerationException {
    if (!this.types.isEmpty()) {
      return this.types;
    }

    try (final RelationalInput input = this.inputGenerator.generateNewCopy()) {
      final int[] columnIndices = new int[input.numberOfColumns()];
      for (int i = 0; i < columnIndices.length; i++) {
        columnIndices[i] = i;
        // initialize types with most specific type long
        this.types.add(new DatatypeLong(null));
      }

      while (input.hasNext()) {
        final List<String> currentRow = input.next();
        for (final int column : columnIndices) {
          if (this.types.get(column).specificType == type.STRING) {
            continue;
          }

          this.types.set(column, this.refreshDatatype(column, currentRow.get(column)));
        }
      }
    } catch (final Exception e) {
      throw new InputGenerationException("Error while inferring types.", e);
    }

    return this.types;
  }

  private Datatype refreshDatatype(final int column, final String value) {
    Datatype datatype = this.types.get(column);

    final Datatype.type oldType = datatype.getSpecificType();
    final Datatype.type newType = this.determineDataType(value);

    if (this.typeOrder.compare(newType, oldType) > 0) {
      switch (newType) {
        case DATE:
          datatype = new DatatypeDate(null);
          break;
        case DOUBLE:
          datatype = new DatatypeDouble(null);
          break;
        case LONG:
          datatype = new DatatypeLong(null);
          break;
        case STRING:
          datatype = new DatatypeString(null);
          break;
        default:
          datatype = new DatatypeString(null);
          break;

      }
    }

    return datatype;
  }

  private type determineDataType(final String value) {

    if (value == null) {
      // null is o.k. with any value, return most specific type
      return type.LONG;
    }

    if (this.isLong(value)) {
      return type.LONG;
    } else if (this.isDouble(value)) {
      return type.DOUBLE;
    } else if (this.isDate(value)) {
      return type.DATE;
    }

    return type.STRING;
  }

  private boolean isDate(final String value) {
    try {
      DateUtils.parseDate(value, TypeInferrer.dateFormats);
    } catch (final ParseException e) {
      // none of the date format patterns in dateFormats could be matched
      return false;
    }
    return true;
  }

  private boolean isDouble(final String value) {
    try {
      Double.parseDouble(value);
    } catch (final NumberFormatException e) {
      return false;
    }
    return true;
  }

  private boolean isLong(final String value) {
    try {
      Long.parseLong(value);
    } catch (final NumberFormatException e) {
      return false;
    }
    return true;
  }

}
