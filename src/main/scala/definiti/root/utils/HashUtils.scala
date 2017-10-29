package definiti.root.utils

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

import com.typesafe.config.Config

import scala.collection.convert.ImplicitConversions._

object HashUtils {
  def hashConfig(config: Config): String = {
    val configAsString = config.entrySet()
      .map(_.toString)
      .toSeq
      .sortBy(s => s)
      .mkString(",")

    val hashBytes = MessageDigest.getInstance("SHA-512").digest(configAsString.getBytes(StandardCharsets.UTF_8))
    hashBytes.map("%02X" format _).mkString
  }
}
