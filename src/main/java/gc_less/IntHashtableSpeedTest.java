package gc_less;

import java.util.HashMap;
import java.util.Map;

public class IntHashtableSpeedTest {
  static final int N = 1000_000;
  //  static final int INITIAL_CAP = 16;
  static final int INITIAL_CAP = 2000_000;
  static final int WARM_UP_ITERATIONS = 10;

  public static void main(String[] args) {
    for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
      warmUp();
    }

    long t0 = System.currentTimeMillis();
    testJavaHashMap();
    long t1 = System.currentTimeMillis();
    testGcLessHashMap();
    long t2 = System.currentTimeMillis();
    testPythonLikeHashMap();
    long t3 = System.currentTimeMillis();

    System.out.println("Java  : " + (t1 - t0));
    System.out.println("GcLess: " + (t2 - t1));
    System.out.println("Python: " + (t3 - t2));
  }

  private static void warmUp() {
    testJavaHashMap();
    testGcLessHashMap();
    testPythonLikeHashMap();
  }

  private static void testJavaHashMap() {
    Map<Integer, Integer> javaMap = new HashMap<>(INITIAL_CAP);
    for (int i = 0; i < N; i++) {
      javaMap.put(i, i);
    }
    int tot = 0;
    for (int i = 0; i < N; i++) {
      tot += javaMap.get(i);
    }
    for (int i = 0; i < N; i++) {
      tot += javaMap.remove(i);
    }
    for (int i = 0; i < N; i++) {
      tot += javaMap.getOrDefault(i, 0);
    }
    System.out.println("tot=" + tot);
  }

  private static void testGcLessHashMap() {
    long gcLessMap = IntHashtable.allocate(null, INITIAL_CAP, .75f); // like in HashMap
    for (int i = 0; i < N; i++) {
      gcLessMap = IntHashtable.put(gcLessMap, i, i);
    }
    int tot = 0;
    for (int i = 0; i < N; i++) {
      tot += IntHashtable.get(gcLessMap, i);
    }
    for (int i = 0; i < N; i++) {
      tot += IntHashtable.remove(gcLessMap, i);
    }
    for (int i = 0; i < N; i++) {
      tot += IntHashtable.get(gcLessMap, i);
    }
    System.out.println("tot=" + tot);
  }

  private static void testPythonLikeHashMap() {
    gc_less.python_like.IntHashtable map = new gc_less.python_like.IntHashtable(INITIAL_CAP, .75f);
    for (int i = 0; i < N; i++) {
      map.put(i, i);
    }
    int tot = 0;
    for (int i = 0; i < N; i++) {
      tot += map.get(i);
    }
    for (int i = 0; i < N; i++) {
      tot += map.remove(i);
    }
    for (int i = 0; i < N; i++) {
      tot += map.get(i);
    }
    System.out.println("tot=" + tot);
  }
}
