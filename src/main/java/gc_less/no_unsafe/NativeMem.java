package gc_less.no_unsafe;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

public class NativeMem {
  private static final Linker linker = Linker.nativeLinker();
  private static final MethodHandle malloc =
      linker.downcallHandle(
          linker.defaultLookup().find("malloc").orElseThrow(),
          FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
  private static final MethodHandle realloc =
      linker.downcallHandle(
          linker.defaultLookup().find("realloc").orElseThrow(),
          FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));
  private static final MethodHandle free =
      linker.downcallHandle(
          linker.defaultLookup().find("free").orElseThrow(),
          FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

  public static MemorySegment malloc(long bytes) {
    MemorySegment pointer;
    try {
      // Invoke malloc(), which returns a pointer
      // The size of the memory segment created by malloc() is zero bytes!
      pointer = (MemorySegment) malloc.invokeExact(bytes);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    return pointer.reinterpret(bytes);
  }

  public static MemorySegment realloc(MemorySegment pointer, long bytes) {
    MemorySegment pointer1;
    try {
      // returns a pointer
      // The size of the memory segment is zero bytes!
      pointer1 = (MemorySegment) realloc.invokeExact(pointer, bytes);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }

    return pointer1.reinterpret(bytes);
  }

  public static void free(MemorySegment pointer) {
    try {
      free.invokeExact(pointer);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
