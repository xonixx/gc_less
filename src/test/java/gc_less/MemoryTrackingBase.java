package gc_less;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

abstract class MemoryTrackingBase {
  static {
    Unsafer.trackMemoryLeaks = true;
  }

  @BeforeEach
  void reset() {
    Unsafer.resetAllocationTracking();
  }

  protected void assertNoLeaks() {
    boolean isMemLeak = Unsafer.isMemoryLeak();
    if (isMemLeak) {
      printMemLeak();
    }
    Assertions.assertFalse(isMemLeak);
  }

  protected void assertLeaks() {
    Assertions.assertTrue(Unsafer.isMemoryLeak());
    printMemLeak();
  }

  private static void printMemLeak() {
    System.out.println("Memory leak:");
    Unsafer.getLeaks().iterator().next().printStackTrace();
  }
}
