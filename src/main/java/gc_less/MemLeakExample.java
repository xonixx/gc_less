package gc_less;

import java.util.Collection;

public class MemLeakExample {
  public static void main(String[] args){
    Unsafer.trackMemoryLeaks = true;

    long mem1 = allocateMem();
    Unsafer.freeMem(mem1);

    long mem2 = allocateMem();
    // forgot to free mem2

    long mem3 = allocateMem();
    Unsafer.freeMem(mem3);

    Collection<Exception> leaks = Unsafer.getLeaks();
    if (!leaks.isEmpty()) {
      System.out.println("Memory leak detected:");
      leaks.iterator().next().printStackTrace();
    }
  }

  private static long allocateMem() {
    return Unsafer.allocateMem(10);
  }
}
