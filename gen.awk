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
function processTemplate(tplFolder, tplFileName,   tplFile,type,outFile){
  tplFile = tplFolder "/" tplFileName
  for (type in GEN) {
    outFile = OUT_FOLDER "/" tplFileName
    sub(/Template/,lcFirst(type),outFile)
    print tplFile " -> " outFile
  }
}
function lcFirst(s) { return toupper(substr(s,1,1)) substr(s,2) }

