package definiti.root

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import definiti.root.api.ApiDownloader
import definiti.root.cache.Cache
import definiti.root.config.Configuration
import definiti.root.project._
import definiti.root.utils.{LoggerContainer, StreamUtils, Task}

import scala.concurrent.{Future, Promise}

class ProjectProcessor(
  configuration: Configuration,
  cache: Cache
)(
  implicit actorSystem: ActorSystem
) extends LoggerContainer {
  implicit private val actorMaterializer = ActorMaterializer()
  implicit private val executionContext = actorSystem.dispatcher

  private val apiDownloader = new ApiDownloader(configuration, cache)
  private val projectBuilder = new ProjectBuilder(configuration, cache)
  private val projectLauncher = new ProjectLauncher(configuration)

  def run(): Future[Unit] = {
    val promise = Promise[Unit]()
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      val apiDownloadTask = Task("Download Definiti API", apiDownloader.load()).toFlow
      val projectBuildingTask = Task("Building Definiti executable", projectBuilder.build()).toFlow
      val projectLauncherTask = Task("Launch project", projectLauncher.launch()).toFlow

      val in = Source.single[Unit]()
      val out = Sink.foreach[Unit](_ => promise.success())

      //@formatter:off
      in ~>
        apiDownloadTask ~>
        projectBuildingTask ~>
        projectLauncherTask ~>
        out
      //@formatter:on

      ClosedShape
    }).run()
    promise.future
  }
}
