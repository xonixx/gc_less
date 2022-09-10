package gc_less.tpl;

import gc_less.Allocator;
import gc_less.Ref;
import gc_less.Unsafer;

import static gc_less.TypeSizes.INT_SIZE;
import static gc_less.TypeSizes.LONG_SIZE;
import static gc_less.Unsafer.getUnsafe;

/** Resizable array (similar to ArrayList in Java) */
public class TemplateArrayList {
  private static final long lengthOffset = 0;
  private static final long refOffset = lengthOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long dataOffset = capOffset + INT_SIZE;

  public static long allocate(int initialCapacity) {
    return allocate(null, initialCapacity);
  }

  public static long allocate(Allocator allocator, int initialCapacity) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long bytes = dataOffset + initialCapacity * Tpl.typeSize();
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

  public static long add(long addr, @Type long value) {
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    Tpl.put(addr + dataOffset + len * Tpl.typeSize(), value);
    setLength(addr, ++len);
    return addr;
  }

  public static long add(long addr, int index, @Type long value) {
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    long indexAddr = addr + dataOffset + index * Tpl.typeSize();
    Unsafer.getUnsafe().copyMemory(indexAddr, indexAddr + 1, (len - index) * Tpl.typeSize());
    Tpl.put(indexAddr, value);
    setLength(addr, ++len);
    return addr;
  }

  private static long ensureCapacity(long addr, int len) {
    int capacity = getCapacity(addr);
    if (capacity == len) {
      setCapacity(addr, capacity = 2 * capacity);
      long newAddr = getUnsafe().reallocateMemory(addr, dataOffset + capacity * Tpl.typeSize());
      Ref.set(getRef(newAddr), newAddr);
      return newAddr;
    }
    return addr;
  }

  public static void set(long address, int index, @Type long value) {
    checkBoundaries(address, index);
    Tpl.put(address + dataOffset + index * Tpl.typeSize(), value);
  }

  public static @Type long get(long address, int index) {
    checkBoundaries(address, index);
    return Tpl.get(address + dataOffset + index * Tpl.typeSize());
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
