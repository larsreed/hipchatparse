package no.mesan.hipchatparse.utils

import java.io.File

/** File and directory utils. */
object FileIO {

  /** Return file contents as a single string (None on error). */
  def fromFile(name: String): Option[String] = try {
     Some(scala.io.Source.fromFile(name).mkString)
    }
    catch {
      case _: Throwable => None
    }

  case class ScanLists(dirs: Seq[String], files: Seq[String])
  object NoFiles extends ScanLists(Seq.empty[String], Seq.empty[String])

  /** Scan a directory, return list of dirs & list of files. */
  def scanDir(dir:String): ScanLists = {
    try {
      val allEntries = new File(dir).listFiles()
      val all= if (allEntries==null) Array.empty[File] else allEntries
      val (dirs,files)= (
        for (file <- all if file.isDirectory) yield file.getCanonicalPath,
        for (file <- all if file.isFile) yield file.getCanonicalPath
      )
      ScanLists(dirs, files)
    }
    catch {
      case _: Throwable => NoFiles
    }
  }

}
