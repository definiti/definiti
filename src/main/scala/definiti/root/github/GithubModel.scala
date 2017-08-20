package definiti.root.github

import spray.json.RootJsonFormat

object  GithubModel {
  import spray.json.DefaultJsonProtocol._

  case class Tag(name: String, zipball_url: String, tarball_url: String, commit: Commit)

  case class Commit(sha: String, url: String)

  implicit val commitFormat: RootJsonFormat[Commit] = jsonFormat2(Commit)
  implicit val commitFormatSeq: RootJsonFormat[Seq[Commit]] = seqFormat[Commit]
  implicit val tagFormat: RootJsonFormat[Tag] = jsonFormat4(Tag)
  implicit val tagFormatSeq: RootJsonFormat[Seq[Tag]] = seqFormat[Tag]
}

