package gc_less;

import gc_less.Ref;

import static gc_less.TypeSizes.INT_SIZE;
import static gc_less.TypeSizes.LONG_SIZE;
import static gc_less.TypeSizes.DOUBLE_SIZE;
import static gc_less.Unsafer.getUnsafe;

public class DoubleStack {
  public static final int INITIAL_CAP = 10;

  private static final long lengthOffset = 0;
  private static final long refOffset = lengthOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long dataOffset = capOffset + INT_SIZE;

  public static long allocate() {
    return allocate(INITIAL_CAP);
  }

  public static long allocate(int initialCapacity) {
    long addr = getUnsafe().allocateMemory(dataOffset + initialCapacity * DOUBLE_SIZE);
    setLength(addr, 0);
    setCapacity(addr, initialCapacity);
    setRef(addr, Ref.create(addr));
    return addr;
  }

  public static long push(long addr, double value) {
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    getUnsafe().putDouble(addr + dataOffset + len * DOUBLE_SIZE, value);
    setLength(addr, ++len);
    return addr;
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

  public static double pop(long addr) {
    int len = getLength(addr);
    setLength(addr, --len);
    return getUnsafe().getDouble(addr + dataOffset + len * DOUBLE_SIZE);
  }

  public static double peek(long addr) {
    return getUnsafe().getDouble(addr + dataOffset + (getLength(addr) - 1) * DOUBLE_SIZE);
  }

  public static void free(long address) {
    getUnsafe().freeMemory(address);
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