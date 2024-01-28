package gc_less;

import static gc_less.Unsafer.getUnsafe;

public class Allocator implements AutoCloseable {

  private static final Allocator instance = new Allocator();
  private static final long stack = LongStack.allocate(10);

  private Allocator() {}

  public static Allocator newFrame() {
    long locals = LongStack.allocate(10);
    LongStack.push(stack, locals);
    return instance;
  }

  public void registerForCleanup(long ref) {
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
      Unsafer.freeMem(addr);

      System.out.println("Freeing local ref  " + ref + "...");
      Ref.free(ref);
    }
    System.out.println("Freeing locals     " + locals + "...");
    Unsafer.freeMem(locals);
  }
}
