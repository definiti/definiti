package definiti.root.cache

import definiti.root.config.Configuration

import scala.concurrent.duration._

class Cache(configuration: Configuration) {
  private val cacheDestination = configuration.workingDirectory.resolve("cache")

  lazy val version: CacheConfig[String] = new CacheConfig[String]("version", expiration = 1.hour, cacheDestination)

  lazy val projectHash: CacheConfig[String] = new CacheConfig[String]("projectHash", expiration = 365.days, cacheDestination)
}