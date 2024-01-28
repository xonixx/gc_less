package gc_less;


import static gc_less.TypeSizes.*;
import static gc_less.Unsafer.getUnsafe;

/** Non-resizable array (similar to arrays in Java) */
public class DoubleArray {

  public static final int typeId = TypeMeta.nextTypeId();
  private static final long lengthOffset = 0;
  private static final long dataOffset = lengthOffset + INT_SIZE;

  public static long allocate(int length) {
    return allocate(null, length);
  }

  public static long allocate(Allocator allocator, int length) {
    long bytes = dataOffset + length * DOUBLE_SIZE;
    long addr = Unsafer.allocateMem(bytes);
    getUnsafe().setMemory(addr, bytes, (byte) 0);
    setLength(addr, length);
    if (allocator != null) {
      allocator.registerForCleanup(Ref.create(addr));
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

  public static void free(long address) {
    Unsafer.freeMem(address);
  }

  public static void arraycopy(long src, int srcPos, long dest, int destPos, int length) {
    getUnsafe()
        .copyMemory(
            src + dataOffset + srcPos * DOUBLE_SIZE,
            dest + dataOffset + destPos * DOUBLE_SIZE,
            length * DOUBLE_SIZE);
  }
}
