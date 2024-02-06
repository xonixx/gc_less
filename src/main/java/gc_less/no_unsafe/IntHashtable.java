package gc_less.no_unsafe;

import static gc_less.TypeSizes.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class IntHashtable {

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
      long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketNode = address.get(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset);

      if (bucketNode != 0) {
        sb.append("  ").append(bucketIdx).append("->");
        for (long node = bucketNode; 0 != node; node = Node.getNext(node)) {
          sb.append(Node.toString(Node.of(node))).append("->");
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
    private static final long valueOffset = keyOffset + INT_SIZE;
    private static final long nextOffset = valueOffset + INT_SIZE;
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
      return address.get(ValueLayout.JAVA_INT_UNALIGNED, hashOffset);
    }

    static int getKey(MemorySegment address) {
      return address.get(ValueLayout.JAVA_INT_UNALIGNED, keyOffset);
    }

    static int getValue(MemorySegment address) {
      return address.get(ValueLayout.JAVA_INT_UNALIGNED, valueOffset);
    }

    //    static MemorySegment getNext(MemorySegment address) {
    //      return MemorySegment.ofAddress(address.get(ValueLayout.JAVA_LONG_UNALIGNED,
    // nextOffset)).reinterpret(totalMemToAllocate);
    //    }
    static long getNext(long address) {
      return of(address).get(ValueLayout.JAVA_LONG_UNALIGNED, nextOffset);
    }

    static MemorySegment of(long address) {
      return MemorySegment.ofAddress(address).reinterpret(totalMemToAllocate);
    }

    static void setHash(MemorySegment address, int hash) {
      address.set(ValueLayout.JAVA_INT_UNALIGNED, hashOffset, hash);
    }

    static void setKey(MemorySegment address, int key) {
      address.set(ValueLayout.JAVA_INT_UNALIGNED, keyOffset, key);
    }

    static void setValue(MemorySegment address, int value) {
      address.set(ValueLayout.JAVA_INT_UNALIGNED, valueOffset, value);
    }

    static void setNext(MemorySegment address, long nextNodeAddress) {
      address.set(ValueLayout.JAVA_LONG_UNALIGNED, nextOffset, nextNodeAddress);
    }

    public static String toString(MemorySegment address) {
      return "{k=" + getKey(address) + ",v=" + getValue(address) + ",h=" + getHash(address) + "}";
    }
  }

  public static MemorySegment allocate(Arena arena, int initialCapacity, float loadFactor) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long bytes = bucketsOffset + initialCapacity * LONG_SIZE;
    MemorySegment addr = NativeMem.malloc(bytes);
    addr.fill((byte) 0);
    setSize(addr, 0);
    setLoadFactor(addr, loadFactor);
    setCapacity(addr, initialCapacity);
    if (arena != null) {
      MemorySegment ref = Ref.create(addr, 1);
      setRef(addr, ref);
      //      allocator.registerForCleanup(ref); TODO
    }
    return addr;
  }

  public static MemorySegment put(MemorySegment address, int key, int value) {

    int hashCode = Integer.hashCode(key);

    int capacity = getCapacity(address);
    int bucketIdx = hashCode % capacity;

    long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = address.get(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset);

    if (bucketNode == 0) { // empty bucket
      MemorySegment node = Node.allocate();
      Node.setKey(node, key);
      Node.setHash(node, hashCode);
      Node.setValue(node, value);
      Node.setNext(node, 0);
      address.set(ValueLayout.ADDRESS_UNALIGNED, bucketOffset, node);
      changeSize(address, 1);
    } else {
      boolean found = false;
      for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
        MemorySegment nodeMs = Node.of(node);
        int nodeKey = Node.getKey(nodeMs);
        if (nodeKey == key) {
          // replace node
          Node.setHash(nodeMs, hashCode);
          Node.setValue(nodeMs, value);
          found = true;
          break;
        }
      }
      if (!found) {
        // insert
        MemorySegment node = Node.allocate();
        Node.setKey(node, key);
        Node.setHash(node, hashCode);
        Node.setValue(node, value);
        Node.setNext(node, bucketNode);
        address.set(ValueLayout.ADDRESS_UNALIGNED, bucketOffset, node);
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
    MemorySegment newAddress = IntHashtable.allocate(null, newCapacity, loadFactor);
    MemorySegment ref = getRef(address);
    setRef(newAddress, ref);
    Ref.set(ref, newAddress);

    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketNode = address.get(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset);

      for (long node = bucketNode; 0 != node; node = Node.getNext(node)) {
        MemorySegment nodeMs = Node.of(node);
        IntHashtable.put(newAddress, Node.getKey(nodeMs), Node.getValue(nodeMs));
      }
    }

    IntHashtable.free(address);
    return newAddress;
  }

  public static int get(MemorySegment address, int key) {
    int hashCode = Integer.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = address.get(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset);

    for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
      MemorySegment nodeMs = Node.of(node);
      int nodeKey = Node.getKey(nodeMs);
      if (nodeKey == key) {
        return Node.getValue(nodeMs);
      }
    }
    return 0; // TODO how we distinguish 0 from absent???
  }

  public static boolean containsKey(MemorySegment address, int key) {
    int hashCode = Integer.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = address.get(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset);

    for (long node = bucketNode; node != 0; node = Node.getNext(node)) {
      MemorySegment nodeMs = Node.of(node);
      int nodeKey = Node.getKey(nodeMs);
      if (nodeKey == key) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return removed value for key
   */
  public static int remove(MemorySegment address, int key) {
    int hashCode = Integer.hashCode(key);

    int bucketIdx = hashCode % getCapacity(address);

    long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
    long bucketNode = address.get(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset);

    for (long prevNode = 0, node = bucketNode; node != 0; node = Node.getNext(node)) {
      MemorySegment nodeP = Node.of(node);
      int nodeKey = Node.getKey(nodeP);
      if (nodeKey == key) {
        // remove node
        int value = Node.getValue(nodeP);
        if (prevNode != 0) {
          Node.setNext(Node.of(prevNode), Node.getNext(node));
        } else {
          // this was first node
          address.set(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset, 0);
        }
        Node.free(nodeP);
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
      long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketNode = address.get(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset);

      for (long node = bucketNode; 0 != node; ) {
        long next = Node.getNext(node);
        Node.free(Node.of(node));
        node = next;
      }
    }
  }

  public static MemorySegment keys(MemorySegment address, Arena arena) {
    MemorySegment keysArrayAddr = IntArray.allocate(arena, getSize(address));
    int capacity = getCapacity(address);
    int i = 0;
    for (int bucketIdx = 0; bucketIdx < capacity; bucketIdx++) {
      long bucketOffset = bucketsOffset + bucketIdx * LONG_SIZE;
      long bucketNode = address.get(ValueLayout.JAVA_LONG_UNALIGNED, bucketOffset);

      for (long node = bucketNode; 0 != node; node = Node.getNext(node)) {
        IntArray.set(keysArrayAddr, i++, Node.getKey(Node.of(node)));
      }
    }
    return keysArrayAddr;
  }

  public static boolean isEmpty(MemorySegment address) {
    return 0 == getSize(address);
  }

  public static int getSize(MemorySegment address) {
    return address.get(ValueLayout.JAVA_INT_UNALIGNED, 0);
  }

  private static void setSize(MemorySegment address, int size) {
    address.set(ValueLayout.JAVA_INT_UNALIGNED, 0, size);
  }

  private static void changeSize(MemorySegment address, int delta) {
    setSize(address, getSize(address) + delta);
  }

  public static float getLoadFactor(MemorySegment address) {
    return address.get(ValueLayout.JAVA_FLOAT_UNALIGNED, loadFactorOffset);
  }

  private static void setLoadFactor(MemorySegment address, float loadFactor) {
    address.set(ValueLayout.JAVA_FLOAT_UNALIGNED, loadFactorOffset, loadFactor);
  }

  public static int getCapacity(MemorySegment address) {
    return address.get(ValueLayout.JAVA_INT_UNALIGNED, capOffset);
  }

  private static void setCapacity(MemorySegment address, int capacity) {
    address.set(ValueLayout.JAVA_INT_UNALIGNED, capOffset, capacity);
  }

  public static MemorySegment getRef(MemorySegment address) {
    return address.get(ValueLayout.ADDRESS_UNALIGNED, refOffset).reinterpret(Ref.totalBytes);
  }

  private static void setRef(MemorySegment address, MemorySegment ref) {
    address.set(ValueLayout.ADDRESS_UNALIGNED, refOffset, ref);
  }

  public static void free(MemorySegment address) {
    clear(address);
    NativeMem.free(address);
  }
}
