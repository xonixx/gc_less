package gc_less;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntStackTests extends MemoryTrackingAssertNoLeaks {
  @Test
  public void test1() {
    try (Allocator allocator = new Allocator()) {
      long stack = IntStack.allocate(allocator, 10);

      long ref = IntStack.getRef(stack);
      assertEquals(stack, Ref.get(ref));

      assertEquals(0, IntStack.getLength(stack));

      stack = IntStack.push(stack, 111);
      assertEquals(stack, Ref.get(ref));

      assertEquals(1, IntStack.getLength(stack));

      stack = IntStack.push(stack, 222);
      assertEquals(stack, Ref.get(ref));

      assertEquals(2, IntStack.getLength(stack));
      assertEquals(222, IntStack.peek(stack));
      assertEquals(222, IntStack.pop(stack));
      assertEquals(1, IntStack.getLength(stack));
      assertEquals(111, IntStack.pop(stack));
      assertEquals(0, IntStack.getLength(stack));
    }
    //    Ref.free(ref);
  }

  @Test
  public void test2() {
    try (Allocator allocator = new Allocator()) {
      long stack = IntStack.allocate(allocator, 2);

      long ref = IntStack.getRef(stack);
      assertEquals(stack, Ref.get(ref));

      for (int i = 0; i < 10; i++) {
        stack = IntStack.push(stack, i);
        assertEquals(stack, Ref.get(ref));
      }

      assertEquals(10, IntStack.getLength(stack));
      assertTrue(IntStack.getCapacity(stack) >= 10);
      assertEquals(9, IntStack.peek(stack));
    }
    //    Ref.free(ref);
  }
}
