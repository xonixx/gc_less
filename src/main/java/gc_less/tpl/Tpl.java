package gc_less.tpl;

import gc_less.TypeSizes;
import gc_less.Unsafer;

public final class Tpl {
  public static long typeSize() {
    return TypeSizes.LONG_SIZE;
  }

  public static void put(long address, long value) {
    Unsafer.getUnsafe().putLong(address, value);
  }

  public static long get(long address) {
    return Unsafer.getUnsafe().getLong(address);
  }
}
