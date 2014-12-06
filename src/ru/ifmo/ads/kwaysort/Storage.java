package ru.ifmo.ads.kwaysort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Storage<E extends Comparable<E>> {
    protected final int mySize;
    protected final List<E> myStorage;

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
        for (int i = pos; i < pos + data.size(); ++i)
            myStorage.set(i, data.get(i - pos));
    }

    public void write(int pos, Storage<E> data) {
        write(pos, data.myStorage);
    }

    public void write(Storage<E> data) {
        write(0, data.myStorage);
    }

    public Storage<E> get(int pos, int len) {
        return new Storage<>(myStorage.subList(pos, Math.min(pos + len, size())));
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
        write(0, from.get(theirPos, size()));
    }

    public void readFrom(Storage<E> from) {
        readFrom(from, 0, 0, from.size());
    }

    public void writeTo(Storage<E> to, int theirPos, int myPos, int len) {
        to.write(theirPos, get(myPos, len));
    }

    public void writeTo(Storage<E> to, int theirPos) {
        to.write(theirPos, get(0, size()));
    }

    public void writeTo(Storage<E> to) {
        to.write(0, get(0, size()));
    }

    public int size() {
        return mySize;
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
}
