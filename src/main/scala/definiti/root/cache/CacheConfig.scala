package definiti.root.cache

import java.nio.charset.StandardCharsets
import java.nio.file.Path

import better.files.File
import definiti.root.utils.DateUtils

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

class CacheConfig[A: CacheFormat](name: String, expiration: FiniteDuration, destination: Path) {
  private val cacheFormat = implicitly[CacheFormat[A]]
  private val cacheFile = File(destination.resolve(name))
  implicit private val charset = StandardCharsets.UTF_8

  def read()(implicit executionContext: ExecutionContext): Future[Option[A]] = Future {
    if (cacheFile.exists) {
      readFile()
    } else {
      None
    }
  }

  private def readFile(): Option[A] = {
    cacheFile.lines(StandardCharsets.UTF_8).toList match {
      case updateDateIso :: content if DateUtils.isDateTime(updateDateIso) =>
        convertIfNotExpired(updateDateIso, content)
      case _ =>
        None
    }
  }

  private def convertIfNotExpired(updateDateIso: String, content: List[String]): Option[A] = {
    val updateDate = DateUtils.asDateTime(updateDateIso)
    if (DateUtils.isExpired(updateDate, expiration)) {
      None
    } else {
      Some(cacheFormat.read(content.mkString("")))
    }
  }

  def update(value: A)(implicit executionContext: ExecutionContext): Future[Unit] = Future {
    val updateDate = DateUtils.nowAsIsoString
    val stringValue = cacheFormat.write(value)
    cacheFile.parent.createDirectories()
    cacheFile.write(updateDate + "\n" + stringValue)
  }
}