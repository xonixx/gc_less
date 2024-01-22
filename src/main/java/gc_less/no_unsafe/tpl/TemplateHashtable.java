package gc_less.no_unsafe.tpl;

import static gc_less.TypeSizes.*;

import gc_less.Allocator;
import gc_less.Ref;
import gc_less.no_unsafe.NativeMem;
import gc_less.tpl.Type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class TemplateHashtable {

  private static final long sizeOffset = 0;
  private static final long refOffset = sizeOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long loadFactorOffset = capOffset + INT_SIZE;
  private static final long bucketsOffset = loadFactorOffset + FLOAT_SIZE;

  public static String toString(MemorySegment address) {
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
      //    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
      //    long bucketNode = getUnsafe().getLong(bucketAddr);
      long bucketNode = address.get(ValueLayout.JAVA_LONG, bucketOffset);

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
    private static final long valueOffset = keyOffset + Tpl.typeSize();
    private static final long nextOffset = valueOffset + Tpl.typeSize();
    private static final long totalMemToAllocate = nextOffset + LONG_SIZE;

    static MemorySegment allocate() {
      MemorySegment addr = NativeMem.malloc(totalMemToAllocate);
      addr.fill((byte) 0);
      return addr;
    }

    static void free(MemorySegment address) {
      NativeMem.free(address);
    }

    static int getHash(MemorySegment address) {
      return address.get(ValueLayout.JAVA_INT, hashOffset);
    }

    static @Type long getKey(MemorySegment address) {
      return Tpl.get(address, keyOffset);
    }

    static @Type long getValue(MemorySegment address) {
      return Tpl.get(address, valueOffset);
    }

    //    static MemorySegment getNext(MemorySegment address) {
    //      return MemorySegment.ofAddress(address.get(ValueLayout.JAVA_LONG,
    // nextOffset)).reinterpret(totalMemToAllocate);
    //    }
    static long getNext(long address) {
      return of(address).get(ValueLayout.JAVA_LONG, nextOffset);
    }

    static MemorySegment of(long address) {
      return MemorySegment.ofAddress(address).reinterpret(totalMemToAllocate);
    }

    static void setHash(MemorySegment address, int hash) {
      address.set(ValueLayout.JAVA_INT, hashOffset, hash);
    }

    static void setKey(MemorySegment address, @Type long key) {
      Tpl.put(address, keyOffset, key);
    }

    static void setValue(MemorySegment address, @Type long value) {
      Tpl.put(address, valueOffset, value);
    }

    static void setNext(MemorySegment address, long nextNodeAddress) {
      address.set(ValueLayout.JAVA_LONG, nextOffset, nextNodeAddress);
    }

    public static String toString(MemorySegment address) {
      return "{k=" + getKey(address) + ",v=" + getValue(address) + ",h=" + getHash(address) + "}";
    }
  }

  public static long allocate(
      Allocator allocator /* TODO */, int initialCapacity, float loadFactor) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long bytes = bucketsOffset + initialCapacity * LONG_SIZE;
    MemorySegment addr = NativeMem.malloc(bytes);
    addr.fill((byte) 0);
    setSize(addr, 0);
    setLoadFactor(addr, loadFactor);
    setCapacity(addr, initialCapacity);
    setRef(addr, Ref.create(addr));
    return addr;
  }

  public static MemorySegment put(MemorySegment address, @Type long key, @Type long value) {

    int hashCode = Tpl.hashCode(key);

    int capacity = getCapacity(address);
    int bucketIdx = hashCode % capacity;

    //    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
    //    long bucketNode = getUnsafe().getLong(bucketAddr);
    long bucketNode = address.get(ValueLayout.JAVA_LONG, bucketOffset);

    if (bucketNode == 0) { // empty bucket
      long node = Node.allocate();
      Node.setKey(node, key);
      Node.setHash(node, hashCode);
      Node.setValue(node, value);
      Node.setNext(node, 0);
      //      getUnsafe().putLong(bucketAddr, node);
      address.set(ValueLayout.JAVA_LONG, bucketOffset, node);
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
        //        getUnsafe().putLong(bucketAddr, node);
        address.set(ValueLayout.JAVA_LONG, bucketOffset, node);
        changeSize(address, 1);
      }
    }

    return resizeIfNeeded(address, capacity);
  }

  private static MemorySegment resizeIfNeeded(MemorySegment address, int capacity) {
    float loadFactor = getLoadFactor(address);
    if (getSize(address) / (float) capacity < loadFactor) {
      return address; // no change
    }

    int newCapacity = capacity * 2;
    long newAddress = TemplateHashtable.allocate(null, newCapacity, loadFactor);
    Ref.set(getRef(newAddress), newAddress);

    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      //    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
      //    long bucketNode = getUnsafe().getLong(bucketAddr);
      long bucketNode = address.get(ValueLayout.JAVA_LONG, bucketOffset);

      for (long node = bucketNode; 0 != node; node = Node.getNext(node)) {
        TemplateHashtable.put(newAddress, Node.getKey(node), Node.getValue(node));
      }
    }

    TemplateHashtable.free(address);
    return newAddress;
  }

  public static @Type long get(MemorySegment address, @Type long key) {
    int hashCode = Tpl.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    //    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
    //    long bucketNode = getUnsafe().getLong(bucketAddr);
    long bucketNode = address.get(ValueLayout.JAVA_LONG, bucketOffset);

    for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
      @Type long nodeKey = Node.getKey(node);
      if (nodeKey == key) {
        return Node.getValue(node);
      }
    }
    return 0; // TODO how we distinguish 0 from absent???
  }

  public static boolean containsKey(MemorySegment address, @Type long key) {
    int hashCode = Tpl.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    //    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
    //    long bucketNode = getUnsafe().getLong(bucketAddr);
    long bucketNode = address.get(ValueLayout.JAVA_LONG, bucketOffset);

    for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
      @Type long nodeKey = Node.getKey(node);
      if (nodeKey == key) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return removed value for key
   */
  public static @Type long remove(MemorySegment address, @Type long key) {
    int hashCode = Tpl.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    //    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
    //    long bucketNode = getUnsafe().getLong(bucketAddr);
    long bucketNode = address.get(ValueLayout.JAVA_LONG, bucketOffset);

    for (long prevNode = 0, node = bucketNode; node != 0; node = Node.getNext(node)) {
      MemorySegment nodeP = Node.of(node);
      @Type long nodeKey = Node.getKey(nodeP);
      if (nodeKey == key) {
        // remove node
        @Type long value = Node.getValue(nodeP);
        if (prevNode != 0) {
          Node.setNext(prevNode, Node.getNext(node));
        } else {
          // this was first node
          address.set(ValueLayout.JAVA_LONG, bucketOffset, 0);
        }
        Node.free();
        changeSize(address, -1);
        return value;
      }
      prevNode = node;
    }
    return 0; // TODO how we distinguish 0 from absent???
    // TODO shrink down?
  }

  public static void clear(MemorySegment address) {
    int capacity = getCapacity(address);
    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      //    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
      //    long bucketNode = getUnsafe().getLong(bucketAddr);
      long bucketNode = address.get(ValueLayout.JAVA_LONG, bucketOffset);

      for (long node = bucketNode; 0 != node; ) {
        long next = Node.getNext(node);
        Node.free(node);
        node = next;
      }
    }
  }

  public static long keys(MemorySegment address, Allocator allocator) {
    long keysArrayAddr = TemplateArray.allocate(allocator, getSize(address));
    int capacity = getCapacity(address);
    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      //    long bucketAddr = address + bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
      //    long bucketNode = getUnsafe().getLong(bucketAddr);
      long bucketNode = address.get(ValueLayout.JAVA_LONG, bucketOffset);

      int i = 0;
      for (long node = bucketNode; 0 != node; ) {
        long next = Node.getNext(node);
        TemplateArray.set(keysArrayAddr, i++, Node.getValue(node));
        node = next;
      }
    }
    return keysArrayAddr;
  }

  public static boolean isEmpty(MemorySegment address) {
    return 0 == getSize(address);
  }

  public static int getSize(MemorySegment address) {
    return address.get(ValueLayout.JAVA_INT, 0);
  }

  private static void setSize(MemorySegment address, int size) {
    address.set(ValueLayout.JAVA_INT, 0, size);
  }

  private static void changeSize(MemorySegment address, int delta) {
    setSize(address, getSize(address) + delta);
  }

  public static float getLoadFactor(MemorySegment address) {
    return address.get(ValueLayout.JAVA_FLOAT, loadFactorOffset);
  }

  private static void setLoadFactor(MemorySegment address, float loadFactor) {
    address.set(ValueLayout.JAVA_FLOAT, loadFactorOffset, loadFactor);
  }

  public static int getCapacity(MemorySegment address) {
    return address.get(ValueLayout.JAVA_INT, capOffset);
  }

  private static void setCapacity(MemorySegment address, int capacity) {
    address.set(ValueLayout.JAVA_INT, capOffset, capacity);
  }

  public static long getRef(MemorySegment address) {
    return getUnsafe().getLong(address + refOffset);
  }

  private static void setRef(MemorySegment address, long ref) {
    getUnsafe().putLong(address + refOffset, ref);
  }

  public static void free(MemorySegment address) {
    clear(address); // TODO is this enough?
    NativeMem.free(address);
  }
}
