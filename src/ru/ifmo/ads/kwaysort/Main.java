package ru.ifmo.ads.kwaysort;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ExternalStorage<Integer> in = new ExternalStorage<>(
                Arrays.asList(15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0));
        ExternalStorage<Integer> out = new ExternalStorage<>(16);
        RamStorage<Integer> ram = new RamStorage<Integer>(8);
        Sorter<Integer> sorter = new Sorter<>();
        sorter.setDebugOutput(true);
        sorter.sort(in, out, ram);
        System.out.println(out.isSorted());
    }
}
