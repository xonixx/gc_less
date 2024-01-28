package gc_less.tpl;

public class TypeMeta {
  private static int typeIdCounter = 0;
  public static int nextTypeId() { return ++typeIdCounter; }

  public void free(long pointer, int typeId) {
    // FREE_LOGIC
  }
}
