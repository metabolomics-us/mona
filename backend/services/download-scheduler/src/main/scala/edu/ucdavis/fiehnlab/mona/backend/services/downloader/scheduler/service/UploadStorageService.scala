package edu.ucdavis.fiehnlab.mona.backend.services.downloader.scheduler.service

import java.io.{IOException, RandomAccessFile}
import java.nio.file._
import java.nio.file.attribute.BasicFileAttributes

import com.typesafe.scalalogging.LazyLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

import scala.jdk.CollectionConverters._

/**
  * Stores uploaded spectra files on disk under mona.uploads, one directory per upload job. Chunks
  * are written at an explicit byte offset so a dropped connection just resends the same range and
  * the write is idempotent. The assembled file and its job directory survive a service restart,
  * which is what lets an interrupted upload resume instead of starting over
  */
@Service
class UploadStorageService extends LazyLogging {

  @Value("${mona.uploads:#{systemProperties['java.io.tmpdir']}}#{systemProperties['file.separator']}mona_uploads")
  val uploadDir: String = null

  def baseDir: Path = Paths.get(uploadDir)

  def jobDir(jobId: String): Path = baseDir.resolve(jobId)

  def storedFile(jobId: String, fileName: String): Path = jobDir(jobId).resolve(sanitize(fileName))

  /**
    * Creates the job directory so chunks have somewhere to land
    */
  def initJob(jobId: String): Unit = {
    val dir: Path = jobDir(jobId)
    if (Files.notExists(dir)) {
      Files.createDirectories(dir)
    }
  }

  /**
    * Writes a chunk at the given byte offset within the job's file and returns the new end offset.
    * Re-writing the same offset simply overwrites, so a resent chunk is harmless
    */
  def writeChunk(jobId: String, fileName: String, offset: Long, chunk: MultipartFile): Long = {
    initJob(jobId)
    val target: Path = storedFile(jobId, fileName)

    val raf: RandomAccessFile = new RandomAccessFile(target.toFile, "rw")
    try {
      raf.seek(offset)
      val bytes: Array[Byte] = chunk.getBytes
      raf.write(bytes)
      offset + bytes.length
    } finally {
      raf.close()
    }
  }

  /**
    * Current size of the assembled file, or 0 if nothing has been written yet. Used both to verify
    * a complete upload and as the resume offset a returning client reads back
    */
  def assembledSize(jobId: String, fileName: String): Long = {
    val target: Path = storedFile(jobId, fileName)
    if (Files.exists(target)) Files.size(target) else 0L
  }

  def fileExists(jobId: String, fileName: String): Boolean = {
    val target: Path = storedFile(jobId, fileName)
    Files.exists(target) && Files.isRegularFile(target)
  }

  /**
    * Sum of every regular file currently living under the uploads volume. Walked
    * once per call so the caller can gate a new upload on remaining space without recursing on
    * every chunk. Returns 0 when the base directory does not exist yet
    */
  def totalSize(): Long = {
    if (Files.notExists(baseDir)) {
      0L
    } else {
      val stream = Files.walk(baseDir)
      try {
        stream.iterator().asScala
          .filter(Files.isRegularFile(_))
          .map(Files.size(_))
          .sum
      } finally {
        stream.close()
      }
    }
  }

  /**
    * Removes a job's directory and the stored file, used when a job is deleted or rolled back
    */
  def deleteJob(jobId: String): Unit = {
    val dir: Path = jobDir(jobId)
    if (Files.exists(dir)) {
      Files.walkFileTree(dir, new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(d: Path, exc: IOException): FileVisitResult = {
          Files.delete(d)
          FileVisitResult.CONTINUE
        }
      })
    }
  }

  // Strip any path components a client might smuggle in the filename so writes stay inside the job dir
  private def sanitize(fileName: String): String = {
    val name: String = if (fileName == null || fileName.trim.isEmpty) "upload" else fileName
    Paths.get(name).getFileName.toString
  }
}
