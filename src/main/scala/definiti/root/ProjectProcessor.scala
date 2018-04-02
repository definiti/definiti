package definiti.root

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.GraphDSL.Implicits._
import akka.stream.scaladsl.{GraphDSL, RunnableGraph, Sink, Source}
import akka.stream.{ActorMaterializer, ClosedShape}
import com.typesafe.scalalogging.Logger
import definiti.root.config.Configuration
import definiti.root.dependencies.DependenciesService
import definiti.root.project._
import definiti.root.utils.Task

import scala.concurrent.{Future, Promise}

class ProjectProcessor(configuration: Configuration)(implicit actorSystem: ActorSystem) {
  implicit private val actorMaterializer = ActorMaterializer()
  implicit private val executionContext = actorSystem.dispatcher
  private implicit val logger = Logger(getClass)

  private val dependenciesService = new DependenciesService(configuration)
  private val projectLauncher = new ProjectLauncher(configuration)

  def run(): Future[Unit] = {
    val promise = Promise[Unit]()
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      val dependenciesLoadingTask = Task("Download dependencies", dependenciesService.load()).toFlow
      val projectLauncherTask = Task("Launch project", projectLauncher.launch()).toFlow

      val in = Source.single[Unit]()
      val out = Sink.foreach[Unit](_ => promise.success())

      //@formatter:off
      in ~>
        dependenciesLoadingTask ~>
        projectLauncherTask ~>
      out
      //@formatter:on

      ClosedShape
    }).run()
    promise.future
  }
}
