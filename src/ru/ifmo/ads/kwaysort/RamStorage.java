package ru.ifmo.ads.kwaysort;

import java.util.Collections;
import java.util.List;

/**
 * Represents RAM that is used in k-way merge sort algorithm.
 *
 * @see ru.ifmo.ads.kwaysort.ExternalStorage
 */
public class RamStorage<E extends Comparable<E>> extends Storage<E> {
    public RamStorage(int size) {
        super(size);
    }

    /**
     * This method is used for base sorting of the whole chunk.
     * See first step of k-way sort algorithm.
     */
    public void sort() {
        Collections.sort(super.myStorage);
    }
}
