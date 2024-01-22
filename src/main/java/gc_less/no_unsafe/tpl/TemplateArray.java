package gc_less.no_unsafe.tpl;

import static gc_less.TypeSizes.LONG_SIZE;

import gc_less.no_unsafe.NativeMem;
import gc_less.tpl.Type;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/** Non-resizable array (similar to arrays in Java) */
public class TemplateArray {
  private static final long lengthOffset = 0;
  //  private static final long dataOffset = lengthOffset + INT_SIZE;
  private static final long dataOffset = lengthOffset + LONG_SIZE; // because of alignment

  public static MemorySegment allocate(int length) {
    return allocate(null, length);
  }

  public static MemorySegment allocate(Arena arena, int length) {
    long bytes = dataOffset + length * Tpl.typeSize();
    MemorySegment addr = NativeMem.malloc(bytes);
    addr.fill((byte) 0);
    setLength(addr, length);
    if (arena != null) {
      addr = addr.reinterpret(arena, TemplateArray::free);
    }
    return addr;
  }

  public static void set(MemorySegment address, int index, @Type long value) {
    checkBoundaries(address, index);
    Tpl.put(address, dataOffset + index * Tpl.typeSize(), value);
  }

  public static @Type long get(MemorySegment address, int index) {
    checkBoundaries(address, index);
    return Tpl.get(address, dataOffset + index * Tpl.typeSize());
  }

  private static void checkBoundaries(MemorySegment address, int index) {
    if (index < 0 /*|| index >= getLength(address)*/) throw new IndexOutOfBoundsException();
  }

  public static int getLength(MemorySegment address) {
    return address.get(ValueLayout.JAVA_INT, 0);
  }

  private static void setLength(MemorySegment address, int length) {
    address.set(ValueLayout.JAVA_INT, 0, length);
  }

  public static void free(MemorySegment address) {
    NativeMem.free(address);
  }

  public static void arraycopy(
      MemorySegment src, int srcPos, MemorySegment dest, int destPos, int length) {
    MemorySegment.copy(
        src,
        dataOffset + srcPos * Tpl.typeSize(),
        dest,
        dataOffset + destPos * Tpl.typeSize(),
        length * Tpl.typeSize());
  }
}
