package gc_less;


import static gc_less.TypeSizes.*;
import static gc_less.Unsafer.getUnsafe;

/** Resizable array (similar to ArrayList in Java) */
public class DoubleArrayList {
  private static final long lengthOffset = 0;
  private static final long refOffset = lengthOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long dataOffset = capOffset + INT_SIZE;

  public static long allocate(int initialCapacity) {
    return allocate(null, initialCapacity);
  }

  public static long allocate(Allocator allocator, int initialCapacity) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long bytes = dataOffset + initialCapacity * DOUBLE_SIZE;
    long addr = getUnsafe().allocateMemory(bytes);
    setLength(addr, 0);
    setCapacity(addr, initialCapacity);
    long ref = Ref.create(addr);
    setRef(addr, ref);
    if (allocator != null) {
      allocator.registerForCleanup(ref);
    }
    return addr;
  }

  public static void free(long address) {
    getUnsafe().freeMemory(address);
  }

  public static long add(long addr, double value) {
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    getUnsafe().putDouble(addr + dataOffset + len * DOUBLE_SIZE, value);
    setLength(addr, ++len);
    return addr;
  }

  public static long add(long addr, int index, double value) {
    checkBoundaries(addr, index);
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    long indexAddr = addr + dataOffset + index * DOUBLE_SIZE;
    Unsafer.getUnsafe()
        .copyMemory(indexAddr, indexAddr + DOUBLE_SIZE, (len - index) * DOUBLE_SIZE);
    getUnsafe().putDouble(indexAddr, value);
    setLength(addr, ++len);
    return addr;
  }

  public static double remove(long addr, int index) {
    double result = get(addr, index);
    int len = getLength(addr);
    long indexAddr = addr + dataOffset + index * DOUBLE_SIZE;
    Unsafer.getUnsafe()
        .copyMemory(indexAddr + DOUBLE_SIZE, indexAddr, (len - index - 1) * DOUBLE_SIZE);
    setLength(addr, --len);
    return result;
  }

  private static long ensureCapacity(long addr, int len) {
    int capacity = getCapacity(addr);
    if (capacity == len) {
      setCapacity(addr, capacity = 2 * capacity);
      long newAddr = getUnsafe().reallocateMemory(addr, dataOffset + capacity * DOUBLE_SIZE);
      Ref.set(getRef(newAddr), newAddr);
      return newAddr;
    }
    return addr;
  }

  public static void set(long address, int index, double value) {
    checkBoundaries(address, index);
    getUnsafe().putDouble(address + dataOffset + index * DOUBLE_SIZE, value);
  }

  public static double get(long address, int index) {
    checkBoundaries(address, index);
    return getUnsafe().getDouble(address + dataOffset + index * DOUBLE_SIZE);
  }

  private static void checkBoundaries(long address, int index) {
    if (index < 0 || index >= getLength(address)) throw new IndexOutOfBoundsException();
  }

  public static int getLength(long address) {
    return getUnsafe().getInt(address);
  }

  private static void setLength(long address, int length) {
    getUnsafe().putInt(address, length);
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
}
