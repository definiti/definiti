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

  def scalaSeq[A](stream: java.util.stream.Stream[A]): Seq[A] = {
    if (stream != null) {
      val buffer = ListBuffer[A]()
      stream.forEach((a) => buffer.append(a))
      buffer
    } else {
      Seq.empty
    }
  }

  def scalaSet[A](set: java.util.Set[A]): Set[A] = {
    if (set != null) {
      set.asScala.toSet
    } else {
      Set.empty
    }
  }

  def scalaEntrySet[A, B](set: java.util.Set[java.util.Map.Entry[A, B]]): Set[Entry[A, B]] = {
    scalaSet(set).map(entry => Entry(entry.getKey, entry.getValue))
  }

  def javaList[A](seq: Seq[A]): java.util.List[A] = {
    new java.util.ArrayList[A](seq.asJava)
  }
}

case class Entry[A, B](key: A, value: B) {
  def _1: A = key
  def left: A = key

  def _2: B = value
  def right: B = value
}