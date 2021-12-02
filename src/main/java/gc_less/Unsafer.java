package gc_less;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Unsafer {
  private static final Unsafe unsafe = prepareUnsafe();

  private static Unsafe prepareUnsafe() {
    try {
      Field f = Unsafe.class.getDeclaredField("theUnsafe");
      f.setAccessible(true);
      return (Unsafe) f.get(null);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static Unsafe getUnsafe() {
    return unsafe;
  }
}
