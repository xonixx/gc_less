package gc_less;

import static gc_less.Cleaner.locals;

public class Main4 {
  public static void main(String[] args) throws InterruptedException {
    while (true) {
      doIt();
      Thread.sleep(1000);
    }
  }

  private static void doIt() {
    long stack;
//    int N = 1000_000_000;
    int N = 500_000_000;
    try (Cleaner ignored = locals().add(stack = IntStack.allocate(N + 1))) {
      for (int i = 0; i < N; i++) {
        stack = IntStack.push(stack, i);
      }
      long sum = 0;
      while (IntStack.getLength(stack) > 0) {
        sum += IntStack.pop(stack);
      }
      System.out.println(sum);
    }
    System.out.println("Done iteration...");
  }
}
