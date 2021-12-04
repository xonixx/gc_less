package gc_less;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntArrayTests {
  @Test
  public void testCreate() {
    long array = IntArray.allocate(10);
    assertEquals(10, IntArray.getLength(array));
    assertEquals(0, IntArray.get(array, 0));
    assertEquals(0, IntArray.get(array, 7));
    assertEquals(0, IntArray.get(array, 9));
    IntArray.free(array);
  }

  @Test
  public void testLegalAccess() {
    long array = IntArray.allocate(10);
    IntArray.set(array, 0, 111);
    IntArray.set(array, 7, 222);
    IntArray.set(array, 9, 333);
    assertEquals(111, IntArray.get(array, 0));
    assertEquals(222, IntArray.get(array, 7));
    assertEquals(333, IntArray.get(array, 9));
    IntArray.free(array);
  }

  @Test
  public void testIllegalAccess() {
    long array = IntArray.allocate(10);

    assertThrows(IndexOutOfBoundsException.class, () -> Integer.hashCode(IntArray.get(array, -1)));
    assertThrows(IndexOutOfBoundsException.class, () -> IntArray.set(array, -1, 7));

    assertDoesNotThrow(() -> Integer.hashCode(IntArray.get(array, 5)));
    assertDoesNotThrow(() -> IntArray.set(array, 5, 7));

    assertThrows(IndexOutOfBoundsException.class, () -> Integer.hashCode(IntArray.get(array, 10)));
    assertThrows(IndexOutOfBoundsException.class, () -> IntArray.set(array, -1, 10));

    IntArray.free(array);
  }
}
