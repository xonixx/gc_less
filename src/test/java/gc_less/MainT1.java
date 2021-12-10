package gc_less;

import java.util.ArrayList;

public class MainT1 {
  public static void main(String[] args) {
    ArrayList a = new ArrayList();
    a.add("hello");
    a.set(0, "hello1");
    System.out.println(a);
  }
}
