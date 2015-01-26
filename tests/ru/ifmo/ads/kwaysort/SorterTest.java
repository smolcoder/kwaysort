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
        doRandomTest(10000, 200, false);
        doRandomTest(100000, 1000, false);
        for (int i = 100000; i < 110000; i += 1000) {
            int inSize = i;
            if (i % 2000 == 0) {
                inSize += RANDOM.nextInt(500);
            }
            int ramSize = (1 + RANDOM.nextInt(4)) * ((int)Math.sqrt(inSize) + 100);
            doRandomTest(inSize, ramSize, false);
        }
    }

    @Test
    public void testBuildStatistic() throws Exception {
        SORTER.setDebugOutput(false);
        Storage.BLOCK_SIZE = 128;

        System.out.println(String.format("%3s\t%6s\t%6s\t%5s\t%6s\t%6s",
                "|in|", "|ram|", "block", "reads", "writes", "seeks"));


        for (int i = 20000; i <= 140000; i += 40000) {
            for (int b = 32; b <= 256; b *= 2) {
                Storage.BLOCK_SIZE = b;
                for (int j = 500; j <= i; j *= 2) {

                    doRandomTest(i, j, true);
                }
                System.out.println("--------------------------------------------");
            }
        }
    }

    private void doRandomTest(int inSize, int ramSize, boolean printStatistics) {
        ExternalStorage<Integer> in = new ExternalStorage<>(generateRandomList(inSize));
        ExternalStorage<Integer> out = new ExternalStorage<>(inSize);
        RamStorage<Integer> ram = new RamStorage<>(ramSize);
        SORTER.sort(in, out, ram);
        assertIsSorted(out);
        if (printStatistics) {
            System.out.println(String.format("%6d\t%6d\t%6d\t%5d\t%6d\t%6d", inSize, ramSize, Storage.BLOCK_SIZE,
                    (in.getBlockWritesCount() + out.getBlockWritesCount()),
                    (in.getBlockReadsCount() + out.getBlockReadsCount()), (in.getSeeksCount() + out.getSeeksCount())));
        }
    }

    private void assertIsSorted(ExternalStorage<Integer> out) {
        int failPos = out.checkIsSorted();
        if (failPos == -1) return;
        assertTrue("at pos " + failPos + ": " + out.get(failPos) + " > " + out.get(failPos + 1) + " in " + out, false);
    }
}