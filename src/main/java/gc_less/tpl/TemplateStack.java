package gc_less.tpl;

import gc_less.Ref;

import static gc_less.TypeSizes.INT_SIZE;
import static gc_less.TypeSizes.LONG_SIZE;
import static gc_less.Unsafer.getUnsafe;

public class TemplateStack {
  private static final long lengthOffset = 0;
  private static final long refOffset = lengthOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long dataOffset = capOffset + INT_SIZE;

  public static long allocate(int initialCapacity) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long addr = getUnsafe().allocateMemory(dataOffset + initialCapacity * Tpl.typeSize());
    setLength(addr, 0);
    setCapacity(addr, initialCapacity);
    setRef(addr, Ref.create(addr));
    return addr;
  }

  public static long push(long addr, @Type long value) {
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    Tpl.put(addr + dataOffset + len * Tpl.typeSize(), value);
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

  public static @Type long pop(long addr) {
    int len = getLength(addr);
    setLength(addr, --len);
    return Tpl.get(addr + dataOffset + len * Tpl.typeSize());
  }

  public static @Type long peek(long addr) {
    return Tpl.get(addr + dataOffset + (getLength(addr) - 1) * Tpl.typeSize());
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
