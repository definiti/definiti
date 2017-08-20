package definiti.root.utils

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, StandardOpenOption}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.Logger
import spray.json.{RootJsonFormat, _}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}

class Http(implicit actorSystem: ActorSystem) {
  private implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
  private implicit val materializer = ActorMaterializer()

  private val logger = Logger(getClass)

  def jsonRequest[A: RootJsonFormat](httpRequest: HttpRequest): Future[A] = {
    Http()
      .singleRequest(httpRequest)
      .flatMap(withRedirection)
      .flatMap(httpResponse => httpResponse.entity.toStrict(1.minute))
      .map(strict => strict.data)
      .map(_.decodeString(StandardCharsets.UTF_8))
      .map(_.parseJson.convertTo[A])
  }

  def binaryRequest(httpRequest: HttpRequest, destination: Path): Future[Unit] = {
    Files.createDirectories(destination.getParent)
    Http()
      .singleRequest(httpRequest)
      .flatMap(withRedirection)
      .flatMap { httpResponse =>
        val dataBytes = httpResponse.entity.withoutSizeLimit().dataBytes
        val size: Long = httpResponse.entity.contentLengthOption.getOrElse(0)
        val outputStream = Files.newOutputStream(destination, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        var downloaded: Long = 0
        dataBytes
          .runForeach { byteString =>
            val previousPercent = if (size > 0) downloaded * 100 / size else 0
            downloaded += byteString.size
            val currentPercent = if (size > 0) downloaded * 100 / size else 0
            if (previousPercent != currentPercent) {
              logger.info(s"Downloading ${destination.getFileName}: ${currentPercent}%")
            }
            outputStream.write(byteString.toArray)
          }
          .map(_ => outputStream.close())
      }
  }

  private def withRedirection(httpResponse: HttpResponse): Future[HttpResponse] = {
    if (httpResponse.status.isRedirection()) {
      httpResponse.header[Location]
        .map(location => Http().singleRequest(HttpRequest(uri = location.uri)))
        .getOrElse(Future.successful(httpResponse))
    } else {
      Future.successful(httpResponse)
    }
  }
}
