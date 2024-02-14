package gc_less;

import static gc_less.TypeSizes.*;
import static gc_less.Unsafer.getUnsafe;


public class DoubleStack {
  public static final int typeId = TypeMeta.nextTypeId();
  private static final long lengthOffset = 0;
  private static final long refOffset = lengthOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long dataOffset = capOffset + INT_SIZE;

  /** Must be freed via {@link #free} */
  public static long allocate(int initialCapacity) {
    return allocate(null, initialCapacity);
  }

  public static long allocate(Cleaner cleaner, int initialCapacity) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long addr = Unsafer.allocateMem(dataOffset + initialCapacity * DOUBLE_SIZE);
    setLength(addr, 0);
    setCapacity(addr, initialCapacity);
    long ref = Ref.create(addr, typeId);
    setRef(addr, ref);
    if (cleaner != null) {
      cleaner.registerForCleanup(ref);
    }
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
      long newAddr = Unsafer.reallocateMem(addr, dataOffset + capacity * DOUBLE_SIZE);
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
    Unsafer.freeMem(address);
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
