package definiti.root.config

import com.typesafe.config.Config
import definiti.root.utils.{CollectionUtils, Invalid, Valid, Validation}

case class DependencyEntry(groupId: String, artifactId: String, version: String)

private[config] class DependenciesConfiguration(config: Config) {
  def load(): Validation[Seq[DependencyEntry]] = buildDependencies()

  private def buildDependencies(): Validation[Seq[DependencyEntry]] = {
    Validation.squash {
      CollectionUtils.scalaSeq(config.getStringList("definiti.dependencies"))
        .map(buildDependency)
    }
  }

  private def buildDependency(line: String): Validation[DependencyEntry] = {
    line.split(" ").filter(_.nonEmpty).toList match {
      case group :: artifact :: version :: Nil => Valid(DependencyEntry(group, artifact, version))
      case rawEntry => Invalid(s"Invalid entry: $rawEntry")
    }
  }
}
