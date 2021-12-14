package gc_less;


import static gc_less.TypeSizes.*;
import static gc_less.Unsafer.getUnsafe;

/** Non-resizable array (similar to arrays in Java) */
public class IntArray {
  private static final long lengthOffset = 0;
  private static final long dataOffset = lengthOffset + INT_SIZE;

  public static long allocate(int length) {
    return allocate(null, length);
  }

  public static long allocate(Allocator allocator, int length) {
    long bytes = dataOffset + length * INT_SIZE;
    long addr = getUnsafe().allocateMemory(bytes);
    getUnsafe().setMemory(addr, bytes, (byte) 0);
    setLength(addr, length);
    if (allocator != null) {
      allocator.registerForCleanup(Ref.create(addr));
    }
    return addr;
  }

  public static void set(long address, int index, int value) {
    checkBoundaries(address, index);
    getUnsafe().putInt(address + dataOffset + index * INT_SIZE, value);
  }

  public static int get(long address, int index) {
    checkBoundaries(address, index);
    return getUnsafe().getInt(address + dataOffset + index * INT_SIZE);
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

  public static void free(long address) {
    getUnsafe().freeMemory(address);
  }

  public static void arraycopy(long src, int srcPos, long dest, int destPos, int length) {
    getUnsafe()
        .copyMemory(
            src + dataOffset + srcPos * INT_SIZE,
            dest + dataOffset + destPos * INT_SIZE,
            length * INT_SIZE);
  }
}
