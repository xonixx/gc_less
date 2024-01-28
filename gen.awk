BEGIN {
  TPL_FOLDER = "src/main/java/gc_less/tpl"
  OUT_FOLDER = "src/main/java/gc_less"
  GEN["int"] = "Integer"
  GEN["long"]
  GEN["double"]
  gen()
}
function gen(   cmd,f) {
  cmd = "ls -1 " TPL_FOLDER " | grep Template"
  while (cmd | getline f) {
    processTemplate(TPL_FOLDER, f)
  }
  close(cmd)
}
function processTemplate(tplFolder, tplFileName,   tplFile,type,outFile,outFileTracked,line,line1,lcfType,typeSizesDone) {
  tplFile = tplFolder "/" tplFileName
  for (type in GEN) {
    outFile = outFileTracked = OUT_FOLDER "/" tplFileName
    sub(/Template/, lcfType = lcFirst(type), outFile)
    sub(/Template/, "Tracked" lcfType, outFileTracked)

    print tplFile " -> " outFile "..."

    printf "" > outFile
    printf "" > outFileTracked

    typeSizesDone = 0
    while (getline line < tplFile) {
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
      line1 = line
      gsub(/Template/, lcfType, line)
      print line >> outFile

      gsub(/Template/, "Tracked" lcfType, line1)
      gsub(/Unsafer\.allocateMem/, "Unsafer.allocateMemTrack", line1)
      gsub(/Unsafer\.freeMem/, "Unsafer.freeMemTrack", line1)
      print line1 >> outFileTracked
    }

    print "DONE."

    close(tplFile)
    close(outFile)
    close(outFileTracked)
  }
}
function lcFirst(s) { return toupper(substr(s, 1, 1)) substr(s, 2) }

