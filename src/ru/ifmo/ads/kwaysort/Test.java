package ru.ifmo.ads.kwaysort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by Arbuzov Ivan.
 */
public class Test {
    public static void main(String[] args) {
        ExternalStorage<Integer> in = new ExternalStorage<>(
                Arrays.asList(8, 9, 10, 11, 12, 13, 14, 15, 0, 1, 2, 3, 4, 5, 6, 7));
        RamStorage<Integer> ram = new RamStorage<Integer>(9);
        List<Chunk> chunks = new ArrayList<>();
//        chunks.add(new Chunk(0, 3, 0, 8, ram, in));
        chunks.add(new Chunk(3, 6, 3, 4, ram, in));

        int i = 0;
        while(chunks.get(i).hasNext()) {
            System.out.println(chunks.get(i).next());
        }

    }

    static class Chunk {
        int leftBound, rightBound;
        int rLeft, rRight;
        int segmentSize;
        int curNotLoadedPosition;
        int curNotRedPositionInChunk;
        RamStorage<Integer> ram;
        ExternalStorage<Integer> externalStorage;
        final String toString;

        Chunk(int ramLeft, int ramRight,  int ESleft, int ESright, RamStorage<Integer> ram, ExternalStorage<Integer> es) {
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
            System.out.print("load: (" + curNotLoadedPosition + ", " + curNotRedPositionInChunk + ", " + rRight + ") ->");
            int positionsToRead = Math.min(segmentSize, rightBound- curNotLoadedPosition);
            ram.readFrom(externalStorage, curNotLoadedPosition, rLeft, positionsToRead);
            curNotLoadedPosition += positionsToRead;
            curNotRedPositionInChunk = rLeft;
            rRight = rLeft + positionsToRead;
            System.out.println(" (" + curNotLoadedPosition + ", " + curNotRedPositionInChunk + ", " + rRight + ")");
        }

        boolean hasNext() {
            return curNotLoadedPosition < rightBound || curNotRedPositionInChunk < rRight;
        }

        Integer next() {
            if (!hasNext())
                throw new NoSuchElementException(this + " hasn't next element.");
            if (curNotRedPositionInChunk == rRight) {
                load();
            }
            return ram.get(curNotRedPositionInChunk++);
        }

        @Override
        public String toString() {
            return toString;
        }
    }
}
