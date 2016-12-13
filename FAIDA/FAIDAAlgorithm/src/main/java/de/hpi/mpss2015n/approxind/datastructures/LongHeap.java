package de.hpi.mpss2015n.approxind.datastructures;

import java.util.Arrays;

/**
 * Binary heap implementation that keeps the greatest values on top.
 */
public class LongHeap {

    private long[] heap;

    private int size = 0;

    public LongHeap(int capacity) {
        this.heap = new long[capacity];
    }

    public int size() {
        return this.size;
    }

    public long peek() {
        if (this.size() == 0) throw new IllegalStateException();
        return this.heap[0];
    }

    public long poll() {
        if (this.size() == 0) throw new IllegalStateException();

        // Remove the top element.
        long top = this.heap[0];

        // Sink the last element.
        long e = this.heap[this.size--];
        int i = 0;
        while (this.size > 2 * i + 1) {
            int parent1 = 2 * i + 1;
            int parent2 = 2 * i + 2;
            long e1 = this.heap[parent1], e2 = -1;
            boolean isSinkLeft = this.size >= parent2 && e1 < (e2 = this.heap[parent2]);
            if (isSinkLeft) {
                if (e1 > e) {
                    this.heap[i] = e1;
                    i = parent1;
                } else break;
            } else {
                if (e2 > e) {
                    this.heap[i] = e2;
                    i = parent2;
                } else break;
            }
        }
        this.heap[i] = e;

        return top;
    }

    public void add(long e) {
        if (this.size >= this.heap.length) throw new IllegalArgumentException();

        // Swim the element from the back.
        int i = this.size++;
        while (i > 0 && this.heap[i / 2] < e) {
            this.heap[i] = this.heap[i /= 2];
        }
        this.heap[i] = e;
    }

    public int capacity() {
        return this.heap.length;
    }

    public long[] toArray() {
        return Arrays.copyOf(this.heap, this.size);
    }
}
