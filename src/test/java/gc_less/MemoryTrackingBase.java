package gc_less;

import org.junit.jupiter.api.BeforeEach;

abstract class MemoryTrackingBase {
  static {
    Unsafer.trackMemoryLeaks = true;
  }
  @BeforeEach
  void reset() {
    Unsafer.resetAllocationTracking();
  }
}
