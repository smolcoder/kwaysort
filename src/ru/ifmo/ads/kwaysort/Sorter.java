package ru.ifmo.ads.kwaysort;

public class Sorter<E extends Comparable<E>> {
    public void sort(ExternalStorage<E> inStorage, ExternalStorage<E> outStorage, RamStorage<E> ram) {
        int chunkCount = prefetchChunkCount(ram.size(), inStorage.size());
        if (chunkCount == 1) {
            baseSort(inStorage, outStorage, ram);
            return;
        }
        for (int i = 0; i < chunkCount; ++i) {
            ram.readFrom(inStorage, i * ram.size());
            ram.sort();
            ram.writeTo(inStorage, i * ram.size());
        }
        mergeSort(inStorage, outStorage, ram, chunkCount);
    }

    private void mergeSort(ExternalStorage<E> inStorage, ExternalStorage<E> outStorage, RamStorage<E> ram, int chunkCount) {

    }

    private class Chunk {
        int leftBound, rightBound;
        int segmentSize;
        int curNotRedPositionInChunk;
        RamStorage<E> ram;
        ExternalStorage<E> externalStorage;

        Chunk(int left, int right, int segSize, RamStorage<E> ram, ExternalStorage<E> es) {
            leftBound = left;
            rightBound = right;
            segmentSize = segSize;
            curNotRedPositionInChunk = 0;
            this.ram = ram;
            externalStorage = es;
        }

        void load(int l, int r) {
            ram.readFrom();
        }

        boolean hasNext() {
            return curNotRedPositionInChunk < rightBound;
        }

        E next() {
            if (curNotRedPositionInChunk)
        }

    }


    public static int prefetchChunkCount(int ramSize, int storageSize) {
        if (ramSize >= storageSize) {
            return 1;
        }
        if (storageSize % ramSize == 0) return storageSize / ramSize;
        return storageSize / ramSize + 1;
    }

    private void baseSort(ExternalStorage<E> inStorage, ExternalStorage<E> outStorage, RamStorage<E> ram) {
        ram.readFrom(inStorage);
        ram.sort();
        ram.writeTo(outStorage);
    }
}
