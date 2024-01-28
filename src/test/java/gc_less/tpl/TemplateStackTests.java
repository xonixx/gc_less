package gc_less.tpl;

import gc_less.Allocator;
import gc_less.MemoryTrackingAssertNoLeaks;
import gc_less.Ref;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateStackTests extends MemoryTrackingAssertNoLeaks {
  @Test
  public void test1() {
    try (Allocator allocator = new Allocator()) {
      long stack = TemplateStack.allocate(allocator, 10);

      long ref = TemplateStack.getRef(stack);
      Assertions.assertEquals(stack, Ref.get(ref));

      assertEquals(0, TemplateStack.getLength(stack));

      stack = TemplateStack.push(stack, 111);
      assertEquals(stack, Ref.get(ref));

      assertEquals(1, TemplateStack.getLength(stack));

      stack = TemplateStack.push(stack, 222);
      assertEquals(stack, Ref.get(ref));

      assertEquals(2, TemplateStack.getLength(stack));
      assertEquals(222, TemplateStack.peek(stack));
      assertEquals(222, TemplateStack.pop(stack));
      assertEquals(1, TemplateStack.getLength(stack));
      assertEquals(111, TemplateStack.pop(stack));
      assertEquals(0, TemplateStack.getLength(stack));
    }
    //    Ref.free(ref);
  }

  @Test
  public void test2() {
    try (Allocator allocator = new Allocator()) {

      long stack = TemplateStack.allocate(allocator, 2);

      long ref = TemplateStack.getRef(stack);
      assertEquals(stack, Ref.get(ref));

      for (int i = 0; i < 10; i++) {
        stack = TemplateStack.push(stack, i);
        assertEquals(stack, Ref.get(ref));
      }

      assertEquals(10, TemplateStack.getLength(stack));
      assertTrue(TemplateStack.getCapacity(stack) >= 10);
      assertEquals(9, TemplateStack.peek(stack));
    }
    //    Ref.free(ref);
  }
}
