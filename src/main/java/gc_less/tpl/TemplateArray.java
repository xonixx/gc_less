package gc_less.tpl;

import gc_less.Cleaner;
import gc_less.Ref;
import gc_less.TypeMeta;
import gc_less.Unsafer;

import static gc_less.TypeSizes.INT_SIZE;
import static gc_less.Unsafer.getUnsafe;

/** Non-resizable array (similar to arrays in Java) */
public class TemplateArray {

  public static final int typeId = TypeMeta.nextTypeId();
  private static final long lengthOffset = 0;
  private static final long dataOffset = lengthOffset + INT_SIZE;

  public static long allocate(int length) {
    return allocate(null, length);
  }

  public static long allocate(Cleaner cleaner, int length) {
    long bytes = dataOffset + length * Tpl.typeSize();
    long addr = Unsafer.allocateMem(bytes);
    getUnsafe().setMemory(addr, bytes, (byte) 0);
    setLength(addr, length);
    if (cleaner != null) {
      cleaner.registerForCleanup(Ref.create(addr, typeId));
    }
    return addr;
  }

  public static void set(long address, int index, @Type long value) {
    checkBoundaries(address, index);
    Tpl.put(address + dataOffset + index * Tpl.typeSize(), value);
  }

  public static @Type long get(long address, int index) {
    checkBoundaries(address, index);
    return Tpl.get(address + dataOffset + index * Tpl.typeSize());
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

  public static void free(long address) {
    Unsafer.freeMem(address);
  }

  public static void arraycopy(long src, int srcPos, long dest, int destPos, int length) {
    getUnsafe()
        .copyMemory(
            src + dataOffset + srcPos * Tpl.typeSize(),
            dest + dataOffset + destPos * Tpl.typeSize(),
            length * Tpl.typeSize());
  }
}
