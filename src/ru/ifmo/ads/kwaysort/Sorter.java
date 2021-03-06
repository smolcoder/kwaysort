package ru.ifmo.ads.kwaysort;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This is main class that implements k-way merge sort.
 * <p/>
 * First, it checks whether whole external data fit the RAM size.
 * If so, {@link RamStorage#sort()}  base sort} is called. Otherwise, external data
 * are splitted into {@link Sorter#calculateChunkCount(int, int) chunkCount} chunks and then there are
 * sorted by base RAM sort.
 * <p/>
 * Second, RAM is splitted into {@code chunkCount + 1} parts. {@code chunkCount} parts are 'associated'
 * with {@code chunkCount} chunks of external memory respectively (where these chunks are already sorted)
 * using {@link ru.ifmo.ads.kwaysort.Sorter.Chunk}. {@link ru.ifmo.ads.kwaysort.Sorter.Chunk} can load next
 * part of respected chunk as needed.
 * <p/>
 * Then all these parts are merged into last one. When last is full, it is flushed into outer memory storage.
 * <p/>
 * This implementation allows sorting via RAM if RAM size is about sqrt(external memory size) or greater.
 * Of course, we can split external memory into several parts, sort 'em with k-way merge sort, then
 * run this algorithm on these sorted parts. But in 'real live' it is enough just about ~2 Mb to sort ~300 Gb
 * basing on this limitation! So there is no need to introduce yet another level of sorting, because it is unlikely
 * we encounter worst case in practice (in that case we will have another great problems:) ).
 *
 * Minimal addressing unit (or loaded block) is array cell.
 *
 * @see ru.ifmo.ads.kwaysort.Sorter.Chunk
 */
public class Sorter<E extends Comparable<E>> {

    private boolean isDebugOutput;

    public void sort(ExternalStorage<E> inStorage, ExternalStorage<E> outStorage, RamStorage<E> ram) {
        int chunkCount = calculateChunkCount(ram.size(), inStorage.size());
        if (inStorage.isEmpty() || outStorage.isEmpty() || ram.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (ram.size() < chunkCount + 1) {
            throw new IllegalArgumentException("Ram size less than (chunk count + 1): " + ram.size() + " < " + (chunkCount + 1));
        }
        debugOutput("Chunk count is " + chunkCount);
        if (chunkCount == 1) {
            debugOutput("Call base sort and out.");
            baseSort(inStorage, outStorage, ram);
            return;
        }
        debugOutput("Call base sort on all chunks.");
        for (int i = 0; i < chunkCount; ++i) {
            ram.readFrom(inStorage, i * ram.size());
            ram.sort();
            ram.writeTo(inStorage, i * ram.size(), 0, Math.min(ram.size(), inStorage.size() - i * ram.size()));
        }
        mergeSort(inStorage, outStorage, ram, chunkCount);
    }

    private void mergeSort(ExternalStorage<E> inStorage, ExternalStorage<E> outStorage, RamStorage<E> ram, int chunkCount) {
        debugOutput("Merge sort: inStorage size = " + inStorage.size() + ", myRam size = " + ram.size());
        int ramChunkSize = ram.size() / (chunkCount + 1);
        debugOutput("Ram chunk size is " + ramChunkSize);
        int accumulateChunkLeftBound = chunkCount * ramChunkSize;
        int curAccumulatePos = accumulateChunkLeftBound;
        int accumulateLen = ram.size() - accumulateChunkLeftBound;
        int outPosToWrite = 0;
        List<Chunk> chunks = new ArrayList<>();
        for (int i = 0; i < chunkCount; i++) {
            chunks.add(new Chunk(i * ramChunkSize, (i + 1) * ramChunkSize,
                    i * ram.size(), Math.min(inStorage.size(), (i + 1) * ram.size()),
                    ram, inStorage));
        }
        for (Chunk c : chunks) {
            c.next();
            debugOutput(c);
        }
        for (int i = 0; i < inStorage.size(); ++i) {
            if (curAccumulatePos == ram.size()) {
                debugOutput("Flush accumulate chunk to out storage.");
                ram.writeTo(outStorage, outPosToWrite, accumulateChunkLeftBound, accumulateLen);
                outPosToWrite += accumulateLen;
                curAccumulatePos = accumulateChunkLeftBound;
            }
            Chunk chunkWithMinFirstElement = null;
            for (Chunk c : chunks) {
                if (!c.isValid()) continue;
                if (chunkWithMinFirstElement == null || c.peek().compareTo(chunkWithMinFirstElement.peek()) < 0) {
                    chunkWithMinFirstElement = c;
                }
            }
            if (chunkWithMinFirstElement == null) {
                debugOutput("Min chunk is null.");
                break;
            }
            E nextElement = chunkWithMinFirstElement.peek();
            debugOutput("Next element is " + nextElement + " in chunk " + chunkWithMinFirstElement);
            ram.set(curAccumulatePos++, chunkWithMinFirstElement.peek());
            chunkWithMinFirstElement.next();
        }
        if (curAccumulatePos > accumulateChunkLeftBound) {
            debugOutput("Flush rest of accumulate chunk to out storage.");
            ram.writeTo(outStorage, outPosToWrite, accumulateChunkLeftBound, curAccumulatePos - accumulateChunkLeftBound);
        }
        debugOutput("Writes count to out storage: " + outStorage.getBlockWritesCount());
    }

    public boolean isDebugOutput() {
        return isDebugOutput;
    }

    public void setDebugOutput(boolean isDebugOutput) {
        this.isDebugOutput = isDebugOutput;
    }

    private void debugOutput(Object s) {
        if (isDebugOutput()) {
            System.out.println(s);
        }
    }

    /**
     * This class manages data chunk from external memory via part of RAM.
     * It loads part of chunk into specified place in RAM, smbd works with it, then next part is loaded
     * and so on.
     */
    class Chunk {
        int storageLeftBound, storageRightBound;
        int leftInRam, rightInRam;
        int segmentSize;
        int curNotLoadedPosition;
        int curNotRedPositionInChunk;
        RamStorage<E> myRam;
        ExternalStorage<E> externalStorage;
        final String toString;
        boolean isValid = true;

        Chunk(int ramLeft, int ramRight, int storageLeft, int storageRight, RamStorage<E> ram, ExternalStorage<E> storage) {
            if (ramLeft >= ramRight) {
                throw new IllegalArgumentException(ramLeft + " >= " + ramRight);
            }
            if (storageLeft >= storageRight) {
                throw new IllegalArgumentException(storageLeft + " >= " + storageRight);
            }
            if (ramRight > ram.size()) {
                throw new IllegalArgumentException(ramRight + " >= " + ram.size());
            }

            if (storageRight > storage.size()) {
                throw new IllegalArgumentException(ramRight + " >= " + storage.size());
            }
            toString = "Chunk: ram[" + ramLeft + "," + ramRight + "], storage[" + storageLeft + "," + storageRight + "].";

            storageLeftBound = storageLeft;
            storageRightBound = storageRight;
            segmentSize = ramRight - ramLeft;
            leftInRam = ramLeft;
            rightInRam = ramRight;
            curNotRedPositionInChunk = ramLeft;
            curNotLoadedPosition = storageLeft;
            myRam = ram;
            externalStorage = storage;
            load();
        }

        /**
         * Load next part of chunk.
         */
        void load() {
            String s = "load: (" + curNotLoadedPosition + ", " + curNotRedPositionInChunk + ", " + rightInRam + ") ->";
            int positionsToRead = Math.min(segmentSize, storageRightBound - curNotLoadedPosition);
            myRam.readFrom(externalStorage, curNotLoadedPosition, leftInRam, positionsToRead);
            curNotLoadedPosition += positionsToRead;
            curNotRedPositionInChunk = leftInRam;
            rightInRam = leftInRam + positionsToRead;
            debugOutput(s + " (" + curNotLoadedPosition + ", " + curNotRedPositionInChunk + ", " + rightInRam + ")");
        }

        boolean hasNext() {
            return curNotLoadedPosition < storageRightBound || curNotRedPositionInChunk < rightInRam;
        }

        E next() {
            if (!hasNext()) {
                if (isValid) {
                    isValid = false;
                    return null;
                }
                throw new NoSuchElementException(this + " hasn't next element.");
            }
            if (curNotRedPositionInChunk == rightInRam) {
                load();
            }
            return myRam.get(curNotRedPositionInChunk++);
        }

        boolean isValid() {
            return isValid;
        }

        E peek() {
            return myRam.get(curNotRedPositionInChunk - 1);
        }

        @Override
        public String toString() {
            return toString;
        }
    }


    public static int calculateChunkCount(int ramSize, int storageSize) {
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
