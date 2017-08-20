package definiti.root.utils

import com.typesafe.scalalogging.Logger

trait LoggerContainer {
  protected implicit val logger = Logger(getClass)
}
