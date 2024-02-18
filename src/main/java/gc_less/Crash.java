package gc_less;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class Crash {
  public static void main(String[] args) throws Exception {
    Field f = Unsafe.class.getDeclaredField("theUnsafe");
    f.setAccessible(true);
    Unsafe unsafe = (Unsafe) f.get(null);
    unsafe.putInt(0,0);
  }
}
