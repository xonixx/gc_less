package gc_less.tpl;

import gc_less.Allocator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateHashtableTests {
  @Test
  public void testCreate() {
    try (Allocator allocator = Allocator.newFrame()) {
      // GIVEN
      long hashTable = TemplateHashtable.allocate(allocator, 10, .75f);
      // WHEN
      hashTable = TemplateHashtable.put(hashTable, 1, 111);
      hashTable = TemplateHashtable.put(hashTable, 2, 222);
      hashTable = TemplateHashtable.put(hashTable, 3, 333);
      hashTable = TemplateHashtable.put(hashTable, 3, 444);
      hashTable = TemplateHashtable.put(hashTable, 13, 4444);
      System.out.println(TemplateHashtable.toString(hashTable));
      // THEN
      assertEquals(4, TemplateHashtable.getSize(hashTable));
    }
  }
  @Test
  public void testGet() {
    try (Allocator allocator = Allocator.newFrame()) {
      // GIVEN
      long hashTable = TemplateHashtable.allocate(allocator, 10, .75f);
      // WHEN
      hashTable = TemplateHashtable.put(hashTable, 1, 111);
      hashTable = TemplateHashtable.put(hashTable, 2, 222);
      hashTable = TemplateHashtable.put(hashTable, 3, 333);
      hashTable = TemplateHashtable.put(hashTable, 3, 444);
      hashTable = TemplateHashtable.put(hashTable, 13, 4444);
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
  }
}
