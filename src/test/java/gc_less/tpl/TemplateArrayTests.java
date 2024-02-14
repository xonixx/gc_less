package gc_less.tpl;

import gc_less.Cleaner;
import gc_less.MemoryTrackingAssertNoLeaks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateArrayTests extends MemoryTrackingAssertNoLeaks {
  @Test
  public void testCreate() {
    try (Cleaner cleaner = new Cleaner()) {
      long array = TemplateArray.allocate(cleaner, 10);
      assertEquals(10, TemplateArray.getLength(array));
      assertEquals(0, TemplateArray.get(array, 0));
      assertEquals(0, TemplateArray.get(array, 7));
      assertEquals(0, TemplateArray.get(array, 9));
    }
  }

  @Test
  public void testLegalAccess() {
    try (Cleaner cleaner = new Cleaner()) {
      // GIVEN
      long array = TemplateArray.allocate(cleaner, 10);

      // WHEN
      TemplateArray.set(array, 0, 111);
      TemplateArray.set(array, 7, 222);
      TemplateArray.set(array, 9, 333);

      // THEN
      assertEquals(111, TemplateArray.get(array, 0));
      assertEquals(222, TemplateArray.get(array, 7));
      assertEquals(333, TemplateArray.get(array, 9));
    }
  }

  @Test
  public void testIllegalAccess() {
    try (Cleaner cleaner = new Cleaner()) {
      long array = TemplateArray.allocate(cleaner, 10);

      assertThrows(
          IndexOutOfBoundsException.class, () -> Long.hashCode(TemplateArray.get(array, -1)));
      assertThrows(IndexOutOfBoundsException.class, () -> TemplateArray.set(array, -1, 7));

      assertDoesNotThrow(() -> Long.hashCode(TemplateArray.get(array, 5)));
      assertDoesNotThrow(() -> TemplateArray.set(array, 5, 7));

      assertThrows(
          IndexOutOfBoundsException.class, () -> Long.hashCode(TemplateArray.get(array, 10)));
      assertThrows(IndexOutOfBoundsException.class, () -> TemplateArray.set(array, -1, 10));
    }
  }

  @Test
  public void testArraycopy() {
    try (Cleaner cleaner = new Cleaner()) {
      // GIVEN
      long array1 = TemplateArray.allocate(cleaner, 10);
      long array2 = TemplateArray.allocate(cleaner, 20);
      TemplateArray.set(array1, 0, 111);
      TemplateArray.set(array1, 7, 222);
      TemplateArray.set(array1, 9, 333);

      // WHEN
      TemplateArray.arraycopy(array1, 0, array2, 10, 10);

      // THEN
      assertEquals(0, TemplateArray.get(array2, 0));
      assertEquals(0, TemplateArray.get(array2, 7));
      assertEquals(0, TemplateArray.get(array2, 9));
      assertEquals(111, TemplateArray.get(array2, 10));
      assertEquals(222, TemplateArray.get(array2, 17));
      assertEquals(333, TemplateArray.get(array2, 19));
    }
  }
}
