package gc_less.python_like;

import gc_less.no_unsafe.NativeMem;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class IntHashtableOffHeap {
  private MemorySegment storage;
  private final long intSize = 4;
  private int capacity;
  private final float loadFactor;
  private int size;
  private float sizeMaxLoad;

  public IntHashtableOffHeap(int initialCapacity, float loadFactor) {
    capacity = initialCapacity;
    this.loadFactor = loadFactor;
    sizeMaxLoad = capacity * loadFactor;
    storage = NativeMem.malloc(capacity * intSize * 2);
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

    for (int bucketIdx = bucket * 2, i = 0; /*i<capacity*/ ; i++, bucketIdx += 2) {
      if (bucketIdx >= storage.byteSize() / intSize) {
        bucketIdx = 0;
      }
      int existingKey = storage.get(ValueLayout.JAVA_INT, bucketIdx * intSize);
      if (existingKey == 0) { // absent
        storage.set(ValueLayout.JAVA_INT, (bucketIdx) * intSize, key);
        storage.set(ValueLayout.JAVA_INT, (bucketIdx + 1) * intSize, value);
        break;
      } else if (existingKey == key) {
        prevVal = storage.get(ValueLayout.JAVA_INT, (bucketIdx + 1) * intSize);
        storage.set(ValueLayout.JAVA_INT, (bucketIdx + 1) * intSize, value); // overwrite
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

    for (int bucketIdx = bucket * 2, i = 0; /*i<capacity*/ ; i++, bucketIdx += 2) {
      if (bucketIdx >= storage.byteSize() / intSize) {
        bucketIdx = 0;
      }
      int existingKey = storage.get(ValueLayout.JAVA_INT, bucketIdx * intSize);
      if (existingKey == 0) { // absent
        return 0; // absent
      } else if (existingKey == key) {
        size--;
        storage.set(ValueLayout.JAVA_INT, (bucketIdx) * intSize, 0); // clear
        return storage.get(ValueLayout.JAVA_INT, (bucketIdx + 1) * intSize);
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
      storage = NativeMem.malloc(capacity * intSize * 2);
      storage.fill((byte) 0);
      for (int bucketIdx = 0; bucketIdx < oldStorage.byteSize() / intSize; bucketIdx += 2) {
        int key = oldStorage.get(ValueLayout.JAVA_INT, bucketIdx * intSize);
        if (key != 0) {
          insert(key, oldStorage.get(ValueLayout.JAVA_INT, (bucketIdx + 1) * intSize));
        }
      }
      NativeMem.free(oldStorage);
    }
  }

  public int get(int key) {
    int bucket = key % capacity;
    // we utilize the fact that for int : hash = value

    for (int bucketIdx = bucket * 2, i = 0; /*i<capacity*/ ; i++, bucketIdx += 2) {
      if (bucketIdx >= storage.byteSize() / intSize) {
        bucketIdx = 0;
      }
      int existingKey = storage.get(ValueLayout.JAVA_INT, bucketIdx * intSize);
      if (existingKey == 0) { // absent
        return 0; // not found
      } else if (existingKey == key) {
        return storage.get(ValueLayout.JAVA_INT, (bucketIdx + 1) * intSize);
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
