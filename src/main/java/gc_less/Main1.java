package gc_less;

public class Main1 {
  public static void main(String[] args) throws Exception {
    long stack = LongStack.allocate(2);
    stack = LongStack.push(stack, 1);
    stack = LongStack.push(stack, 2);
    stack = LongStack.push(stack, 3);
    stack = LongStack.push(stack, 4);
    stack = LongStack.push(stack, 5);

    while (LongStack.getLength(stack) > 0) {
      System.out.println(LongStack.pop(stack));
    }
    LongStack.free(stack);
  }
}
