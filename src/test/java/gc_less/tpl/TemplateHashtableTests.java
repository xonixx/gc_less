package gc_less.tpl;

import gc_less.Allocator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TemplateHashtableTests {
  @Test
  public void testCreate() {
    try (Allocator allocator = Allocator.newFrame()) {
      long hashTable = TemplateHashtable.allocate(allocator, 10, .75f);
      System.out.println(TemplateHashtable.toString(hashTable));
    }
  }
}
