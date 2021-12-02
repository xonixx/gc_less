package gc_less;

import static gc_less.Unsafer.getUnsafe;

public class LongStack {
  public static final int INITIAL_CAP = 10;
  public static final long INT_SIZE = 4;
  public static final long LONG_SIZE = 8;

  private static final long lengthOffset = 0;
  private static final long capOffset = lengthOffset + INT_SIZE;
  private static final long dataOffset = capOffset + INT_SIZE;

  public static long init() {
    return init(INITIAL_CAP);
  }

  public static long init(int initialCapacity) {
    long addr = getUnsafe().allocateMemory(dataOffset + initialCapacity * LONG_SIZE);
    setLength(addr, 0);
    setCapacity(addr, initialCapacity);
    return addr;
  }

  public static long push(long addr, long value) {
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    getUnsafe().putLong(addr + dataOffset + len * LONG_SIZE, value);
    setLength(addr, ++len);
    return addr;
  }

  private static long ensureCapacity(long addr, int len) {
    int capacity = getCapacity(addr);
    if (capacity == len) {
      setCapacity(addr, capacity = 2 * capacity);
      return getUnsafe().reallocateMemory(addr, dataOffset + capacity * LONG_SIZE);
    }
    return addr;
  }

  public static long pop(long addr) {
    int len = getLength(addr);
    setLength(addr, --len);
    return getUnsafe().getLong(addr + dataOffset + len * LONG_SIZE);
  }

  public static long peek(long addr) {
    return getUnsafe().getLong(addr + dataOffset + (getLength(addr) - 1) * LONG_SIZE);
  }

  public static void free(long address) {
    getUnsafe().freeMemory(address);
  }

  public static int getLength(long address) {
    return getUnsafe().getInt(address);
  }

  public static void setLength(long address, int length) {
    getUnsafe().putInt(address, length);
  }

  public static int getCapacity(long address) {
    return getUnsafe().getInt(address + capOffset);
  }

  static void setCapacity(long address, int capacity) {
    getUnsafe().putInt(address + capOffset, capacity);
  }
}
