package ru.ifmo.ads.kwaysort;

import java.util.ArrayList;
import java.util.List;
/**
 * Represents basic storage (RAM, HDD, etc.) operation like read from, write to and so on.
 *
 * @see ru.ifmo.ads.kwaysort.RamStorage
 * @see ru.ifmo.ads.kwaysort.ExternalStorage
 * */
public class Storage<E extends Comparable<E>> {
    public static int BLOCK_SIZE = 32;

    protected final int mySize;
    protected final List<E> myStorage;

    private int myBlockWritesCount = 0;
    private int myBlockReadsCount = 0;

    private int mySeeksCount = 0;

    public Storage(int size) {
        mySize = size;
        myStorage = new ArrayList<>(size);
        clear();
    }

    public Storage(List<E> data) {
        mySize = data.size();
        myStorage = new ArrayList<>(data);
    }

    public void write(int pos, List<E> data) {
//        System.out.println(data.size() / BLOCK_SIZE);
        myBlockWritesCount += data.size() / BLOCK_SIZE;
        if (data.size() % BLOCK_SIZE > 0) myBlockWritesCount++;

        mySeeksCount++;

        for (int i = pos; i < pos + data.size(); ++i) {
            myStorage.set(i, data.get(i - pos));
        }
    }

    public void write(int pos, Storage<E> data) {
        write(pos, data.myStorage);
    }

    public Storage<E> get(int pos, int len) {
        myBlockReadsCount += len / BLOCK_SIZE;
        if (len % BLOCK_SIZE > 0) myBlockReadsCount++;

        mySeeksCount++;

        return new Storage<>(myStorage.subList(pos, pos + len));
    }

    public E get(int pos) {
        return myStorage.get(pos);
    }

    public void set(int pos, E value) {
        myStorage.set(pos, value);
    }

    public void readFrom(Storage<E> from, int theirPos, int myPos, int len) {
        write(myPos, from.get(theirPos, len));
    }

    public void readFrom(Storage<E> from, int theirPos) {
        write(0, from.get(theirPos, Math.min(size(), from.size() - theirPos)));
    }

    public void readFrom(Storage<E> from) {
        readFrom(from, 0, 0, from.size());
    }

    public void writeTo(Storage<E> to, int theirPos, int myPos, int len) {
        to.write(theirPos, get(myPos, len));
    }

    public void writeTo(Storage<E> to) {
        to.write(0, get(0, size()));
    }

    public int size() {
        return mySize;
    }

    public boolean isEmpty() {
        return mySize == 0;
    }

    public void clear() {
        myStorage.clear();
        for (int i = 0; i < mySize; i++) {
            myStorage.add(null);
        }
    }

    public String toString() {
        return myStorage.toString();
    }

    public int getBlockWritesCount() {
        return myBlockWritesCount;
    }

    public int getBlockReadsCount() {
        return myBlockReadsCount;
    }

    public int getSeeksCount() {
        return mySeeksCount;
    }
}
