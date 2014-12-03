package ru.ifmo.ads.kwaysort;

import java.util.Collections;
import java.util.List;

public class RamStorage<E extends Comparable<E>> extends Storage<E> {
    public RamStorage(int size) {
        super(size);
    }

    public RamStorage(List<E> data) {
        super(data);
    }

    public void sort() {
        Collections.sort(super.myStorage);
    }
}