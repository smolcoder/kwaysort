package ru.ifmo.ads.kwaysort;

import java.util.List;

public class ExternalStorage<E extends Comparable<E>> extends Storage<E> {
    public ExternalStorage(int size) {
        super(size);
    }

    public ExternalStorage(List<E> data) {
        super(data);
    }

    boolean isSorted() {
        E prev = null;
        for (E e : myStorage) {
            if (e == null) return false;
            if (prev == null) {
                prev = e;
            } else {
                if (prev.compareTo(e) > 0) return false;
            }
        }
        return true;
    }
}