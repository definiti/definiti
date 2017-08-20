package definiti.root.cache

trait CacheFormat[A] {
  def read(value: String): A

  def write(value: A): String
}

object CacheFormat {
  implicit val stringFormat: CacheFormat[String] = new CacheFormat[String] {
    override def read(value: String): String = value

    override def write(value: String): String = value
  }
}