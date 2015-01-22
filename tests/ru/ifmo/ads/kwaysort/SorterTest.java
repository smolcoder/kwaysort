package ru.ifmo.ads.kwaysort;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class SorterTest {
    public static final long SEED = 10000000007L;
    private static final Random RANDOM = new Random(SEED);
    private static final Sorter<Integer> SORTER = new Sorter<>();

    @Before
    public void setUp() {
        SORTER.setDebugOutput(true);
    }

    public static List<Integer> generateRandomList(int size) {
        List<Integer> res = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            res.add(RANDOM.nextInt());
        }
        return res;
    }

    @Test
    public void testDescending() throws Exception {
        ExternalStorage<Integer> in = new ExternalStorage<>(Arrays.asList(15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0));
        ExternalStorage<Integer> out = new ExternalStorage<>(16);
        RamStorage<Integer> ram = new RamStorage<>(8);
        SORTER.sort(in, out, ram);
        assertIsSorted(out);
    }

    @Test
    public void testAscending() throws Exception {
        ExternalStorage<Integer> in = new ExternalStorage<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14));
        ExternalStorage<Integer> out = new ExternalStorage<>(15);
        RamStorage<Integer> ram = new RamStorage<>(5);
        SORTER.sort(in, out, ram);
        assertIsSorted(out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmpty() throws Exception {
        ExternalStorage<Integer> in = new ExternalStorage<>(Collections.<Integer>emptyList());
        ExternalStorage<Integer> out = new ExternalStorage<>(16);
        RamStorage<Integer> ram = new RamStorage<>(8);
        SORTER.sort(in, out, ram);
        assertIsSorted(out);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTooLittleRam1() throws Exception {
        ExternalStorage<Integer> in = new ExternalStorage<>(100);
        ExternalStorage<Integer> out = new ExternalStorage<>(100);
        RamStorage<Integer> ram = new RamStorage<>(10);
        SORTER.sort(in, out, ram);
        assertIsSorted(out);
    }

    @Test
    public void testLittleRam() throws Exception {
        ExternalStorage<Integer> in = new ExternalStorage<>(generateRandomList(100));
        ExternalStorage<Integer> out = new ExternalStorage<>(100);
        RamStorage<Integer> ram = new RamStorage<>(11);
        SORTER.sort(in, out, ram);
        assertIsSorted(out);
    }

    @Test
    public void testRandom() throws Exception {
        SORTER.setDebugOutput(false);
        doRandomTest(10000, 200);
        doRandomTest(100000, 1000);
        for (int i = 1000; i < 110000; i += 1000) {
            int inSize = i;
            if (i % 2000 == 0) {
                inSize += RANDOM.nextInt(500);
            }
            int ramSize = (1 + RANDOM.nextInt(4)) * ((int)Math.sqrt(inSize) + 100);
            doRandomTest(inSize, ramSize);
        }
    }

    private void doRandomTest(int inSize, int ramSize) {
        ExternalStorage<Integer> in = new ExternalStorage<>(generateRandomList(inSize));
        ExternalStorage<Integer> out = new ExternalStorage<>(inSize);
        RamStorage<Integer> ram = new RamStorage<>(ramSize);
        SORTER.sort(in, out, ram);
        assertIsSorted(out);
    }

    private void assertIsSorted(ExternalStorage<Integer> out) {
        int failPos = out.checkIsSorted();
        if (failPos == -1) return;
        assertTrue("at pos " + failPos + ": " + out.get(failPos) + " > " + out.get(failPos + 1) + " in " + out, false);
    }
}