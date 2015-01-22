package ru.ifmo.ads.kwaysort;

import java.util.List;

public class ExternalStorage<E extends Comparable<E>> extends Storage<E> {
    public ExternalStorage(int size) {
        super(size);
    }

    public ExternalStorage(List<E> data) {
        super(data);
    }

    int checkIsSorted() {
        E prev = myStorage.get(0);
        for (int i = 1; i < myStorage.size(); i++) {
            E e = myStorage.get(i);
            if (prev.compareTo(e) > 0) return i - 1;
            prev = e;
        }
        return -1;
    }
}