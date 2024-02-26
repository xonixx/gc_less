package gc_less;

import gc_less.no_unsafe.NativeMem;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

public class Main6 {
  public static final int N = 100_000_000;

  public static void main(String[] args) {
    int i = 0;
    for (int n=0; n<N;n++) {
      MemorySegment mem = NativeMem.malloc(10);
      i += mem.get(ValueLayout.JAVA_BYTE, 7);
      NativeMem.free(mem);
    }
    System.out.println(i);
  }
}
