package gc_less;

import static gc_less.Cleaner.locals;

public class Main3 {
  public static void main(String[] args) throws InterruptedException {
    while (true) {
      doIt();
      Thread.sleep(1000);
    }
  }

  private static void doIt() {
    long stack;
    try (Cleaner ignored = locals().add(stack = IntStack.allocate(1000_000_001))) {
      for (int i = 0; i < 1000_000_000; i++) {
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
