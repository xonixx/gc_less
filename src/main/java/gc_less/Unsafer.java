package gc_less;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import sun.misc.Unsafe;

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

  public static long allocateMem(long bytes) {
    return unsafe.allocateMemory(bytes);
  }

  public static void freeMem(long pointer) {
    unsafe.freeMemory(pointer);
  }

  private static final Map<Long, Exception> allocationTracking = new HashMap<>();

  public static void resetAllocationTracking() {
    allocationTracking.clear();
  }

  public static long allocateMemTrack(long bytes) {
    long pointer = allocateMem(bytes);
    allocationTracking.put(pointer, new Exception());
    return pointer;
  }

  public static void freeMemTrack(long pointer) {
    allocationTracking.remove(pointer);
    freeMem(pointer);
  }

  public static boolean isMemoryLeak() {
    return !allocationTracking.isEmpty();
  }

  public static Collection<Exception> getLeaks() {
    return allocationTracking.values();
  }
}
