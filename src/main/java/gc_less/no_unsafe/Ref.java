package gc_less.no_unsafe;

import gc_less.TypeSizes;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Ref is needed when we want to register some object for cleanup. But we can't simply register its
 * address, because the object can be relocated due to memory reallocation due to growing.
 */
public class Ref {
  private static final long targetOffset = 0;
  private static final long typeIdOffset = targetOffset + TypeSizes.LONG_SIZE;
  public static final long totalBytes = typeIdOffset + TypeSizes.INT_SIZE;

  public static MemorySegment create(MemorySegment target, int typeId) {
    MemorySegment ref = NativeMem.malloc(totalBytes);
    set(ref, target);
    setTypeId(ref,typeId);
    return ref;
  }

  public static void set(MemorySegment ref, MemorySegment target) {
    ref.set(ValueLayout.ADDRESS_UNALIGNED, targetOffset, target);
  }

  public static long get(MemorySegment ref) {
    return ref.get(ValueLayout.JAVA_LONG_UNALIGNED, targetOffset);
  }

  private static void setTypeId(MemorySegment ref, int typeId) {
    ref.set(ValueLayout.JAVA_INT_UNALIGNED, typeIdOffset, typeId);
  }

  public static int getTypeId(MemorySegment ref) {
    return ref.get(ValueLayout.JAVA_INT_UNALIGNED, typeIdOffset);
  }

  public static void free(MemorySegment ref) {
    NativeMem.free(ref);
  }
}
