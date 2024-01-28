package gc_less;

import org.junit.jupiter.api.Test;

public class TrackMemAllocationTests extends MemoryTrackingBase {
  @Test
  void testNoLeak() {
    long pointer = Unsafer.allocateMem(10);
    Unsafer.freeMem(pointer);
    assertNoLeaks();
  }

  @Test
  void testLeak() {
    long ignored = Unsafer.allocateMem(10);
    assertLeaks();
  }
}
