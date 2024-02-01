package gc_less;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import sun.misc.Unsafe;

public class Unsafer {
  private static final Unsafe unsafe = prepareUnsafe();
  public static boolean trackMemoryLeaks = false;

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
    long pointer = unsafe.allocateMemory(bytes);
    if (trackMemoryLeaks) {
      allocationTracking.put(pointer, new Exception());
    }
    return pointer;
  }

  public static long reallocateMem(long pointer, long bytes) {
    long pointer1 = unsafe.reallocateMemory(pointer, bytes);
    if (trackMemoryLeaks) {
      allocationTracking.remove(pointer);
      allocationTracking.put(pointer1, new Exception());
    }
    return pointer1;
  }

  public static void freeMem(long pointer) {
    if (trackMemoryLeaks) {
      allocationTracking.remove(pointer);
    }
    unsafe.freeMemory(pointer);
  }

  private static final Map<Long, Exception> allocationTracking = new HashMap<>();

  public static void resetAllocationTracking() {
    allocationTracking.clear();
  }

  public static boolean isMemoryLeak() {
    return !allocationTracking.isEmpty();
  }

  public static Collection<Exception> getLeaks() {
    return allocationTracking.values();
  }
}
