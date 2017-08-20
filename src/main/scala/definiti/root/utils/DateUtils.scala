package definiti.root.utils

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import scala.concurrent.duration.FiniteDuration

object DateUtils {
  def isDateTime(string: String): Boolean = {
    try {
      asDateTime(string)
      true
    } catch {
      case _: DateTimeParseException => false
    }
  }

  def asDateTime(string: String): LocalDateTime = {
    LocalDateTime.parse(string)
  }

  def isExpired(date: LocalDateTime, duration: FiniteDuration): Boolean = {
    val expirationDate = date.plusSeconds(duration.toSeconds)
    LocalDateTime.now.isAfter(expirationDate)
  }

  def asIsoString(date: LocalDateTime): String = {
    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(date)
  }

  def nowAsIsoString: String = {
    asIsoString(LocalDateTime.now())
  }
}
