package gc_less;

import static gc_less.Unsafer.getUnsafe;

/**
 * Ref is needed when we want to register some object for cleanup. But we can't simply register its
 * address, because the object can be relocated due to memory reallocation due to growing.
 */
public class Ref {
  public static long create() {
    return create(0);
  }

  public static long create(long target) {
    long ref = getUnsafe().allocateMemory(TypeSizes.LONG_SIZE);
    set(ref, target);
    return ref;
  }

  public static void set(long ref, long target) {
    getUnsafe().putLong(ref, target);
  }

  public static long get(long ref) {
    return getUnsafe().getLong(ref);
  }

  public static void free(long ref) {
    getUnsafe().freeMemory(ref);
  }
}
