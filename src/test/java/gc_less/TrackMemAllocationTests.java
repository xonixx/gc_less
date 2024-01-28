package gc_less;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TrackMemAllocationTests extends MemoryTrackingBase {
  @Test
  void testNoLeak() {
    long pointer = Unsafer.allocateMem(10);
    Unsafer.freeMem(pointer);
    Assertions.assertFalse(Unsafer.isMemoryLeak());
  }

  @Test
  void testLeak() {
    long ignored = Unsafer.allocateMem(10);
    Assertions.assertTrue(Unsafer.isMemoryLeak());
    System.out.println("Memory leak:");
    Unsafer.getLeaks().iterator().next().printStackTrace();
  }
}
