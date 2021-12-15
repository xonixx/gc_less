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
    // hash int, key type, value type, next long
    private static final long hashOffset = 0;
    private static final long keyOffset = hashOffset + INT_SIZE;
    private static final long valueOffset = keyOffset + Tpl.typeSize();
    private static final long nextOffset = valueOffset + Tpl.typeSize();
    private static final long totalMemToAllocate = nextOffset + LONG_SIZE;

    static long allocate() {
      long addr = getUnsafe().allocateMemory(totalMemToAllocate);
      getUnsafe().setMemory(addr, totalMemToAllocate, (byte) 0);
      return addr;
    }

    static void free(long address) {
      getUnsafe().freeMemory(address);
    }

    static int getHash(long address) {
      return getUnsafe().getInt(address + hashOffset);
    }

    static @Type long getKey(long address) {
      return Tpl.get(address + keyOffset);
    }

    static @Type long getValue(long address) {
      return Tpl.get(address + valueOffset);
    }

    static long getNext(long address) {
      return getUnsafe().getLong(address + nextOffset);
    }

    static void setHash(long address, int hash) {
      getUnsafe().putInt(address + hashOffset, hash);
    }

    static void setKey(long address, @Type long key) {
      Tpl.put(address + keyOffset, key);
    }

    static void setValue(long address, @Type long value) {
      Tpl.put(address + valueOffset, value);
    }

    static void setNext(long address, long nextNodeAddress) {
      getUnsafe().putLong(address + nextOffset, nextNodeAddress);
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
      changeSize(address, 1);
    } else {
      boolean found = false;
      for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
        @Type long nodeKey = Node.getKey(node);
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

    // TODO reallocate
    return address;
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
    getUnsafe().freeMemory(address);
    throw new UnsupportedOperationException("TBD");
  }
}
