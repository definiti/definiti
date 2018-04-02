package definiti.root.dependencies

import com.typesafe.scalalogging.Logger
import coursier.Fetch.Metadata
import coursier._
import definiti.root.config.{Configuration, JarDependencyEntry}
import definiti.root.utils.FutureUtils._
import scalaz.concurrent.Task

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

private[dependencies] class JarDependencies(configuration: Configuration) {
  private val logger = Logger(getClass)
  private val cacheDirectory = configuration.jarDependenciesDirectory.toFile

  def load()(implicit executionContext: ExecutionContext): Future[Unit] = {
    Future(fetch)
      .flatMap(initialResolution.process.run(_))
      .flatMap { resolution =>
        logger.info(s"Downloading ${resolution.artifacts.length} dependencies")
        Future.traverse(resolution.artifacts) { artifact =>
          Cache.file(artifact = artifact, cache = cacheDirectory).run
        }
      }
      .map(unit)
      .andThen {
        case Failure(error) => logger.error(error.getMessage)
      }
  }

  private def initialResolution: Resolution = {
    Resolution(
      (
        jarDependencies.map(jarDependencyEntryToCoursierDependency) :+
          versionDependency
      ).toSet
    )
  }

  private def jarDependencies: Seq[JarDependencyEntry] = {
    configuration.dependencies.collect {
      case entry: JarDependencyEntry => entry
    }
  }

  private def jarDependencyEntryToCoursierDependency(jarDependencyEntry: JarDependencyEntry): Dependency = {
    Dependency(
      module = Module(
        organization = jarDependencyEntry.groupId,
        name = jarDependencyEntry.artifactId
      ),
      version = jarDependencyEntry.version
    )
  }

  private def versionDependency: Dependency = {
    Dependency(
      module = Module(
        organization = "io.github.definiti",
        name = "api"
      ),
      version = configuration.apiVersion
    )
  }

  private def fetch: Metadata[Task] = {
    Fetch.from(repositories, Cache.fetch(cache = cacheDirectory))
  }

  private def repositories: Seq[Repository] = {
    Seq(
      Cache.ivy2Local,
      MavenRepository("https://repo1.maven.org/maven2"),
      MavenRepository("https://oss.sonatype.org/content/repositories/snapshots")
    )
  }
}