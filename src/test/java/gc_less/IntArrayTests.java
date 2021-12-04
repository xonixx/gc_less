package gc_less;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntArrayTests {
  @Test
  public void testCreate() {
    long array = IntArray.allocate(10);
    assertEquals(10, IntArray.getLength(array));
    assertEquals(0, IntArray.get(array, 0));
    assertEquals(0, IntArray.get(array, 7));
    assertEquals(0, IntArray.get(array, 9));
  }
}
