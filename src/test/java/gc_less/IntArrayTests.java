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

  @Test
  public void testArraycopy() {
    // GIVEN
    long array1 = IntArray.allocate(10);
    long array2 = IntArray.allocate(20);
    IntArray.set(array1, 0, 111);
    IntArray.set(array1, 7, 222);
    IntArray.set(array1, 9, 333);

    // WHEN
    IntArray.arraycopy(array1,0, array2, 10, 10);

    // THEN
    assertEquals(0, IntArray.get(array2, 0));
    assertEquals(0, IntArray.get(array2, 7));
    assertEquals(0, IntArray.get(array2, 9));
    assertEquals(111, IntArray.get(array2, 10));
    assertEquals(222, IntArray.get(array2, 17));
    assertEquals(333, IntArray.get(array2, 19));

    IntArray.free(array1);
    IntArray.free(array2);
  }
}
