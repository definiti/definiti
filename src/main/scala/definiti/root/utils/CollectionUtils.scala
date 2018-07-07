package definiti.root.utils

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

private[root] object CollectionUtils {
  def scalaSeq[A](list: java.util.List[A]): Seq[A] = {
    if (list != null) {
      list.asScala.toList
    } else {
      Seq.empty
    }
  }
}