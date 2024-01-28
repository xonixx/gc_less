package gc_less;


import static gc_less.TypeSizes.*;
import static gc_less.Unsafer.getUnsafe;

public class DoubleHashtable {

  private static final long sizeOffset = 0;
  private static final long refOffset = sizeOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long loadFactorOffset = capOffset + INT_SIZE;
  private static final long bucketsOffset = loadFactorOffset + FLOAT_SIZE;

  public static String toString(long address) {
    int capacity = getCapacity(address);
    StringBuilder sb =
        new StringBuilder()
            .append("{size=")
            .append(getSize(address))
            .append(",cap=")
            .append(capacity)
            .append(",lf=")
            .append(getLoadFactor(address))
            .append(",\nbuckets=[\n");

    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketNode = getUnsafe().getLong(bucketAddr);

      if (bucketNode != 0) {
        sb.append("  ").append(bucketIdx).append("->");
        for (long node = bucketNode; 0 != node; node = Node.getNext(node)) {
          sb.append(Node.toString(node)).append("->");
        }
        sb.setLength(sb.length() - 2);
        sb.append("\n");
      }
    }

    sb.append("]}");
    return sb.toString();
  }

  private static class Node {
    // hash int, key type, value type, next long
    private static final long hashOffset = 0;
    private static final long keyOffset = hashOffset + INT_SIZE;
    private static final long valueOffset = keyOffset + DOUBLE_SIZE;
    private static final long nextOffset = valueOffset + DOUBLE_SIZE;
    private static final long totalMemToAllocate = nextOffset + LONG_SIZE;

    static long allocate() {
      long addr = Unsafer.allocateMem(totalMemToAllocate);
      getUnsafe().setMemory(addr, totalMemToAllocate, (byte) 0);
      return addr;
    }

    static void free(long address) {
      Unsafer.freeMem(address);
    }

    static int getHash(long address) {
      return getUnsafe().getInt(address + hashOffset);
    }

    static double getKey(long address) {
      return getUnsafe().getDouble(address + keyOffset);
    }

    static double getValue(long address) {
      return getUnsafe().getDouble(address + valueOffset);
    }

    static long getNext(long address) {
      return getUnsafe().getLong(address + nextOffset);
    }

    static void setHash(long address, int hash) {
      getUnsafe().putInt(address + hashOffset, hash);
    }

    static void setKey(long address, double key) {
      getUnsafe().putDouble(address + keyOffset, key);
    }

    static void setValue(long address, double value) {
      getUnsafe().putDouble(address + valueOffset, value);
    }

    static void setNext(long address, long nextNodeAddress) {
      getUnsafe().putLong(address + nextOffset, nextNodeAddress);
    }

    public static String toString(long address) {
      return "{k=" + getKey(address) + ",v=" + getValue(address) + ",h=" + getHash(address) + "}";
    }
  }

  public static long allocate(Allocator allocator/* TODO */, int initialCapacity, float loadFactor) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long bytes = bucketsOffset + initialCapacity * LONG_SIZE;
    long addr = Unsafer.allocateMem(bytes);
    getUnsafe().setMemory(addr, bytes, (byte) 0);
    setSize(addr, 0);
    setLoadFactor(addr, loadFactor);
    setCapacity(addr, initialCapacity);
    setRef(addr, Ref.create(addr));
    return addr;
  }

  public static long put(long address, double key, double value) {

    int hashCode = Double.hashCode(key);

    int capacity = getCapacity(address);
    int bucketIdx = hashCode % capacity;

    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = getUnsafe().getLong(bucketAddr);

    if (bucketNode == 0) { // empty bucket
      long node = Node.allocate();
      Node.setKey(node, key);
      Node.setHash(node, hashCode);
      Node.setValue(node, value);
      Node.setNext(node, 0);
      getUnsafe().putLong(bucketAddr, node);
      changeSize(address, 1);
    } else {
      boolean found = false;
      for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
        double nodeKey = Node.getKey(node);
        if (nodeKey == key) {
          // replace node
          Node.setHash(node, hashCode);
          Node.setValue(node, value);
          found = true;
          break;
        }
      }
      if (!found) {
        // insert
        long node = Node.allocate();
        Node.setKey(node, key);
        Node.setHash(node, hashCode);
        Node.setValue(node, value);
        Node.setNext(node, bucketNode);
        getUnsafe().putLong(bucketAddr, node);
        changeSize(address, 1);
      }
    }

    return resizeIfNeeded(address, capacity);
  }

  private static long resizeIfNeeded(long address, int capacity) {
    float loadFactor = getLoadFactor(address);
    if (getSize(address) / (float) capacity < loadFactor) {
      return address; // no change
    }

    int newCapacity = capacity * 2;
    long newAddress = DoubleHashtable.allocate(null, newCapacity, loadFactor);
    Ref.set(getRef(newAddress), newAddress);

    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketNode = getUnsafe().getLong(bucketAddr);

      for (long node = bucketNode; 0 != node; node = Node.getNext(node)) {
        DoubleHashtable.put(newAddress, Node.getKey(node), Node.getValue(node));
      }
    }

    DoubleHashtable.free(address);
    return newAddress;
  }

  public static double get(long address, double key) {
    int hashCode = Double.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = getUnsafe().getLong(bucketAddr);

    for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
      double nodeKey = Node.getKey(node);
      if (nodeKey == key) {
        return Node.getValue(node);
      }
    }
    return 0; // TODO how we distinguish 0 from absent???
  }

  public static boolean containsKey(long address, double key) {
    int hashCode = Double.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = getUnsafe().getLong(bucketAddr);

    for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
      double nodeKey = Node.getKey(node);
      if (nodeKey == key) {
        return true;
      }
    }
    return false;
  }

  /** @return removed value for key */
  public static double remove(long address, double key) {
    int hashCode = Double.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = getUnsafe().getLong(bucketAddr);

    for (long prevNode = 0, node = bucketNode; node != 0; node = Node.getNext(node)) {
      double nodeKey = Node.getKey(node);
      if (nodeKey == key) {
        // remove node
        double value = Node.getValue(node);
        if (prevNode != 0) {
          Node.setNext(prevNode, Node.getNext(node));
        } else {
          // this was first node
          getUnsafe().putLong(bucketAddr, 0);
        }
        Node.free(node);
        changeSize(address, -1);
        return value;
      }
      prevNode = node;
    }
    return 0; // TODO how we distinguish 0 from absent???
    // TODO shrink down?
  }

  public static void clear(long address) {
    int capacity = getCapacity(address);
    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketNode = getUnsafe().getLong(bucketAddr);

      for (long node = bucketNode; 0 != node; ) {
        long next = Node.getNext(node);
        Node.free(node);
        node = next;
      }
    }
  }

  public static long keys(long address, Allocator allocator) {
    long keysArrayAddr = DoubleArray.allocate(allocator, getSize(address));
    int capacity = getCapacity(address);
    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketNode = getUnsafe().getLong(bucketAddr);

      int i = 0;
      for (long node = bucketNode; 0 != node; ) {
        long next = Node.getNext(node);
        DoubleArray.set(keysArrayAddr, i++, Node.getValue(node));
        node = next;
      }
    }
    return keysArrayAddr;
  }

  public static boolean isEmpty(long address) {
    return 0 == getSize(address);
  }

  public static int getSize(long address) {
    return getUnsafe().getInt(address);
  }

  private static void setSize(long address, int size) {
    getUnsafe().putInt(address, size);
  }

  private static void changeSize(long address, int delta) {
    setSize(address, getSize(address) + delta);
  }

  public static float getLoadFactor(long address) {
    return getUnsafe().getFloat(address + loadFactorOffset);
  }

  private static void setLoadFactor(long address, float loadFactor) {
    getUnsafe().putFloat(address + loadFactorOffset, loadFactor);
  }

  public static int getCapacity(long address) {
    return getUnsafe().getInt(address + capOffset);
  }

  private static void setCapacity(long address, int capacity) {
    getUnsafe().putInt(address + capOffset, capacity);
  }

  public static long getRef(long address) {
    return getUnsafe().getLong(address + refOffset);
  }

  private static void setRef(long address, long ref) {
    getUnsafe().putLong(address + refOffset, ref);
  }

  public static void free(long address) {
    clear(address); // TODO is this enough?
    Unsafer.freeMem(address);
  }
}
