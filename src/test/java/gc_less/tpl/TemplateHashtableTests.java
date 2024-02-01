package gc_less.tpl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import gc_less.Allocator;
import gc_less.MemoryTrackingAssertNoLeaks;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TemplateHashtableTests extends MemoryTrackingAssertNoLeaks {
  @Test
  public void testCreate() {
    try (Allocator allocator = new Allocator()) {
      // GIVEN
      long hashTable = TemplateHashtable.allocate(allocator, 10, .75f);
      // WHEN
      hashTable = fill(hashTable, allocator);
      System.out.println(TemplateHashtable.toString(hashTable));
      // THEN
      assertEquals(4, TemplateHashtable.getSize(hashTable));
      assertEquals(10, TemplateHashtable.getCapacity(hashTable));
    }
  }

  @Test
  public void testGet() {
    try (Allocator allocator = new Allocator()) {
      doTestGet(allocator, 10);
    }
  }

  @Test
  public void testRemove() {
    try (Allocator allocator = new Allocator()) {
      // GIVEN
      long hashTable = TemplateHashtable.allocate(allocator, 10, .75f);
      hashTable = fill(hashTable, allocator);
      assertEquals(4, TemplateHashtable.getSize(hashTable));
      assertEquals(222, TemplateHashtable.get(hashTable, 2));
      assertEquals(444, TemplateHashtable.get(hashTable, 3));
      assertEquals(4444, TemplateHashtable.get(hashTable, 13));
      // WHEN
      TemplateHashtable.remove(hashTable, 3);
      // THEN
      assertEquals(3, TemplateHashtable.getSize(hashTable));
      assertEquals(0, TemplateHashtable.get(hashTable, 3));
      // WHEN
      TemplateHashtable.remove(hashTable, 2);
      // THEN
      assertEquals(2, TemplateHashtable.getSize(hashTable));
      assertEquals(0, TemplateHashtable.get(hashTable, 2));
      // WHEN
      TemplateHashtable.remove(hashTable, 13);
      // THEN
      assertEquals(1, TemplateHashtable.getSize(hashTable));
      assertEquals(0, TemplateHashtable.get(hashTable, 13));
    }
  }

  @Test
  public void testGetWithReallocations() {
    try (Allocator allocator = new Allocator()) {
      doTestGet(allocator, 1);
    }
  }

  private void doTestGet(Allocator allocator, int initialCapacity) {
    // GIVEN
    long hashTable = TemplateHashtable.allocate(allocator, initialCapacity, .75f);
    // WHEN
    hashTable = fill(hashTable, allocator);
    System.out.println(TemplateHashtable.toString(hashTable));
    // THEN
    assertEquals(0, TemplateHashtable.get(hashTable, 0));
    assertEquals(0, TemplateHashtable.get(hashTable, 100));
    assertEquals(111, TemplateHashtable.get(hashTable, 1));
    assertEquals(222, TemplateHashtable.get(hashTable, 2));
    assertEquals(444, TemplateHashtable.get(hashTable, 3));
    assertEquals(4444, TemplateHashtable.get(hashTable, 13));
    assertEquals(0, TemplateHashtable.get(hashTable, 14));
  }

  private long fill(long hashTable, Allocator allocator) {
    hashTable = put(hashTable, 1, 111);
    hashTable = put(hashTable, 2, 222);
    hashTable = put(hashTable, 3, 333);
    hashTable = put(hashTable, 3, 444);
    hashTable = put(hashTable, 13, 4444);

    Assertions.assertTrue(TemplateHashtable.containsKey(hashTable, 1));
    Assertions.assertTrue(TemplateHashtable.containsKey(hashTable, 2));
    Assertions.assertTrue(TemplateHashtable.containsKey(hashTable, 3));
    Assertions.assertTrue(TemplateHashtable.containsKey(hashTable, 13));
    Assertions.assertFalse(TemplateHashtable.containsKey(hashTable, 100));
    Assertions.assertFalse(TemplateHashtable.containsKey(hashTable, -1));
    Assertions.assertFalse(TemplateHashtable.containsKey(hashTable, 0));

//    System.out.println(TemplateHashtable.toString(hashTable));
    long keys = TemplateHashtable.keys(hashTable, allocator);
    Assertions.assertEquals(4, TemplateArray.getLength(keys));
    Assertions.assertEquals(
        Set.of(1L, 2L, 3L, 13L),
        Set.of(
            TemplateArray.get(keys, 0),
            TemplateArray.get(keys, 1),
            TemplateArray.get(keys, 2),
            TemplateArray.get(keys, 3)));
    return hashTable;
  }

  private long put(long hashTable, long key, long val) {
    long newAddr = TemplateHashtable.put(hashTable, key, val);
    if (newAddr != hashTable) {
      System.out.printf("Reallocation occurred: %s -> %s\n", hashTable, newAddr);
    }
    return newAddr;
  }
}
