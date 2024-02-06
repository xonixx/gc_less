package gc_less.no_unsafe;

import static gc_less.TypeSizes.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/** Non-resizable array (similar to arrays in Java) */
public class DoubleArray {
  private static final long lengthOffset = 0;
  private static final long dataOffset = lengthOffset + INT_SIZE;

  public static MemorySegment allocate(int length) {
    return allocate(null, length);
  }

  public static MemorySegment allocate(Arena arena, int length) {
    long bytes = dataOffset + length * DOUBLE_SIZE;
    MemorySegment addr = NativeMem.malloc(bytes);
    addr.fill((byte) 0);
    setLength(addr, length);
    if (arena != null) {
      addr = addr.reinterpret(arena, DoubleArray::free);
    }
    return addr;
  }

  public static void set(MemorySegment address, int index, double value) {
    checkBoundaries(address, index);
    address.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, dataOffset + index * DOUBLE_SIZE, value);
  }

  public static double get(MemorySegment address, int index) {
    checkBoundaries(address, index);
    return address.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, dataOffset + index * DOUBLE_SIZE);
  }

  private static void checkBoundaries(MemorySegment address, int index) {
    if (index < 0 /*|| index >= getLength(address)*/) throw new IndexOutOfBoundsException();
  }

  public static int getLength(MemorySegment address) {
    return address.get(ValueLayout.JAVA_INT_UNALIGNED, 0);
  }

  private static void setLength(MemorySegment address, int length) {
    address.set(ValueLayout.JAVA_INT_UNALIGNED, 0, length);
  }

  public static void free(MemorySegment address) {
    NativeMem.free(address);
  }

  public static void arraycopy(
      MemorySegment src, int srcPos, MemorySegment dest, int destPos, int length) {
    MemorySegment.copy(
        src,
        dataOffset + srcPos * DOUBLE_SIZE,
        dest,
        dataOffset + destPos * DOUBLE_SIZE,
        length * DOUBLE_SIZE);
  }
}
