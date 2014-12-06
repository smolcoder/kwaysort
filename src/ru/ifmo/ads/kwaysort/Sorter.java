package ru.ifmo.ads.kwaysort;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Sorter<E extends Comparable<E>> {
    public void sort(ExternalStorage<E> inStorage, ExternalStorage<E> outStorage, RamStorage<E> ram) {
        int chunkCount = prefetchChunkCount(ram.size(), inStorage.size());
        if (ram.size() < chunkCount + 1)
            throw new IllegalArgumentException(ram.size() + " < " + (chunkCount + 1));
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
        int ramChunkSize = ram.size() / (chunkCount + 1);
        int cacheLeft = chunkCount * ramChunkSize;
        int cacheRight = ram.size();
        int curCachePos = cacheLeft;
        int outPosToWrite = 0;
        List<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkCount; i++) {
            chunks.add(new Chunk(
                    i * ramChunkSize, (i + 1) * ramChunkSize,
                    i * ram.size(), Math.min(inStorage.size(), (i + 1) * ram.size()),
                    ram, inStorage));
        }
        for (Chunk c : chunks) {
            System.out.println(c);
            c.next();
        }
        for (int i = 0; i < inStorage.size(); ++i) {
            Chunk minC = null;
            for (Chunk c : chunks) {
                if (!c.isValid()) continue;
                if (minC == null || c.peek().compareTo(minC.peek()) < 0) {
                    minC = c;
                }
            }
            if (minC == null) break;
            if (curCachePos == cacheRight) {
                ram.writeTo(outStorage, outPosToWrite, cacheLeft, curCachePos - cacheLeft);
                outPosToWrite += curCachePos - cacheLeft;
                curCachePos = cacheLeft;
            }
            ram.set(curCachePos++, minC.peek());
            minC.next();
        }
        if (curCachePos > cacheLeft)
            ram.writeTo(outStorage, outPosToWrite, cacheLeft, curCachePos - cacheLeft);
    }

    class Chunk {
        int leftBound, rightBound;
        int rLeft, rRight;
        int segmentSize;
        int curNotLoadedPosition;
        int curNotRedPositionInChunk;
        RamStorage<E> ram;
        ExternalStorage<E> externalStorage;
        final String toString;
        boolean isValid = true;

        Chunk(int ramLeft, int ramRight,  int ESleft, int ESright, RamStorage<E> ram, ExternalStorage<E> es) {
            if (ramLeft >= ramRight)
                throw new IllegalArgumentException(ramLeft + " >= " + ramRight);
            if (ESleft >= ESright)
                throw new IllegalArgumentException(ESleft + " >= " + ESright);
            if (ramRight > ram.size())
                throw new IllegalArgumentException(ramRight + " >= " + ram.size());
            if (ESright > es.size())
                throw new IllegalArgumentException(ramRight + " >= " + es.size());
            toString = "Chunk: ram[" + ramLeft + "," + ramRight + "], es[" + ESleft + "," + ESright + "].";

            leftBound = ESleft;
            rightBound = ESright;
            segmentSize = ramRight - ramLeft;
            rLeft = ramLeft;
            rRight = ramRight;
            curNotRedPositionInChunk = ramLeft;
            curNotLoadedPosition = ESleft;
            this.ram = ram;
            externalStorage = es;
            load();
        }

        void load() {
//            System.out.print("load: (" + curNotLoadedPosition + ", " + curNotRedPositionInChunk + ", " + rRight + ") ->");
            int positionsToRead = Math.min(segmentSize, rightBound- curNotLoadedPosition);
            ram.readFrom(externalStorage, curNotLoadedPosition, rLeft, positionsToRead);
            curNotLoadedPosition += positionsToRead;
            curNotRedPositionInChunk = rLeft;
            rRight = rLeft + positionsToRead;
//            System.out.println(" (" + curNotLoadedPosition + ", " + curNotRedPositionInChunk + ", " + rRight + ")");
        }

        boolean hasNext() {
            return curNotLoadedPosition < rightBound || curNotRedPositionInChunk < rRight;
        }

        E next() {
            if (!hasNext()) {
                if (isValid) {
                    isValid = false;
                    return null;
                }
                throw new NoSuchElementException(this + " hasn't next element.");
            }
            if (curNotRedPositionInChunk == rRight) {
                load();
            }
            return ram.get(curNotRedPositionInChunk++);
        }

        boolean isValid() {
            return isValid;
        }

        E peek() {
            return ram.get(curNotRedPositionInChunk - 1);
        }

        @Override
        public String toString() {
            return toString;
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
