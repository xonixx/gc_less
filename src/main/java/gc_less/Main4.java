package gc_less;

public class Main4 {
  public static void main(String[] args) throws InterruptedException {
    while (true) {
      doIt();
      Thread.sleep(1000);
    }
  }

  private static void doIt() {
    //    int N = 1000_000_000;
    int N = 500_000_000;
    //    int N = 100_000_000;
    try (Allocator allocator = new Allocator()) {
      long stack = IntStack.allocate(allocator, 10);
      for (int i = 0; i < N; i++) {
        long oldStack = stack;
        stack = IntStack.push(stack, i);
        if (oldStack != stack) {
          System.out.println("Reallocation occurred.");
        }
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
