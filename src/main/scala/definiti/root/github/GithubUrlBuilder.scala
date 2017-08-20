package definiti.root.github

import akka.http.scaladsl.model.Uri

object GithubUrlBuilder {
  private val organization = "definiti"
  private val repository = "definiti-api"

  def tagList: Uri = {
    s"https://api.github.com/repos/${organization}/${repository}/tags"
  }

  def tagZip(tagName: String): Uri = {
    s"https://github.com/${organization}/${repository}/archive/${tagName}.zip"
  }
}
