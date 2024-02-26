package gc_less;

import gc_less.no_unsafe.NativeMem;

import java.lang.foreign.MemorySegment;

public class Main5 {
  public static void main(String[] args){
    while (true) {
      MemorySegment mem = NativeMem.malloc(10);
      NativeMem.free(mem);
    }
  }
}
