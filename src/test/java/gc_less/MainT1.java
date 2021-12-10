package gc_less;

import java.util.ArrayList;

import static gc_less.Unsafer.getUnsafe;

public class MainT1 {
  public static void main(String[] args) {
    long mem = getUnsafe().allocateMemory(4);
    getUnsafe().putInt(mem + 40, 123);
    getUnsafe().freeMemory(mem);
    System.out.println(getUnsafe().getInt(mem + 40));
  }
}
