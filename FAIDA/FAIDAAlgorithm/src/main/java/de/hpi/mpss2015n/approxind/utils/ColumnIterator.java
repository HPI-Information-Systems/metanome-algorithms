package de.hpi.mpss2015n.approxind.utils;

import java.util.Iterator;

public interface ColumnIterator extends Iterator<long[]>, AutoCloseable {

    @Override
    void close(); // No throws declarations.

}