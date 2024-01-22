package gc_less.no_unsafe.tpl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

public class TemplateArrayTests {
  @Test
  public void testCreate() {
    try (Arena arena = Arena.ofShared()) {
      MemorySegment array = TemplateArray.allocate(arena, 10);
      assertEquals(10, TemplateArray.getLength(array));
      assertEquals(0, TemplateArray.get(array, 0));
      assertEquals(0, TemplateArray.get(array, 7));
      assertEquals(0, TemplateArray.get(array, 9));
    }
  }

  @Test
  public void testLegalAccess() {
    try (Arena arena = Arena.ofShared()) {
      MemorySegment array = TemplateArray.allocate(arena, 10);
      TemplateArray.set(array, 0, 111);
      TemplateArray.set(array, 7, 222);
      TemplateArray.set(array, 9, 333);
      assertEquals(111, TemplateArray.get(array, 0));
      assertEquals(222, TemplateArray.get(array, 7));
      assertEquals(333, TemplateArray.get(array, 9));
    }
  }

  @Test
  public void testIllegalAccess() {
    try (Arena arena = Arena.ofShared()) {
      MemorySegment array = TemplateArray.allocate(arena, 10);

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
    try (Arena arena = Arena.ofShared()) {
      // GIVEN
      MemorySegment array1 = TemplateArray.allocate(arena, 10);
      MemorySegment array2 = TemplateArray.allocate(arena, 20);
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
