package definiti.root.github

import java.nio.file.Path

import akka.actor.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import definiti.root.utils.Http
import spray.json._

import scala.concurrent.Future

class GithubApi(implicit actorSystem: ActorSystem) extends SprayJsonSupport with DefaultJsonProtocol {
  import GithubModel._

  private val http = new Http

  def tagList(): Future[Seq[Tag]] = {
    http.jsonRequest[Seq[Tag]](HttpRequest(
      method = HttpMethods.GET,
      uri = GithubUrlBuilder.tagList
    ))
  }

  def tagZip(tagName: String, destination: Path): Future[Unit] = {
    http.binaryRequest(HttpRequest(
      method = HttpMethods.GET,
      uri = GithubUrlBuilder.tagZip(tagName)
    ), destination)
  }
}