package gc_less.tpl;

import gc_less.Allocator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateHashtableTests {
  @Test
  public void testCreate() {
    try (Allocator allocator = Allocator.newFrame()) {
      long hashTable = TemplateHashtable.allocate(allocator, 10, .75f);
      hashTable = TemplateHashtable.put(hashTable, 1, 111);
      hashTable = TemplateHashtable.put(hashTable, 2, 222);
      hashTable = TemplateHashtable.put(hashTable, 3, 333);
      hashTable = TemplateHashtable.put(hashTable, 3, 444);
      hashTable = TemplateHashtable.put(hashTable, 13, 4444);
      System.out.println(TemplateHashtable.toString(hashTable));
      assertEquals(4, TemplateHashtable.getSize(hashTable));
    }
  }
}
