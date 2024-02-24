package gc_less;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import gc_less.no_unsafe.NativeMem;
import java.lang.foreign.MemorySegment;
import sun.misc.Unsafe;

public class AccessSpeedTest {

  //  public static final int LEN = 20_000_000;
//  public static final int LEN = 10_000_000;
  public static final int LEN = 5_000_000;
//    public static final int TIMES = 200;
    public static final int TIMES = 500;
//  public static final int TIMES = 100;

  static final int WARM_UP_ITERATIONS = 3;


  public static void main(String[] args) {
    testRead();
    testWrite();
  }

  private static void testRead() {
    for(int i = 0; i < WARM_UP_ITERATIONS; i++) {
      System.out.println("Warm up...");
      testMemRead();
      testUnsafeRead();
      testArrRead();
    }

    long t0 = System.currentTimeMillis();

    int res1 = testMemRead();
    System.out.println("res1=" + res1);

    long t1 = System.currentTimeMillis();

    int res2 = testUnsafeRead();
    System.out.println("res2=" + res2);

    long t2 = System.currentTimeMillis();

    int res3 = testArrRead();
    System.out.println("res3=" + res3);

    long t3 = System.currentTimeMillis();

    System.out.println("\n--- READ SPEED ---");
    long memTime = t1 - t0;
    System.out.println("mem: " + memTime);

    long unsTime = t2 - t1;
    System.out.println("uns: " + unsTime);

    long arrTime = t3 - t2;
    System.out.println("arr: " + arrTime);

//    System.out.println("arr faster than memSegm   : " + (1f * memTime / arrTime) + "x");
//    System.out.println("arr faster than Unsafe    : " + (1f * unsTime / arrTime) + "x");
//    System.out.println("memSegm faster than Unsafe: " + (1f * unsTime / memTime) + "x");
//    System.out.println();
  }
  private static void testWrite() {
    for(int i = 0; i < WARM_UP_ITERATIONS; i++) {
      System.out.println("Warm up...");
      testMemWrite();
      testUnsafeWrite();
      testArrWrite();
    }

    long t0 = System.currentTimeMillis();

    int res1 = testMemWrite();
    System.out.println("res1=" + res1);

    long t1 = System.currentTimeMillis();

    int res2 = testUnsafeWrite();
    System.out.println("res2=" + res2);

    long t2 = System.currentTimeMillis();

    int res3 = testArrWrite();
    System.out.println("res3=" + res3);

    long t3 = System.currentTimeMillis();

    System.out.println("\n--- WRITE SPEED ---");
    long memTime = t1 - t0;
    System.out.println("mem: " + memTime);

    long unsTime = t2 - t1;
    System.out.println("uns: " + unsTime);

    long arrTime = t3 - t2;
    System.out.println("arr: " + arrTime);

//    System.out.println("arr faster than memSegm   : " + (1f * memTime / arrTime) + "x");
//    System.out.println("arr faster than Unsafe    : " + (1f * unsTime / arrTime) + "x");
//    System.out.println("memSegm faster than Unsafe: " + (1f * unsTime / memTime) + "x");
//    System.out.println();
  }

  private static int testMemRead() {
    MemorySegment mem = NativeMem.malloc(LEN * 4);

    int res = 0;

    // fill
    for (int i = 0; i < LEN; i++) {
      mem.set(JAVA_INT, i * 4, 7);
    }

    for (int i = 0; i < TIMES; i++) {
      for (int j = 0; j < LEN; j++) {
        res += mem.get(JAVA_INT, j * 4);
      }
      for (int j = LEN; j-- > 0; ) {
        res += mem.get(JAVA_INT, j * 4);
      }
    }

    return res;
  }

  private static int testUnsafeRead() {
    Unsafe unsafe = Unsafer.getUnsafe();
    long mem = unsafe.allocateMemory(LEN * 4);

    int res = 0;

    // fill
    for (int i = 0; i < LEN; i++) {
      unsafe.putInt(mem + i * 4, 7);
    }

    for (int i = 0; i < TIMES; i++) {
      for (int j = 0; j < LEN; j++) {
        res += unsafe.getInt(mem + j * 4);
      }
      for (int j = LEN; j-- > 0; ) {
        res += unsafe.getInt(mem + j * 4);
      }
    }

    return res;
  }

  private static int testArrRead() {
    int[] arr = new int[LEN];

    int res = 0;

    // fill
    for (int i = 0; i < LEN; i++) {
      arr[i] = 7;
    }

    for (int i = 0; i < TIMES; i++) {
      for (int j = 0; j < LEN; j++) {
        res += arr[j];
      }
      for (int j = LEN; j-- > 0; ) {
        res += arr[j];
      }
    }

    return res;
  }

  private static int testMemWrite() {
    MemorySegment mem = NativeMem.malloc(LEN * 4);

    // write
    for (int i = 0; i < TIMES; i++) {
      for (int j = 0; j < LEN; j++) {
        mem.set(JAVA_INT, j * 4, i);
      }
      for (int j = LEN; j-- > 0; ) {
        mem.set(JAVA_INT, j * 4, i);
      }
    }

    int res = 0;
    for (int i = 0; i < LEN; i++) {
      res += mem.get(JAVA_INT, i * 4);
    }

    return res;
  }

  private static int testUnsafeWrite() {
    Unsafe unsafe = Unsafer.getUnsafe();
    long mem = unsafe.allocateMemory(LEN * 4);

    // write
    for (int i = 0; i < TIMES; i++) {
      for (int j = 0; j < LEN; j++) {
        unsafe.putInt(mem + j * 4, i);
      }
      for (int j = LEN; j-- > 0; ) {
        unsafe.putInt(mem + j * 4, i);
      }
    }

    int res = 0;
    for (int i = 0; i < LEN; i++) {
      res += unsafe.getInt(mem + i * 4);
    }

    return res;
  }

  private static int testArrWrite() {
    int[] arr = new int[LEN];

    // write
    for (int i = 0; i < TIMES; i++) {
      for (int j = 0; j < LEN; j++) {
        arr[j] = i;
      }
      for (int j = LEN; j-- > 0; ) {
        arr[j] = i;
      }
    }

    int res = 0;
    for (int i = 0; i < LEN; i++) {
      res += arr[i];
    }

    return res;
  }
}
