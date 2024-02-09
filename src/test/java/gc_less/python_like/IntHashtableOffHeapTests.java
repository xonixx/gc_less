package gc_less.python_like;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class IntHashtableOffHeapTests {

  @Test
  void test1() {
    IntHashtableOffHeap h = new IntHashtableOffHeap(10, .75f);

    assertEquals(0, h.put(111, 111));
    assertEquals(0, h.put(222, 222));
    assertEquals(222, h.put(222, 22222));
    assertEquals(0, h.put(777, 777));

    assertEquals(3, h.size());
    assertEquals(111, h.get(111));
    assertEquals(22222, h.get(222));
    assertEquals(777, h.get(777));

    assertEquals(111, h.remove(111));
    assertEquals(2, h.size());
    assertEquals(0, h.get(111));
    assertEquals(22222, h.get(222));
    assertEquals(777, h.get(777));

    h.clear();
    assertEquals(0, h.size());
    assertEquals(0, h.get(111));
    assertEquals(0, h.get(222));
    assertEquals(0, h.get(777));

    for (int k = 1; k <= 100; k++) {
      assertEquals(0, h.put(k, k * 7));
    }

    assertEquals(100, h.size());

    for (int k = 1; k <= 100; k++) {
      assertEquals(k * 7, h.get(k));
    }
  }

  @Test
  void test2() {
    IntHashtable h = new IntHashtable(10, .75f);
    h.put(9, 9);
    h.put(19, 19);
    h.put(29, 29);
    assertEquals(3, h.size());
    assertEquals(9, h.get(9));
    assertEquals(19, h.get(19));
    assertEquals(29, h.get(29));
  }
}
