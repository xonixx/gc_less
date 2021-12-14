package gc_less.tpl;

import gc_less.Allocator;
import gc_less.Ref;

import static gc_less.TypeSizes.*;
import static gc_less.Unsafer.getUnsafe;

public class TemplateHashtable {

  private static final long sizeOffset = 0;
  private static final long refOffset = sizeOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long loadFactorOffset = capOffset + INT_SIZE;
  private static final long bucketsOffset = loadFactorOffset + FLOAT_SIZE;

  private static class Node {
    static long allocate() {
      throw new UnsupportedOperationException("TBD");
    }

    static void free(long address) {
      throw new UnsupportedOperationException("TBD");
    }

    static int getHash(long address) {
      throw new UnsupportedOperationException("TBD");
    }

    static @Type long getKey(long address) {
      throw new UnsupportedOperationException("TBD");
    }

    static @Type long getValue(long address) {
      throw new UnsupportedOperationException("TBD");
    }

    static long getNext(long address) {
      return 0;
      //      throw new UnsupportedOperationException("TBD");
    }

    static void setHash(long address, int hash) {
      throw new UnsupportedOperationException("TBD");
    }

    static void setKey(long address, @Type long key) {
      throw new UnsupportedOperationException("TBD");
    }

    static void setValue(long address, @Type long value) {
      throw new UnsupportedOperationException("TBD");
    }

    static void setNext(long address, long nextNodeAddress) {
      throw new UnsupportedOperationException("TBD");
    }
  }

  public static long allocate(int initialCapacity, float loadFactor) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long bytes = bucketsOffset + initialCapacity * LONG_SIZE;
    long addr = getUnsafe().allocateMemory(bytes);
    getUnsafe().setMemory(addr, bytes, (byte) 0);
    setSize(addr, 0);
    setLoadFactor(addr, loadFactor);
    setCapacity(addr, initialCapacity);
    setRef(addr, Ref.create(addr));
    return addr;
  }

  public static @Type long put(long address, @Type long key, @Type long value) {

    int hashCode = Tpl.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    long bucketAddr = bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = getUnsafe().getLong(bucketAddr);

    if (bucketNode == 0) { // empty bucket
      long node = Node.allocate();
      Node.setKey(node, key);
      Node.setHash(node, hashCode);
      Node.setValue(node, value);
      Node.setNext(node, 0);
      getUnsafe().putLong(bucketAddr, node);
    } else {
      for (long prevNode = 0, node = bucketNode; node != 0; node = Node.getNext(node)) {
        @Type long nodeKey = Node.getKey(node);
        if (nodeKey == key) {
          // replace node
          Node.setHash(node, hashCode);
          Node.setValue(node, value);

          break;
        }
        prevNode = node;
      }
    }

    throw new UnsupportedOperationException("TBD");
  }

  public static @Type long get(long address, @Type long key) {
    throw new UnsupportedOperationException("TBD");
  }

  public static boolean containsKey(long address, @Type long key) {
    throw new UnsupportedOperationException("TBD");
  }

  public static @Type long remove(long address, @Type long key) {
    throw new UnsupportedOperationException("TBD");
  }

  public static void clear(long address) {
    throw new UnsupportedOperationException("TBD");
  }

  public static long keys(long address, Allocator allocator) {
    long keysArrayAddr = TemplateArray.allocate(allocator, getSize(address));
    // TODO implement
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
    getUnsafe().freeMemory(address);
    throw new UnsupportedOperationException("TBD");
  }
}
