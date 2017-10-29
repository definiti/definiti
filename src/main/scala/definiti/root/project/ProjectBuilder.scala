package definiti.root.project

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import akka.actor.ActorSystem
import definiti.root.cache.Cache
import definiti.root.config.{Configuration, DependencyEntry}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.sys.process.Process

class ProjectBuilder(configuration: Configuration, cache: Cache)(implicit actorSystem: ActorSystem) {
  import ProjectBuilder._

  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def build(): Future[Unit] = {
    cache.projectHash.read()
      .flatMap {
        case Some(hash) if hash == configuration.hash => Future.successful(())
        case _ =>
          processBuild()
            .map { _ => cache.projectHash.update(configuration.hash) }
      }
  }

  private def processBuild(): Future[Unit] = {
    val createSbtFileFuture = Future(configuration.dependencies.map(createSbtFile))
    val createPluginFileFuture = Future(createPluginFile())
    val createPropertiesFileFuture = Future(createPropertiesFile())
    val projectBuilding = for {
      _ <- createSbtFileFuture
      _ <- createPluginFileFuture
      _ <- createPropertiesFileFuture
    } yield Unit
    projectBuilding.flatMap(_ => assemble())
  }

  private def createSbtFile(dependencies: Seq[DependencyEntry]) = {
    val buildFileContent = buildSbt(dependencies)
    val file = configuration.workingDirectory.resolve(projectDirectory).resolve(buildFile)
    Files.createDirectories(file.getParent)
    Files.write(file, Seq(buildFileContent).asJava, StandardCharsets.UTF_8)
  }

  private def buildSbt(dependencies: Seq[DependencyEntry]): String = {
    s"""
       |scalaVersion := "2.12.1"
       |
       |libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.7"
       |${dependencies.map(buildSbtDependencyEntry).mkString("\n")}
       |
       |enablePlugins(JavaAppPackaging)
       |
       |mainClass in Compile := Some("definiti.core.Boot")
     """.stripMargin
  }

  private def buildSbtDependencyEntry(dependencyEntry: DependencyEntry): String = {
    s"""libraryDependencies += "${dependencyEntry.groupId}" %% "${dependencyEntry.artifactId}" % "${dependencyEntry.version}""""
  }

  private def createPluginFile() = {
    val fileContent = """addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.0")"""
    val file = configuration.workingDirectory.resolve(projectDirectory).resolve(pluginFile)
    Files.createDirectories(file.getParent)
    Files.write(file, Seq(fileContent).asJava, StandardCharsets.UTF_8)
  }

  private def createPropertiesFile() = {
    val fileContent =
      """
        |sbt.version = 1.0.2
        |""".stripMargin
    val file = configuration.workingDirectory.resolve(projectDirectory).resolve(propertiesFile)
    Files.createDirectories(file.getParent)
    Files.write(file, Seq(fileContent).asJava, StandardCharsets.UTF_8)
  }

  private def assemble(): Future[Unit] = Future {
    val projectPath = configuration.workingDirectory.resolve(projectDirectory)
    Process(s"sbt stage", projectPath.toFile).!
  }
}

object ProjectBuilder {
  val projectDirectory = "project"

  val buildFile = "build.sbt"

  val pluginFile = "project/plugins.sbt"

  val propertiesFile = "project/build.properties"
}