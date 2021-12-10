package gc_less.tpl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TemplateArrayListTests {
  @Test
  public void testCreate() {
    // WHEN
    long arrayList = TemplateArrayList.allocate(10);

    // THEN
    assertEquals(0, TemplateArrayList.getLength(arrayList));
    assertEquals(10, TemplateArrayList.getCapacity(arrayList));

    TemplateArrayList.free(arrayList);
  }

  @Test
  public void testLegalAccess() {
    // GIVEN
    long arrayList = TemplateArrayList.allocate(10);

    // WHEN
    arrayList = insertData(arrayList);

    // THEN
    assertEquals(3, TemplateArrayList.getLength(arrayList));
    assertEquals(10, TemplateArrayList.getCapacity(arrayList));

    assertEquals(111, TemplateArrayList.get(arrayList, 0));
    assertEquals(222, TemplateArrayList.get(arrayList, 1));
    assertEquals(333, TemplateArrayList.get(arrayList, 2));

    TemplateArrayList.free(arrayList);
  }

  @Test
  public void testLegalAccessWithReallocations() {
    // GIVEN
    long arrayList = TemplateArrayList.allocate(1);

    // WHEN
    arrayList = insertData(arrayList);

    // THEN
    assertEquals(3, TemplateArrayList.getLength(arrayList));
    assertEquals(4, TemplateArrayList.getCapacity(arrayList));

    assertEquals(111, TemplateArrayList.get(arrayList, 0));
    assertEquals(222, TemplateArrayList.get(arrayList, 1));
    assertEquals(333, TemplateArrayList.get(arrayList, 2));

    TemplateArrayList.free(arrayList);
  }

  private long insertData(long arrayList) {
    arrayList = TemplateArrayList.add(arrayList, 111);
    arrayList = TemplateArrayList.add(arrayList, 222);
    arrayList = TemplateArrayList.add(arrayList, 333);
    return arrayList;
  }

  @Test
  public void testIllegalAccess() {
    // GIVEN
    long arrayList = TemplateArrayList.allocate(10);

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

    TemplateArrayList.free(arrayList);
  }
}
