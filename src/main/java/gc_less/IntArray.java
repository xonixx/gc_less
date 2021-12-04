package gc_less;

import static gc_less.TypeSizes.INT_SIZE;
import static gc_less.Unsafer.getUnsafe;

/** Non-resizable array (similar to arrays in Java) */
// TODO check boundaries
// TODO init with zero
// TODO arraycopy
public class IntArray {
  private static final long lengthOffset = 0;
  private static final long dataOffset = lengthOffset + INT_SIZE;

  public static long allocate(int length) {
    long addr = getUnsafe().allocateMemory(dataOffset + length * INT_SIZE);
    setLength(addr, length);
    return addr;
  }

  public static void set(long address, long index, int value) {
    getUnsafe().putInt(dataOffset + index * INT_SIZE, value);
  }

  public static int get(long address, long index) {
    return getUnsafe().getInt(dataOffset + index * INT_SIZE);
  }

  public static int getLength(long address) {
    return getUnsafe().getInt(address);
  }

  private static void setLength(long address, int length) {
    getUnsafe().putInt(address, length);
  }
}
