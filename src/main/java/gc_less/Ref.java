package gc_less;

import static gc_less.Unsafer.getUnsafe;

/**
 * Ref is needed when we want to register some object for cleanup. But we can't simply register its
 * address, because the object can be relocated due to memory reallocation due to growing.
 */
public class Ref {
  private static final long targetOffset = 0;
  private static final long typeIdOffset = targetOffset + TypeSizes.LONG_SIZE;
  private static final long totalBytes = typeIdOffset + TypeSizes.INT_SIZE;

  public static long create(long target, int typeId) {
    long ref = Unsafer.allocateMem(totalBytes);
    set(ref, target);
    setTypeId(ref,typeId);
    return ref;
  }

  public static void set(long ref, long target) {
    getUnsafe().putLong(ref, target);
  }

  public static long get(long ref) {
    return getUnsafe().getLong(ref);
  }

  private static void setTypeId(long ref, int typeId) {
    getUnsafe().putInt(ref + typeIdOffset, typeId);
  }

  public static int getTypeId(long ref) {
    return getUnsafe().getInt(ref + typeIdOffset);
  }

  public static void free(long ref) {
    Unsafer.freeMem(ref);
  }
}
