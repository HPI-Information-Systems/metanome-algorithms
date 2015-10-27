package de.hpi.mpss2015n.approxind.utils;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Iterator;

import com.google.common.base.Verify;

public final class ColumnIterator implements Iterator<long[]>, Closeable {

    private final FileInputStream[] in;
    private final FileChannel[] channel;
    private final ByteBuffer[] bb;
    private final long[] data;
    private boolean hasMore = true;

    ColumnIterator(FileInputStream[] in) {
        this.in = in;
        channel = new FileChannel[in.length];
        bb = new ByteBuffer[in.length];
        //ipArr = new long[in.length][];
        for (int i = 0; i < in.length; i++) {
            channel[i] = in[i].getChannel();
            bb[i] = ByteBuffer.allocateDirect(ColumnStore.BUFFERSIZE);
        }
        readNext();
        this.data = new long[in.length];
    }

    @Override
    public boolean hasNext() {
        return bb[0].remaining() > 0 || hasMore && readNext();
    }

    @Override
    public long[] next() {
        for (int i = 0; i < in.length; i++) {
            data[i] = bb[i].getLong();
        }
        return data;
    }

    private boolean readNext() {
        Verify.verify(hasMore, "invalid call");
        try {
            for (int i = 0; i < in.length; i++) {
                int len = 0;
                bb[i].clear();
                hasMore &= ((len = channel[i].read(bb[i])) != -1);
                bb[i].flip();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hasMore;
    }

    @Override
    public void close() {
        try {
            for (FileInputStream input : in) {
                input.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}