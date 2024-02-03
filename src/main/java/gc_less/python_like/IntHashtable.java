package gc_less.python_like;

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
    int existingVal = insert(key, value);

    size++;

    resizeIfNeeded();

    return existingVal;
  }

  private int insert(int key, int value) {
    // we utilize the fact that for int : hash = value
    int bucket = key % capacity;

    int existingVal = 0;

    out:
    {
      for (int bucketIdx = bucket * 2; bucketIdx < storage.length; bucketIdx += 2) {
        int existingKey = storage[bucketIdx];
        if (existingKey == 0) { // absent
          storage[bucketIdx] = key;
          storage[bucketIdx + 1] = value;
          break out;
        } else if (existingKey == key) {
          existingVal = storage[bucketIdx + 1];
          storage[bucketIdx + 1] = value; // overwrite
          break out;
        }
      }

      throw new RuntimeException("todo handle this case");
    }
    return existingVal;
  }

  /**
   * @return prev value for key
   */
  public int remove(int key) {
    // we utilize the fact that for int : hash = value
    int bucket = key % capacity;

    for (int bucketIdx = bucket * 2; bucketIdx < storage.length; bucketIdx += 2) {
      int existingKey = storage[bucketIdx];
      if (existingKey == 0) { // absent
        return 0; // absent
      } else if (existingKey == key) {
        size--;
        storage[bucketIdx] = 0; // clear
        return storage[bucketIdx + 1];
      }
    }

    return 0; // not found
  }

  private void resizeIfNeeded() {
    if (size > sizeMaxLoad) {
      int oldCapacity = capacity;
      capacity = (int) (capacity * 1.5);
      System.out.println("resizing " + oldCapacity + " -> " + capacity + "...");
      sizeMaxLoad = capacity * loadFactor;

      int[] oldStorage = storage;
      storage = new int[capacity];

      for (int i = 0; i < oldCapacity; i += 2) {
        int key = oldStorage[i];
        if (key != 0) {
          insert(key, oldStorage[i + 1]);
        }
      }
    }
  }

  public int get(int key) {
    int bucket = key % capacity;
    // we utilize the fact that for int : hash = value

    for (int bucketIdx = bucket * 2; bucketIdx < storage.length; bucketIdx += 2) {
      int existingKey = storage[bucketIdx];
      if (existingKey == 0) { // absent
        return 0; // not found
      } else if (existingKey == key) {
        return storage[bucketIdx + 1];
      }
    }

    return 0; // absent
  }

  public int size() {
    return size;
  }
}
