package gc_less;

import static gc_less.Unsafer.getUnsafe;

public class Allocator implements AutoCloseable {

  private static final Allocator instance = new Allocator();
  private static final long stack = LongStack.allocate();

  private Allocator() {}

  public static Allocator newFrame() {
    long locals = LongStack.allocate();
    LongStack.push(stack, locals);
    return instance;
  }

  public static long newIntStack(int initialCapacity) {
    return IntStack.allocate(initialCapacity);
  }

  public static long newLongStack(int initialCapacity) {
    return LongStack.allocate(initialCapacity);
  }

  private Allocator add(long ref) {
    long locals = LongStack.peek(stack);
    LongStack.push(locals, ref);
    return this;
  }

  @Override
  public void close() {
    System.out.println("Freeing...");
    long locals = LongStack.pop(stack);
    while (LongStack.getLength(locals) > 0) {
      long addr = LongStack.pop(locals);
      System.out.println("Freeing local " + addr + "...");
      getUnsafe().freeMemory(addr);
    }
    System.out.println("Freeing locals " + locals + "...");
    getUnsafe().freeMemory(locals);
  }
}
