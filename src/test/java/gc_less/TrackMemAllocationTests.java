package gc_less;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TrackMemAllocationTests {
  @BeforeEach
  void reset() {
    Unsafer.resetAllocationTracking();
  }
  
  @Test
  void testNoLeak() {
    long pointer = Unsafer.allocateMemTrack(10);
    Unsafer.freeMemTrack(pointer);
    Assertions.assertFalse(Unsafer.isMemoryLeak());
  }

  @Test
  void testLeak() {
    long ignored = Unsafer.allocateMemTrack(10);
    Assertions.assertTrue(Unsafer.isMemoryLeak());
    System.out.println("Memory leak:");
    Unsafer.getLeaks().iterator().next().printStackTrace();
  }
}
