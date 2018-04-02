package definiti.root.config

import java.nio.file.{Path, Paths}

import com.typesafe.config.Config

class Configuration(config: Config) {
  lazy val workingDirectory: Path = Paths.get(".definiti")

  lazy val dependenciesDirectory: Path = workingDirectory.resolve("dependencies")

  lazy val jarDependenciesDirectory: Path = dependenciesDirectory.resolve("jars")

  lazy val apiVersion: String = config.getString("definiti.api.version")

  lazy val dependencies: Seq[DependencyEntry] = new DependenciesConfiguration(config).load()
}
