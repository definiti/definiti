package definiti.root.dependencies

import definiti.root.config.Configuration

import scala.concurrent.{ExecutionContext, Future}

class DependenciesService(configuration: Configuration) {
  private val jarDependencies = new JarDependencies(configuration)

  def load()(implicit executionContext: ExecutionContext): Future[Unit] = {
    jarDependencies.load()
  }
}
