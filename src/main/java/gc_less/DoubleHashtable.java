package gc_less;


import static gc_less.TypeSizes.*;
import static gc_less.Unsafer.getUnsafe;

public class DoubleHashtable {

  private static final long sizeOffset = 0;
  private static final long refOffset = sizeOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long loadFactorOffset = capOffset + INT_SIZE;
  private static final long bucketsOffset = loadFactorOffset + FLOAT_SIZE;

  public static long allocate(int initialCapacity, float loadFactor) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long addr = getUnsafe().allocateMemory(bucketsOffset + initialCapacity * DOUBLE_SIZE);
    setSize(addr, 0);
    setLoadFactor(addr, loadFactor);
    setCapacity(addr, initialCapacity);
    setRef(addr, Ref.create(addr));
    return addr;
  }

  public static double put(long address, double key, double value) {
    throw new UnsupportedOperationException("TBD");
  }

  public static double get(long address, double key) {
    throw new UnsupportedOperationException("TBD");
  }

  public static boolean containsKey(long address, double key) {
    throw new UnsupportedOperationException("TBD");
  }

  public static double remove(long address, double key) {
    throw new UnsupportedOperationException("TBD");
  }

  public static void clear(long address) {
    throw new UnsupportedOperationException("TBD");
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
}
