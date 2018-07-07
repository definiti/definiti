package definiti.root.config

import com.typesafe.config.Config
import definiti.root.utils.{CollectionUtils, Invalid, Valid, Validation}

sealed trait DependencyEntry

case class JarDependencyEntry(groupId: String, artifactId: String, version: String) extends DependencyEntry

private[config] class DependenciesConfiguration(config: Config) {
  def load(): Seq[DependencyEntry] = {
    buildDependencies() match {
      case Valid(dependencies) => dependencies
      case Invalid(errors) => sys.error(errors.map(_.messages).mkString("\n"))
    }
  }

  private def buildDependencies(): Validation[Seq[DependencyEntry]] = {
    Validation.squash {
      CollectionUtils.scalaSeq(config.getStringList("definiti.dependencies"))
        .map(buildDependency)
    }
  }

  private def buildDependency(line: String): Validation[DependencyEntry] = {
    if (line.matches(".*\\:.*\\:.*")) {
      buildJarDependency(line)
    } else {
      Invalid(s"Invalid entry: $line")
    }
  }

  private def buildJarDependency(line: String): Validation[DependencyEntry] = {
    line.split(":").toList match {
      case group :: artifact :: version :: Nil => Valid(JarDependencyEntry(group, artifact, version))
      case rawEntry => Invalid(s"Invalid entry: $rawEntry")
    }
  }
}
