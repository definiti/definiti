package definiti.root.config

import java.nio.file.{Path, Paths}

import com.typesafe.config.Config
import definiti.root.utils.Validation

class Configuration(config: Config) {
  lazy val dependencies: Validation[Seq[DependencyEntry]] = new DependenciesConfiguration(config).load()

  lazy val workingDirectory: Path = Paths.get(".definiti")

  lazy val projectDirectory: Path = workingDirectory.resolve("project")

  lazy val apiVersion: String = "latest"

  lazy val sourceDirectory: Path = Paths.get("src", "main", "resources", "samples", "src1")

  lazy val confFile: Path = Paths.get("definiti.conf")
}
