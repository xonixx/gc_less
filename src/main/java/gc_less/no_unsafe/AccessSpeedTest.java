package gc_less.no_unsafe;

import gc_less.Unsafer;
import sun.misc.Unsafe;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.MemorySegment;

public class AccessSpeedTest {

//  public static final int LEN = 20_000_000;
  public static final int LEN = 10_000_000;
//  public static final int TIMES = 200;
//  public static final int TIMES = 500;
  public static final int TIMES = 100;

  public static void main(String[] args) {
    testRead();
  }

  private static void testRead() {
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

    long memTime = t1 - t0;
    System.out.println("mem: " + memTime);

    long unsTime = t2 - t1;
    System.out.println("uns: " + unsTime);

    long arrTime = t3 - t2;
    System.out.println("arr: " + arrTime);

    System.out.println("arr faster than memSegm   : " + (1f * memTime / arrTime) + "x");
    System.out.println("arr faster than Unsafe    : " + (1f * unsTime / arrTime) + "x");
    System.out.println("memSegm slower than Unsafe: " + (1f * memTime / unsTime) + "x");
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
}
