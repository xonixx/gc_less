package gc_less;

import org.junit.jupiter.api.AfterEach;

public abstract class MemoryTrackingAssertNoLeaks extends MemoryTrackingBase {
  @Override
  @AfterEach
  protected void assertNoLeaks() {
    super.assertNoLeaks();
  }
}
