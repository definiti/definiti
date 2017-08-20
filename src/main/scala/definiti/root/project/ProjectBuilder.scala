package definiti.root.project

import java.nio.charset.StandardCharsets
import java.nio.file.Files

import akka.actor.ActorSystem
import definiti.root.cache.Cache
import definiti.root.config.{Configuration, DependencyEntry}

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContextExecutor, Future}

class ProjectBuilder(configuration: Configuration, cache: Cache)(implicit actorSystem: ActorSystem) {
  import ProjectBuilder._

  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  def build(): Future[Unit] = Future {
    configuration.dependencies
      .map(createSbtFile)
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
       |resolvers += Resolver.mavenLocal
       |
       |mainClass in Compile := Some("definiti.core.Boot")
     """.stripMargin
  }

  private def buildSbtDependencyEntry(dependencyEntry: DependencyEntry): String = {
    s"""libraryDependencies += "${dependencyEntry.groupId}" %% "${dependencyEntry.artifactId}" % "${dependencyEntry.version}""""
  }
}

object ProjectBuilder {
  val projectDirectory = "project"

  val buildFile = "build.sbt"
}