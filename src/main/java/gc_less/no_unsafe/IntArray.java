package gc_less.no_unsafe;

import static gc_less.TypeSizes.*;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/** Non-resizable array (similar to arrays in Java) */
public class IntArray {
  private static final long lengthOffset = 0;
  private static final long dataOffset = lengthOffset + INT_SIZE;

  public static MemorySegment allocate(int length) {
    return allocate(null, length);
  }

  public static MemorySegment allocate(Arena arena, int length) {
    long bytes = dataOffset + length * INT_SIZE;
    MemorySegment addr = NativeMem.malloc(bytes);
    addr.fill((byte) 0);
    setLength(addr, length);
    if (arena != null) {
      addr = addr.reinterpret(arena, IntArray::free);
    }
    return addr;
  }

  public static void set(MemorySegment address, int index, int value) {
    checkBoundaries(address, index);
    address.set(ValueLayout.JAVA_INT_UNALIGNED, dataOffset + index * INT_SIZE, value);
  }

  public static int get(MemorySegment address, int index) {
    checkBoundaries(address, index);
    return address.get(ValueLayout.JAVA_INT_UNALIGNED, dataOffset + index * INT_SIZE);
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
        dataOffset + srcPos * INT_SIZE,
        dest,
        dataOffset + destPos * INT_SIZE,
        length * INT_SIZE);
  }
}
