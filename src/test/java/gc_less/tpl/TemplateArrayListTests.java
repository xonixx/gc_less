package gc_less.tpl;

import gc_less.Cleaner;
import gc_less.MemoryTrackingAssertNoLeaks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateArrayListTests extends MemoryTrackingAssertNoLeaks {
  @Test
  public void testCreate() {
    try (Cleaner cleaner = new Cleaner()) {
      // WHEN
      long arrayList = TemplateArrayList.allocate(cleaner, 10);

      // THEN
      assertEquals(0, TemplateArrayList.getLength(arrayList));
      assertEquals(10, TemplateArrayList.getCapacity(arrayList));
    }
  }

  @Test
  public void testLegalAccess() {
    try (Cleaner cleaner = new Cleaner()) {
      // GIVEN
      long arrayList = TemplateArrayList.allocate(cleaner, 10);

      // WHEN
      arrayList = insertData(arrayList);

      // THEN
      assertEquals(3, TemplateArrayList.getLength(arrayList));
      assertEquals(10, TemplateArrayList.getCapacity(arrayList));

      assertEquals(111, TemplateArrayList.get(arrayList, 0));
      assertEquals(222, TemplateArrayList.get(arrayList, 1));
      assertEquals(333, TemplateArrayList.get(arrayList, 2));
    }
  }

  @Test
  public void testLegalAccessWithReallocations() {
    try (Cleaner cleaner = new Cleaner()) {

      // GIVEN
      long arrayList = TemplateArrayList.allocate(cleaner, 1);

      // WHEN
      arrayList = insertData(arrayList);

      // THEN
      assertEquals(3, TemplateArrayList.getLength(arrayList));
      assertEquals(4, TemplateArrayList.getCapacity(arrayList));

      assertEquals(111, TemplateArrayList.get(arrayList, 0));
      assertEquals(222, TemplateArrayList.get(arrayList, 1));
      assertEquals(333, TemplateArrayList.get(arrayList, 2));
    }
  }

  @Test
  public void testInsert() {
    try (Cleaner cleaner = new Cleaner()) {
      // GIVEN
      long arrayList = TemplateArrayList.allocate(cleaner, 10);

      // WHEN
      arrayList = TemplateArrayList.add(arrayList, 111);
      arrayList = TemplateArrayList.add(arrayList, 222);
      arrayList = TemplateArrayList.add(arrayList, 1, 333);

      // THEN
      assertEquals(3, TemplateArrayList.getLength(arrayList));
      assertEquals(10, TemplateArrayList.getCapacity(arrayList));

      assertEquals(111, TemplateArrayList.get(arrayList, 0));
      assertEquals(333, TemplateArrayList.get(arrayList, 1));
      assertEquals(222, TemplateArrayList.get(arrayList, 2));
    }
  }

  @Test
  public void testRemove() {
    try (Cleaner cleaner = new Cleaner()) {
      // GIVEN
      long arrayList = TemplateArrayList.allocate(cleaner, 10);
      arrayList = insertData(arrayList);

      // WHEN
      long removed = TemplateArrayList.remove(arrayList, 1);

      // THEN
      assertEquals(222, removed);
      assertEquals(2, TemplateArrayList.getLength(arrayList));
      assertEquals(10, TemplateArrayList.getCapacity(arrayList));

      assertEquals(111, TemplateArrayList.get(arrayList, 0));
      assertEquals(333, TemplateArrayList.get(arrayList, 1));
    }
  }

  private long insertData(long arrayList) {
    arrayList = TemplateArrayList.add(arrayList, 111);
    arrayList = TemplateArrayList.add(arrayList, 222);
    arrayList = TemplateArrayList.add(arrayList, 333);
    return arrayList;
  }

  @Test
  public void testIllegalAccess() {
    try (Cleaner cleaner = new Cleaner()) {
      // GIVEN
      long arrayList = TemplateArrayList.allocate(cleaner, 10);

      // WHEN
      arrayList = insertData(arrayList);

      // THEN
      final long arrayListFinal = arrayList;
      for (int illegalIndex : new int[] {-1, 10}) {
        assertThrows(
            IndexOutOfBoundsException.class,
            () -> Long.hashCode(TemplateArrayList.get(arrayListFinal, illegalIndex)));
        assertThrows(
            IndexOutOfBoundsException.class,
            () -> TemplateArrayList.set(arrayListFinal, illegalIndex, 7));
      }

      for (int legalIndex : new int[] {0, 1, 2}) {
        assertDoesNotThrow(() -> Long.hashCode(TemplateArrayList.get(arrayListFinal, legalIndex)));
        assertDoesNotThrow(() -> TemplateArrayList.set(arrayListFinal, legalIndex, 7));
      }
    }
  }

  @Test
  public void testIllegalAccessInsert() {
    try (Cleaner cleaner = new Cleaner()) {
      // GIVEN
      long arrayList = TemplateArrayList.allocate(cleaner, 10);

      // WHEN
      arrayList = insertData(arrayList);

      // THEN
      final long arrayListFinal = arrayList;
      assertThrows(
          IndexOutOfBoundsException.class,
          () -> Long.hashCode(TemplateArrayList.add(arrayListFinal, 5, 7)));
    }
  }
}
