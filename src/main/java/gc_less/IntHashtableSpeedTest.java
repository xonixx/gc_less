package gc_less;

import java.util.HashMap;
import java.util.Map;

public class IntHashtableSpeedTest {
  static final int N = 1000_000;

  public static void main(String[] args) {
    warmUp();

    long t0 = System.currentTimeMillis();
    testJavaHashMap();
    long t1 = System.currentTimeMillis();
    testGcLessHashMap();
    long t2 = System.currentTimeMillis();

    System.out.println("Java  : " + (t1 - t0));
    System.out.println("GcLess: " + (t2 - t1));
  }

  private static void warmUp() {
    testJavaHashMap();
    testGcLessHashMap();
  }

  private static void testJavaHashMap() {
    Map<Integer, Integer> javaMap = new HashMap<>();
    for (int i = 0; i < N; i++) {
      javaMap.put(i, i);
    }
    int tot = 0;
    for (int i = 0; i < N; i++) {
      tot += javaMap.get(i);
    }
    System.out.println("tot=" + tot);
  }

  private static void testGcLessHashMap() {
    long gcLessMap = IntHashtable.allocate(null, 16, .75f); // like in HashMap
    for (int i = 0; i < N; i++) {
      gcLessMap = IntHashtable.put(gcLessMap, i, i);
    }
    int tot = 0;
    for (int i = 0; i < N; i++) {
      tot += IntHashtable.get(gcLessMap, i);
    }
    System.out.println("tot=" + tot);
  }
}
