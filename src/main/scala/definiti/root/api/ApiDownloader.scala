package definiti.root.api

import java.nio.file.Files

import akka.actor.ActorSystem
import better.files._
import definiti.root.cache.Cache
import definiti.root.config.Configuration
import definiti.root.github.GithubApi
import definiti.root.utils.FunctionUtils

import scala.concurrent.{ExecutionContextExecutor, Future}

class ApiDownloader(configuration: Configuration, cache: Cache)(implicit actorSystem: ActorSystem) {
  import ApiDownloader._

  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher

  val githubApi = new GithubApi

  def load(): Future[Unit] = {
    fetchRealVersionName()
      .flatMap { realVersion =>
        if (Files.notExists(configuration.workingDirectory.resolve(apiDirectory).resolve(realVersion))) {
          downloadVersion(realVersion)
        } else {
          Future.successful()
        }
      }
  }

  private def fetchRealVersionName(): Future[String] = {
    if (configuration.apiVersion == latestVersionName) {
      cache.version.read().flatMap {
        case Some(cachedRealVersion) =>
          Future.successful(cachedRealVersion)
        case None =>
          fetchVersionNameFromGithub()
      }

    } else {
      Future.successful(configuration.apiVersion)
    }
  }

  private def fetchVersionNameFromGithub(): Future[String] = {
    githubApi.tagList()
      .map { tagList =>
        if (tagList.nonEmpty) {
          tagList.map(_.name).max
        } else {
          defaultVersionName
        }
      }
      .map(FunctionUtils.withIdentity { versionName =>
        cache.version.update(versionName)
      })
  }

  private def downloadVersion(realVersion: String): Future[Unit] = {
    val destinationFile = File(configuration.workingDirectory.resolve(temporaryZipFile))
    val apiFile = configuration.workingDirectory.resolve(apiDirectory)
    val apiInSrcFile = File(configuration.projectDirectory.resolve("src/main/resources/api"))
    downloadZip(realVersion)
      .map(_ => destinationFile.unzipTo(apiFile))
      .map(_ => destinationFile.delete())
      .map(_ => File(apiFile.resolve(extractedZipName(realVersion))).renameTo(realVersion))
      .map { _ =>
        apiInSrcFile.parent.createDirectories()
        File(apiFile.resolve(realVersion).resolve("src"))
          .copyTo(apiInSrcFile)
      }
  }

  private def extractedZipName(realVersion: String): String = s"definiti-api-${realVersion}"

  private def downloadZip(realVersion: String): Future[Unit] = {
    githubApi.tagZip(realVersion, configuration.workingDirectory.resolve(temporaryZipFile))
  }
}

object ApiDownloader {
  val latestVersionName = "latest"

  val defaultVersionName = "0.0.0"

  val temporaryZipFile = "_definiti-api.zip"

  val apiDirectory = "api"
}