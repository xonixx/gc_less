package gc_less.python_like;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import gc_less.no_unsafe.NativeMem;
import java.lang.foreign.MemorySegment;

public class IntHashtableOffHeap {
  private MemorySegment storage;
  private final int intSize = 4;
  private int capacity;
  private final float loadFactor;
  private int size;
  private float sizeMaxLoad;

  public IntHashtableOffHeap(int initialCapacity, float loadFactor) {
    capacity = initialCapacity;
    this.loadFactor = loadFactor;
    sizeMaxLoad = capacity * loadFactor;
    storage = NativeMem.malloc((long) capacity * intSize * 2);
    storage.fill((byte) 0);
  }

  /**
   * @return prev value for key
   */
  public int put(int key, int value) {
    int prevVal = insert(key, value);

    if (prevVal == 0) {
      size++;
    }

    resizeIfNeeded();

    return prevVal;
  }

  private int insert(int key, int value) {
    // we utilize the fact that for int : hash = value
    int bucket = key % capacity;

    int prevVal = 0;

    int storageLen = (int) storage.byteSize();
    int delta = 2 * intSize;

    for (int bucketIdxOffset = bucket * delta, i = 0; /*i<capacity*/ ; i++, bucketIdxOffset += delta) {
      if (bucketIdxOffset >= storageLen) {
        bucketIdxOffset = 0;
      }
      int existingKey = storage.get(JAVA_INT, bucketIdxOffset);
      if (existingKey == 0) { // absent
        storage.set(JAVA_INT, bucketIdxOffset, key);
        storage.set(JAVA_INT, bucketIdxOffset + intSize, value);
        break;
      } else if (existingKey == key) {
        long valOffset = bucketIdxOffset + intSize;
        prevVal = storage.get(JAVA_INT, valOffset);
        storage.set(JAVA_INT, valOffset, value); // overwrite
        break;
      }
    }
    return prevVal;
  }

  /**
   * @return prev value for key
   */
  public int remove(int key) {
    // we utilize the fact that for int : hash = value
    int bucket = key % capacity;

    int storageLen = (int) storage.byteSize();
    int delta = 2 * intSize;

    for (int bucketIdxOffset = bucket * delta, i = 0; /*i<capacity*/ ; i++, bucketIdxOffset += delta) {
      if (bucketIdxOffset >= storageLen) {
        bucketIdxOffset = 0;
      }
      int existingKey = storage.get(JAVA_INT, bucketIdxOffset);
      if (existingKey == 0) { // absent
        return 0; // absent
      } else if (existingKey == key) {
        size--;
        storage.set(JAVA_INT, bucketIdxOffset, 0); // clear
        return storage.get(JAVA_INT, bucketIdxOffset + intSize);
      }
    }

    //    return 0; // not found
  }

  private void resizeIfNeeded() {
    if (size > sizeMaxLoad) {
      //      int oldCapacity = capacity;
      capacity *= 2;
      //      System.out.println("resizing " + oldCapacity + " -> " + capacity + "...");
      sizeMaxLoad = capacity * loadFactor;

      MemorySegment oldStorage = storage;
      //      storage = new int[capacity * 2];
      storage = NativeMem.malloc((long) capacity * intSize * 2);
      storage.fill((byte) 0);

      int storageLen = (int) oldStorage.byteSize();
      int delta = 2 * intSize;

      for (int bucketIdxOffset = 0; bucketIdxOffset < storageLen; bucketIdxOffset += delta) {
        int key = oldStorage.get(JAVA_INT, bucketIdxOffset);
        if (key != 0) {
          insert(key, oldStorage.get(JAVA_INT, bucketIdxOffset + intSize));
        }
      }
      NativeMem.free(oldStorage);
    }
  }

  public int get(int key) {
    int bucket = key % capacity;
    // we utilize the fact that for int : hash = value

    int storageLen = (int) storage.byteSize();
    int delta = 2 * intSize;

    for (int bucketIdxOffset = bucket * delta, i = 0; /*i<capacity*/ ; i++, bucketIdxOffset += delta) {
      if (bucketIdxOffset >= storageLen) {
        bucketIdxOffset = 0;
      }
      int existingKey = storage.get(JAVA_INT, bucketIdxOffset);
      if (existingKey == 0) { // absent
        return 0; // not found
      } else if (existingKey == key) {
        return storage.get(JAVA_INT, bucketIdxOffset + intSize);
      }
    }

    //    return 0; // absent
  }

  public void clear() {
    size = 0;
    storage.fill((byte) 0);
  }

  public void free() {
    NativeMem.free(storage);
  }

  public int size() {
    return size;
  }
}
