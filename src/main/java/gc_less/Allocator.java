package gc_less;

import static gc_less.Unsafer.getUnsafe;

public class Allocator implements AutoCloseable {

  public static final Allocator instance = new Allocator();
  private static final long stack = LongStack.init();

  private Allocator() {}

  public static Allocator locals() {
    long locals = LongStack.init();
    LongStack.push(stack, locals);
    return instance;
  }

  public Allocator add(long address) {
    long locals = LongStack.peek(stack);
    LongStack.push(locals, address);
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
