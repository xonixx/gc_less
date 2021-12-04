package gc_less;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LongStackTests {
  @Test
  public void test1() {
    long stack = LongStack.allocate(10);

    assertEquals(0, LongStack.getLength(stack));

    stack = LongStack.push(stack, 111);

    assertEquals(1, LongStack.getLength(stack));

    stack = LongStack.push(stack, 222);

    assertEquals(2, LongStack.getLength(stack));
    assertEquals(222, LongStack.peek(stack));
    assertEquals(222, LongStack.pop(stack));
    assertEquals(1, LongStack.getLength(stack));
    assertEquals(111, LongStack.pop(stack));
    assertEquals(0, LongStack.getLength(stack));

    LongStack.free(stack);
  }

  @Test
  public void test2() {
    long stack = LongStack.allocate(2);

    for (int i = 0; i < 10; i++) {
      stack = LongStack.push(stack, i);
    }

    assertEquals(10, LongStack.getLength(stack));
    assertTrue(LongStack.getCapacity(stack) >= 10);
    assertEquals(9, LongStack.peek(stack));

    LongStack.free(stack);
  }
}
