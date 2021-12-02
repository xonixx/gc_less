package pc_less;

import gc_less.IntStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntStackTests {
  @Test
  public void test1() {
    long stack = IntStack.init(10);

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
}
