package gc_less;

import gc_less.python_like.IntHashtableOffHeap;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class MainHashtableComparison {
  public static final int N = 50_000_000;
  public static final int PRINT_EVERY = 1_000_000;

  public static void main(String[] args) {
    System.out.println("\n===== Unsafe-based hashtable =====");
    try (Cleaner cleaner = new Cleaner()) {
      long hashtableUnsafe = IntHashtable.allocate(cleaner, 100, .75f);
      for (int i = 0; i < N; i++) {
        if (i % PRINT_EVERY == 0) {
          System.out.println("Unsafe-based: " + i);
        }
        hashtableUnsafe = IntHashtable.put(hashtableUnsafe, i, i);
      }
    }

    System.out.println("\n===== MemorySegment-based hashtable =====");
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment hashtableMS = gc_less.no_unsafe.IntHashtable.allocate(arena, 100, .75f);
      for (int i = 0; i < N; i++) {
        if (i % PRINT_EVERY == 0) {
          System.out.println("MemorySegment-based: " + i);
        }
        hashtableMS = gc_less.no_unsafe.IntHashtable.put(hashtableMS, i, i);
      }
    }

    System.out.println("\n===== Python-based hashtable =====");
    IntHashtableOffHeap intHashtablePy = new IntHashtableOffHeap(100, .75f);
    for (int i = 0; i < N; i++) {
      if (i % PRINT_EVERY == 0) {
        System.out.println("Python-based: " + i);
      }
      intHashtablePy.put(i, i);
    }
    intHashtablePy.free();

    /*System.out.println("Done!");
    try {
      Thread.sleep(5000);
    } catch (InterruptedException ignore) {
    }*/
  }
}
