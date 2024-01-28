package gc_less;


import static gc_less.TypeSizes.*;
import static gc_less.Unsafer.getUnsafe;

/** Resizable array (similar to ArrayList in Java) */
public class IntArrayList {
  public static final int typeId = TypeMeta.nextTypeId();
  private static final long lengthOffset = 0;
  private static final long refOffset = lengthOffset + INT_SIZE;
  private static final long capOffset = refOffset + LONG_SIZE;
  private static final long dataOffset = capOffset + INT_SIZE;

  public static long allocate(int initialCapacity) {
    return allocate(null, initialCapacity);
  }

  public static long allocate(Allocator allocator, int initialCapacity) {
    if (initialCapacity <= 0) throw new IllegalArgumentException("initialCapacity should be > 0");
    long bytes = dataOffset + initialCapacity * INT_SIZE;
    long addr = Unsafer.allocateMem(bytes);
    setLength(addr, 0);
    setCapacity(addr, initialCapacity);
    long ref = Ref.create(addr);
    setRef(addr, ref);
    if (allocator != null) {
      allocator.registerForCleanup(ref);
    }
    return addr;
  }

  public static void free(long address) {
    Unsafer.freeMem(address);
  }

  public static long add(long addr, int value) {
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    getUnsafe().putInt(addr + dataOffset + len * INT_SIZE, value);
    setLength(addr, ++len);
    return addr;
  }

  public static long add(long addr, int index, int value) {
    checkBoundaries(addr, index);
    int len = getLength(addr);
    addr = ensureCapacity(addr, len);
    long indexAddr = addr + dataOffset + index * INT_SIZE;
    Unsafer.getUnsafe()
        .copyMemory(indexAddr, indexAddr + INT_SIZE, (len - index) * INT_SIZE);
    getUnsafe().putInt(indexAddr, value);
    setLength(addr, ++len);
    return addr;
  }

  public static int remove(long addr, int index) {
    int result = get(addr, index);
    int len = getLength(addr);
    long indexAddr = addr + dataOffset + index * INT_SIZE;
    Unsafer.getUnsafe()
        .copyMemory(indexAddr + INT_SIZE, indexAddr, (len - index - 1) * INT_SIZE);
    setLength(addr, --len);
    return result;
  }

  private static long ensureCapacity(long addr, int len) {
    int capacity = getCapacity(addr);
    if (capacity == len) {
      setCapacity(addr, capacity = 2 * capacity);
      long newAddr = Unsafer.reallocateMem(addr, dataOffset + capacity * INT_SIZE);
      Ref.set(getRef(newAddr), newAddr);
      return newAddr;
    }
    return addr;
  }

  public static void set(long address, int index, int value) {
    checkBoundaries(address, index);
    getUnsafe().putInt(address + dataOffset + index * INT_SIZE, value);
  }

  public static int get(long address, int index) {
    checkBoundaries(address, index);
    return getUnsafe().getInt(address + dataOffset + index * INT_SIZE);
  }

  private static void checkBoundaries(long address, int index) {
    if (index < 0 || index >= getLength(address)) throw new IndexOutOfBoundsException();
  }

  public static int getLength(long address) {
    return getUnsafe().getInt(address);
  }

  private static void setLength(long address, int length) {
    getUnsafe().putInt(address, length);
  }

  public static int getCapacity(long address) {
    return getUnsafe().getInt(address + capOffset);
  }

  private static void setCapacity(long address, int capacity) {
    getUnsafe().putInt(address + capOffset, capacity);
  }

  public static long getRef(long address) {
    return getUnsafe().getLong(address + refOffset);
  }

  private static void setRef(long address, long ref) {
    getUnsafe().putLong(address + refOffset, ref);
  }
}
