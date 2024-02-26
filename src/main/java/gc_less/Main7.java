package gc_less;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Main7 {
  public static final int N = 10_000_000;

  public static void main(String[] args) {
    int i = 0;
    for (int n=0; n<N;n++) {
      try (Arena arena = Arena.ofConfined()) {
        MemorySegment allocate = arena.allocate(10);
        i += allocate.get(ValueLayout.JAVA_BYTE, 7);
      }
    }
    System.out.println(i);
  }
}
