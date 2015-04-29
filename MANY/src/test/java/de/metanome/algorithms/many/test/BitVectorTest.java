package de.metanome.algorithms.many.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.metanome.algorithms.many.bitvectors.BitVector;
import de.metanome.algorithms.many.bitvectors.LongArrayBitVector;

public class BitVectorTest {

    @Test
    public void testBitVector() {
        BitVector<LongArrayBitVector> testVector = new LongArrayBitVector(33);
        assertEquals(33, testVector.size());
        testVector.set(5);
        for (int i = 0; i < testVector.size(); i++) {
            if (i == 5) {
                assertTrue(testVector.get(i));
                continue;
            }
            assertFalse(testVector.get(i));
        }
        testVector.flip();
        for (int i = 0; i < testVector.size(); i++) {
            if (i == 5) {
                assertFalse(testVector.get(i));
                continue;
            }
            assertTrue(testVector.get(i));
        }
    }

    @Test
    public void testBitVectorCopy() {
        BitVector<LongArrayBitVector> testVector1 = new LongArrayBitVector(8);
        testVector1.set(0);
        testVector1.set(1);

        BitVector<LongArrayBitVector> testVector2 = testVector1.copy();

        assertEquals(testVector1, testVector2);
        assertEquals(testVector1.size(), testVector2.size());
        assertTrue(testVector2.get(0));
        assertTrue(testVector2.get(1));
    }

    @Test
    public void testBitVectorAnd() {
        BitVector<LongArrayBitVector> testVector1 = new LongArrayBitVector(8);
        BitVector<LongArrayBitVector> testVector2 = new LongArrayBitVector(8);
        testVector1.set(0);
        testVector1.set(1);
        testVector2.set(1);
        testVector2.set(2);
        testVector1.and(testVector2);

        assertFalse(testVector1.get(0));
        assertFalse(testVector1.get(2));
        assertTrue(testVector1.get(1));
    }

    @Test
    public void testBitVectorAnd2() {
        BitVector<LongArrayBitVector> testVector1 = new LongArrayBitVector(8).flip();
        BitVector<LongArrayBitVector> testVector2 = new LongArrayBitVector(8);
        testVector1.and(testVector2);

        for (int i = 0; i < testVector1.size(); i++) {
            assertFalse(testVector1.get(i));
        }
    }

    @Test
    public void testBitVectorOr() {
        BitVector<LongArrayBitVector> testVector1 = new LongArrayBitVector(8);
        BitVector<LongArrayBitVector> testVector2 = new LongArrayBitVector(8);
        testVector2.set(1);
        testVector2.set(2);
        testVector1.or(testVector2);

        assertTrue(testVector1.get(2));
        assertTrue(testVector1.get(1));
    }

    @Test
    public void testBitVectorNext() {
        BitVector<LongArrayBitVector> testVector1 = new LongArrayBitVector(12345678);
        int[] pos = { 0, 1, 63, 64, 65, 127, 128, 129, 1023, 1024, 1025, 4095, 4096, 5000, 8000, 12345, 40959, 40960,
                40961,
                1234566, 12345677 };
        for (int p : pos) {
            testVector1.set(p);
        }
        int iteration = 0;
        int lastPos = -1;
        do {
            lastPos = testVector1.next(lastPos);
            assertEquals(pos[iteration++], lastPos);
        } while (lastPos != -1 && iteration < pos.length);
    }

    @Test
    public void testBitVectorNextSmallBitVector() {
        BitVector<LongArrayBitVector> testVector1 = new LongArrayBitVector(8);
        BitVector<LongArrayBitVector> testVector2 = new LongArrayBitVector(20);
        testVector2.flip();
        testVector1.flip();
        testVector1.clear(7);
        int[] pos = { 0, 1, 2, 3, 4, 5, 6 };
        for (int p : pos) {
            testVector1.set(p);
        }
        testVector2.and(testVector1);
        int iteration = 0;
        int lastPos = -1;
        do {
            lastPos = testVector2.next(lastPos);
            assertEquals(pos[iteration++], lastPos);
        } while (lastPos != -1 && iteration < pos.length);
        assertEquals(-1, testVector1.next(6));
    }

    @Test
    public void testBitVectorCount() {
        BitVector<LongArrayBitVector> testVector1 = new LongArrayBitVector(10000);
        int[] pos = { 0, 1, 2, 3, 4, 5, 6, 8403, 1030, 8888, 9999 };
        for (int p : pos) {
            testVector1.set(p);
        }

        assertEquals(pos.length, testVector1.count());
    }

    @Test
    public void testBitVectorClearedAfterCreation() {
        BitVector<LongArrayBitVector> testVector = new LongArrayBitVector(33);
        for (int i = 0; i < testVector.size(); i++) {
            assertFalse(testVector.get(i));
        }
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testBitVectorBoundaries1() {
        BitVector<LongArrayBitVector> testVector = new LongArrayBitVector(33);
        testVector.get(34);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testBitVectorBoundaries2() {
        BitVector<LongArrayBitVector> testVector = new LongArrayBitVector(33);
        testVector.set(34);
    }
}
