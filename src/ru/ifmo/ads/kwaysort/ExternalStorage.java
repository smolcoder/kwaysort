package ru.ifmo.ads.kwaysort;

import java.util.List;

public class ExternalStorage<E extends Comparable<E>> extends Storage<E> {
    public ExternalStorage(int size) {
        super(size);
    }

    public ExternalStorage(List<E> data) {
        super(data);
    }
}