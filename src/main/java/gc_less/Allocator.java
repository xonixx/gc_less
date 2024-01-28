package gc_less;

public class Allocator implements AutoCloseable {

  private final long stack;

  public Allocator() {
    stack = LongStack.allocate(null, 10);
    long locals = LongStack.allocate(null, 10);
    LongStack.push(stack, locals);
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
    Ref.free(LongStack.getRef(locals));
    Unsafer.freeMem(locals);
    Ref.free(LongStack.getRef(stack));
    LongStack.free(stack);
  }
}
