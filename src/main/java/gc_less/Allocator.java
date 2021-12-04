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

  public long newIntStack(int initialCapacity) {
    long stack = IntStack.allocate(initialCapacity);
    registerForCleanup(IntStack.getRef(stack));
    return stack;
  }

  public long newLongStack(int initialCapacity) {
    return LongStack.allocate(initialCapacity);
  }

  private static void registerForCleanup(long ref) {
    long locals = LongStack.peek(stack);
    LongStack.push(locals, ref);
  }

  @Override
  public void close() {
    System.out.println("Freeing...");
    long locals = LongStack.pop(stack);
    while (LongStack.getLength(locals) > 0) {
      long ref = LongStack.pop(locals);
      long addr = Ref.get(ref);

      System.out.println("Freeing local addr " + addr + "...");
      getUnsafe().freeMemory(addr);

      System.out.println("Freeing local ref  " + ref + "...");
      Ref.free(ref);
    }
    System.out.println("Freeing locals     " + locals + "...");
    getUnsafe().freeMemory(locals);
  }
}
