package gc_less.no_unsafe;

import java.lang.foreign.*;

public class Main3 {
  public static void main(String[] args) throws Throwable {
    MemorySegment p1 = NativeMem.malloc(100L);
    p1.set(ValueLayout.JAVA_BYTE, 0,(byte) 1);
    p1.set(ValueLayout.JAVA_BYTE, 99,(byte) 2);
    p1.set(ValueLayout.JAVA_BYTE, 100,(byte) 3); // java.lang.IndexOutOfBoundsException expected
  }
}
