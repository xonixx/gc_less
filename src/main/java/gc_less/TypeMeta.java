package gc_less;
import gc_less.tpl.*;

public class TypeMeta {
  private static int typeIdCounter = 0;
  public static int nextTypeId() { return ++typeIdCounter; }

  public static void free(long pointer, int typeId) {
    if (typeId==TemplateArray.typeId) TemplateArray.free(pointer);
    else if (typeId==LongArray.typeId) LongArray.free(pointer);
    else if (typeId==DoubleArray.typeId) DoubleArray.free(pointer);
    else if (typeId==IntArray.typeId) IntArray.free(pointer);
    else if (typeId==TemplateArrayList.typeId) TemplateArrayList.free(pointer);
    else if (typeId==LongArrayList.typeId) LongArrayList.free(pointer);
    else if (typeId==DoubleArrayList.typeId) DoubleArrayList.free(pointer);
    else if (typeId==IntArrayList.typeId) IntArrayList.free(pointer);
    else if (typeId==TemplateHashtable.typeId) TemplateHashtable.free(pointer);
    else if (typeId==LongHashtable.typeId) LongHashtable.free(pointer);
    else if (typeId==DoubleHashtable.typeId) DoubleHashtable.free(pointer);
    else if (typeId==IntHashtable.typeId) IntHashtable.free(pointer);
    else if (typeId==TemplateStack.typeId) TemplateStack.free(pointer);
    else if (typeId==LongStack.typeId) LongStack.free(pointer);
    else if (typeId==DoubleStack.typeId) DoubleStack.free(pointer);
    else if (typeId==IntStack.typeId) IntStack.free(pointer);
  }
}
