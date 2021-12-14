BEGIN {
  TPL_FOLDER = "src/main/java/gc_less/tpl"
  OUT_FOLDER = "src/main/java/gc_less"
  GEN["int"]
  GEN["long"]
  GEN["double"]
  gen()
}
function gen(   cmd,f){
  cmd = "ls -1 " TPL_FOLDER " | grep Template"
  while (cmd | getline f) {
    processTemplate(TPL_FOLDER , f)
  }
  close(cmd)
}
function processTemplate(tplFolder, tplFileName,   tplFile,type,outFile,line,lcfType,typeSizesDone){
  tplFile = tplFolder "/" tplFileName
  for (type in GEN) {
    outFile = OUT_FOLDER "/" tplFileName
    sub(/Template/,lcfType=lcFirst(type),outFile)

    print tplFile " -> " outFile "..."

    printf "" > outFile

    typeSizesDone=0
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
      }
      print line >> outFile
    }

    print "DONE."

    close(tplFile)
    close(outFile)
  }
}
function lcFirst(s) { return toupper(substr(s,1,1)) substr(s,2) }

