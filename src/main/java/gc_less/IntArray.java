package gc_less;

import static gc_less.TypeSizes.INT_SIZE;
import static gc_less.Unsafer.getUnsafe;

/** Non-resizable array (similar to arrays in Java) */
// TODO arraycopy
public class IntArray {
  private static final long lengthOffset = 0;
  private static final long dataOffset = lengthOffset + INT_SIZE;

  public static long allocate(int length) {
    long bytes = dataOffset + length * INT_SIZE;
    long addr = getUnsafe().allocateMemory(bytes);
    getUnsafe().setMemory(addr, bytes, (byte) 0);
    setLength(addr, length);
    return addr;
  }

  public static void set(long address, long index, int value) {
    checkBoundaries(address, index);
    getUnsafe().putInt(address + dataOffset + index * INT_SIZE, value);
  }

  public static int get(long address, long index) {
    checkBoundaries(address, index);
    return getUnsafe().getInt(address + dataOffset + index * INT_SIZE);
  }

  private static void checkBoundaries(long address, long index) {
    if (index < 0 || index >= getLength(address)) throw new IndexOutOfBoundsException();
  }

  public static int getLength(long address) {
    return getUnsafe().getInt(address);
  }

  private static void setLength(long address, int length) {
    getUnsafe().putInt(address, length);
  }

  public static void free(long address) {
    getUnsafe().freeMemory(address);
  }
}
