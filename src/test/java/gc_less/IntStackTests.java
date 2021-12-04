package gc_less;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntStackTests {
  @Test
  public void test1() {
    long stack = IntStack.allocate(10);

    assertEquals(0, IntStack.getLength(stack));

    stack = IntStack.push(stack, 111);

    assertEquals(1, IntStack.getLength(stack));

    stack = IntStack.push(stack, 222);

    assertEquals(2, IntStack.getLength(stack));
    assertEquals(222, IntStack.peek(stack));
    assertEquals(222, IntStack.pop(stack));
    assertEquals(1, IntStack.getLength(stack));
    assertEquals(111, IntStack.pop(stack));
    assertEquals(0, IntStack.getLength(stack));

    IntStack.free(stack);
  }

  @Test
  public void test2() {
    long stack = IntStack.allocate(2);

    for (int i = 0; i < 10; i++) {
      stack = IntStack.push(stack, i);
    }

    assertEquals(10, IntStack.getLength(stack));
    assertTrue(IntStack.getCapacity(stack) >= 10);
    assertEquals(9, IntStack.peek(stack));

    IntStack.free(stack);
  }
}
