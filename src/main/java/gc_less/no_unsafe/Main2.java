package gc_less.no_unsafe;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

public class Main2 {
  public static void main(String[] args) throws Throwable {
    MemorySegment p1 = NativeMem.malloc(500_000_000L);
    p1.fill((byte) 7);
    System.out.println(p1.byteSize());

    Thread.sleep(5000);

    MemorySegment p2 = NativeMem.realloc(p1, 1000_000_000L);
    System.out.println(p2.byteSize());
    System.out.println(p2.get(ValueLayout.JAVA_BYTE, 0)); // should be 7
    p2.fill((byte) 8);

    Thread.sleep(5000);

    NativeMem.free(p2);
    System.out.println("after free");
    Thread.sleep(5000);

    System.out.println("exit");
  }
}
