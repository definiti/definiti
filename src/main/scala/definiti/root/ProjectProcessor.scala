package definiti.root

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{Broadcast, GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import definiti.root.api.ApiDownloader
import definiti.root.cache.Cache
import definiti.root.config.Configuration
import definiti.root.project.{ProjectBuilder, ProjectCopier, ProjectLauncher, SbtDownloader}
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
  private val sbtDownloader = new SbtDownloader(configuration)
  private val projectBuilder = new ProjectBuilder(configuration, cache)
  private val projectLauncher = new ProjectLauncher(configuration)
  private val projectCopier = new ProjectCopier(configuration)

  def run(): Future[Unit] = {
    val promise = Promise[Unit]()
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      val apiDownloadTask = Task("Download Definiti API", apiDownloader.load()).toFlow
      val sbtDownloadTask = Task("Download SBT", sbtDownloader.load()).toFlow
      val projectBuildingTask = Task("Prepare project", projectBuilder.build()).toFlow
      val projectCopyingTask = Task("Move files to working directory", projectCopier.copyProject()).toFlow
      val projectLauncherTask = Task("Compile project", projectLauncher.launch()).toFlow

      val in = Source.single[Unit]()
      val out = Sink.foreach[Unit](_ => promise.success())

      val broadcast = builder.add(Broadcast[Unit](4))
      val merge = builder.add(StreamUtils.wait4())

      //@formatter:off
      in ~> broadcast ~> sbtDownloadTask     ~> merge.in0
            broadcast ~> apiDownloadTask     ~> merge.in1
            broadcast ~> projectBuildingTask ~> merge.in2
            broadcast ~> projectCopyingTask  ~> merge.in3
                                                merge.out ~> projectLauncherTask ~> out
      //@formatter:on

      ClosedShape
    }).run()
    promise.future
  }
}
