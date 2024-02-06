package gc_less.no_unsafe.tpl;

import gc_less.TypeSizes;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public final class Tpl {
  public static long typeSize() {
    return TypeSizes.LONG_SIZE;
  }

  public static void put(MemorySegment address, long offset, long value) {
    address.set(ValueLayout.JAVA_LONG_UNALIGNED, offset, value);
  }

  public static long get(MemorySegment address, long offset) {
    return address.get(ValueLayout.JAVA_LONG_UNALIGNED, offset);
  }

  public static int hashCode(long value) {
    return Long.hashCode(value);
  }
}
