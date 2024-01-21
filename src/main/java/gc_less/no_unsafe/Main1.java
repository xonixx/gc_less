package gc_less.no_unsafe;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

public class Main1 {
  public static void main(String[] args) throws Throwable {
    try(Arena arena = Arena.ofShared()) {
      MemorySegment memorySegment = allocateMemory(5000_000_000_000L, arena);
      System.out.println(memorySegment.byteSize());
      Thread.sleep(5000);
    }
  }

  static MemorySegment allocateMemory(long byteSize, Arena arena) throws Throwable {

    // Obtain an instance of the native linker
    Linker linker = Linker.nativeLinker();

    // Locate the address of malloc()
    var malloc_addr = linker.defaultLookup().find("malloc").orElseThrow();

    // Create a downcall handle for malloc()
    MethodHandle malloc = linker.downcallHandle(
        malloc_addr,
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    );

    // Invoke malloc(), which returns a pointer
    MemorySegment segment = (MemorySegment) malloc.invokeExact(byteSize);

    // The size of the memory segment created by malloc() is zero bytes!
    System.out.println(
        "Size, in bytes, of memory segment created by calling malloc.invokeExact(" +
            byteSize + "): " + segment.byteSize());

    // Localte the address of free()
    var free_addr = linker.defaultLookup().find("free").orElseThrow();

    // Create a downcall handle for free()
    MethodHandle free = linker.downcallHandle(
        free_addr,
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    );

    // This reintepret method:
    // 1. Resizes the memory segment so that it's equal to byteSize
    // 2. Associates it with an existing arena
    // 3. Invokes free() to deallocate the memory allocated by malloc()
    //    when its arena is closed

    Consumer<MemorySegment> cleanup = s -> {
      try {
        free.invokeExact(s);
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    };

    return segment.reinterpret(byteSize, arena, cleanup);
  }
}
