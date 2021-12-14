package gc_less;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntArrayTests {
  @Test
  public void testCreate() {
    try (Allocator allocator = Allocator.newFrame()) {
      long array = IntArray.allocate(allocator,10);
      assertEquals(10, IntArray.getLength(array));
      assertEquals(0, IntArray.get(array, 0));
      assertEquals(0, IntArray.get(array, 7));
      assertEquals(0, IntArray.get(array, 9));
    }
  }

  @Test
  public void testLegalAccess() {
    try (Allocator allocator = Allocator.newFrame()) {
      long array = IntArray.allocate(allocator,10);
      IntArray.set(array, 0, 111);
      IntArray.set(array, 7, 222);
      IntArray.set(array, 9, 333);
      assertEquals(111, IntArray.get(array, 0));
      assertEquals(222, IntArray.get(array, 7));
      assertEquals(333, IntArray.get(array, 9));
    }
  }

  @Test
  public void testIllegalAccess() {
    try (Allocator allocator = Allocator.newFrame()) {
      long array = IntArray.allocate(allocator,10);

      for (int illegalIndex : new int[] {-1, 10}) {
        assertThrows(
            IndexOutOfBoundsException.class,
            () -> Integer.hashCode(IntArray.get(array, illegalIndex)));
        assertThrows(IndexOutOfBoundsException.class, () -> IntArray.set(array, illegalIndex, 7));
      }

      for (int legalIndex : new int[] {0, 5, 9}) {
        assertDoesNotThrow(() -> Integer.hashCode(IntArray.get(array, legalIndex)));
        assertDoesNotThrow(() -> IntArray.set(array, legalIndex, 7));
      }
    }
  }

  @Test
  public void testArraycopy() {
    try (Allocator allocator = Allocator.newFrame()) {
      // GIVEN
      long array1 = IntArray.allocate(allocator,10);
      long array2 = IntArray.allocate(allocator,20);
      IntArray.set(array1, 0, 111);
      IntArray.set(array1, 7, 222);
      IntArray.set(array1, 9, 333);

      // WHEN
      IntArray.arraycopy(array1, 0, array2, 10, 10);

      // THEN
      assertEquals(0, IntArray.get(array2, 0));
      assertEquals(0, IntArray.get(array2, 7));
      assertEquals(0, IntArray.get(array2, 9));
      assertEquals(111, IntArray.get(array2, 10));
      assertEquals(222, IntArray.get(array2, 17));
      assertEquals(333, IntArray.get(array2, 19));
    }
  }
}
