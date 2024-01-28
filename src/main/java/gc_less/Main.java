package gc_less;

public class Main {
  public static void main(String[] args) {
    long stack, stack1;
    try (Allocator allocator = new Allocator()) {
      stack = IntStack.allocate(allocator, 2);
      stack1 = LongStack.allocate(allocator, 10);

      stack = IntStack.push(stack, 1);
      stack = IntStack.push(stack, 2);
      stack = IntStack.push(stack, 3);
      stack = IntStack.push(stack, 4);
      stack = IntStack.push(stack, 5);

      main2();

      stack1 = LongStack.push(stack1, 1);
      stack1 = LongStack.push(stack1, 2);
      stack1 = LongStack.push(stack1, 3);
      stack1 = LongStack.push(stack1, 4);
      stack1 = LongStack.push(stack1, 5);

      while (IntStack.getLength(stack) > 0) {
        System.out.println(IntStack.pop(stack));
      }
      while (LongStack.getLength(stack1) > 0) {
        System.out.println(LongStack.pop(stack1));
      }
    }
  }

  public static void main2() {
    long stack, stack1;
    try (Allocator allocator = new Allocator()) {
      stack = IntStack.allocate(allocator, 2);
      stack1 = LongStack.allocate(allocator, 10);
      
      stack = IntStack.push(stack, 1);
      stack = IntStack.push(stack, 2);
      stack = IntStack.push(stack, 3);
      stack = IntStack.push(stack, 4);
      stack = IntStack.push(stack, 5);

      stack1 = LongStack.push(stack1, 1);
      stack1 = LongStack.push(stack1, 2);
      stack1 = LongStack.push(stack1, 3);
      stack1 = LongStack.push(stack1, 4);
      stack1 = LongStack.push(stack1, 5);

      while (IntStack.getLength(stack) > 0) {
        System.out.println(IntStack.pop(stack));
      }
      while (LongStack.getLength(stack1) > 0) {
        System.out.println(LongStack.pop(stack1));
      }
    }
  }
}
