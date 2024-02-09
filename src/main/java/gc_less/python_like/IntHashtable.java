package gc_less.python_like;

import java.util.Arrays;

public class IntHashtable {
  private int[] storage;

  private int capacity;
  private final float loadFactor;
  private int size;
  private float sizeMaxLoad;

  public IntHashtable(int initialCapacity, float loadFactor) {
    capacity = initialCapacity;
    this.loadFactor = loadFactor;
    sizeMaxLoad = capacity * loadFactor;
    storage = new int[capacity * 2];
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
      if (bucketIdx >= storage.length) {
        bucketIdx = 0;
      }
      int existingKey = storage[bucketIdx];
      if (existingKey == 0) { // absent
        storage[bucketIdx] = key;
        storage[bucketIdx + 1] = value;
        break;
      } else if (existingKey == key) {
        prevVal = storage[bucketIdx + 1];
        storage[bucketIdx + 1] = value; // overwrite
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
      if (bucketIdx >= storage.length) {
        bucketIdx = 0;
      }
      int existingKey = storage[bucketIdx];
      if (existingKey == 0) { // absent
        return 0; // absent
      } else if (existingKey == key) {
        size--;
        storage[bucketIdx] = 0; // clear
        return storage[bucketIdx + 1];
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

      int[] oldStorage = storage;
      storage = new int[capacity * 2];

      for (int bucketIdx = 0; bucketIdx < oldStorage.length; bucketIdx += 2) {
        int key = oldStorage[bucketIdx];
        if (key != 0) {
          insert(key, oldStorage[bucketIdx + 1]);
        }
      }
    }
  }

  public int get(int key) {
    int bucket = key % capacity;
    // we utilize the fact that for int : hash = value

    for (int bucketIdx = bucket * 2, i = 0; /*i<capacity*/ ; i++, bucketIdx += 2) {
      if (bucketIdx >= storage.length) {
        bucketIdx = 0;
      }
      int existingKey = storage[bucketIdx];
      if (existingKey == 0) { // absent
        return 0; // not found
      } else if (existingKey == key) {
        return storage[bucketIdx + 1];
      }
    }

//    return 0; // absent
  }

  public void clear() {
    size = 0;
    Arrays.fill(storage, 0);
  }

  public int size() {
    return size;
  }
}
