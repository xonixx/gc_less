BEGIN {
  TPL_FOLDER = "src/main/java/gc_less/tpl"
  OUT_FOLDER = "src/main/java/gc_less"
  GEN["int"] = "Integer"
  GEN["long"]
  GEN["double"]
  delete Classes
  gen()
  genTypeMeta()
}
function genTypeMeta(   i,outFile,cls) {
  outFile = OUT_FOLDER "/TypeMeta.java"
  printf "" > outFile
  while (getline < (TPL_FOLDER "/TypeMeta.java")) {
    if (/package gc_less.tpl/) {
      print "package gc_less;\nimport gc_less.tpl.*;" >> outFile
    } else if (/FREE_LOGIC/) {
      for (i = 0; i in Classes; i++) {
        printf ("    %sif (typeId==%s.typeId) %s.free(pointer);\n", i > 0 ? "else " : "", cls = Classes[i], cls) >> outFile
      }
    } else print >> outFile
  }
}
function gen(   cmd,f) {
  cmd = "ls -1 " TPL_FOLDER " | grep Template"
  while (cmd | getline f) {
    processTemplate(TPL_FOLDER, f)
  }
  close(cmd)
}
function className(tplFile,   c) {
  c = tplFile
  sub(/\.java/, "", c)
  sub(/.+\//, "", c)
  return c
}

function processTemplate(tplFolder, tplFileName,   tplFile,type,outFile,line,lcfType,typeSizesDone) {
  Classes[length(Classes)] = className(tplFile = tplFolder "/" tplFileName)
  for (type in GEN) {
    outFile = OUT_FOLDER "/" tplFileName
    sub(/Template/, lcfType = lcFirst(type), outFile)

    Classes[length(Classes)] = className(outFile)

    print tplFile " -> " outFile "..."

    printf "" > outFile

    typeSizesDone = 0
    while (getline line < tplFile) {
      gsub(/Template/, lcfType, line)
      if (line ~ /^package/)
        line = "package gc_less;"
      else if (line ~ /import static gc_less\.TypeSizes\./)
        if (!typeSizesDone++)
          line = "import static gc_less.TypeSizes.*;"
        else
          continue
      else if (line ~ /import gc_less\./)
        continue
      else {
        gsub(/\@Type long/, type, line)
        if (line ~ /\@Type/) {
          print "Error at file: " tplFile ", line:"
          print line
          exit 1
        }
        gsub(/Tpl\.typeSize\(\)/, toupper(type) "_SIZE", line)
        gsub(/Tpl\.put\(/, "getUnsafe().put" lcfType "(", line)
        gsub(/Tpl\.get\(/, "getUnsafe().get" lcfType "(", line)
        gsub(/Tpl\.hashCode\(/, (GEN[type] ? GEN[type] : lcfType) ".hashCode(", line)
      }
      print line >> outFile
    }

    print "DONE."

    close(tplFile)
    close(outFile)
  }
}
function lcFirst(s) { return toupper(substr(s, 1, 1)) substr(s, 2) }

