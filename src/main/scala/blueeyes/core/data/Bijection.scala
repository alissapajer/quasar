package blueeyes.core.data

import blueeyes.core.http.MimeType

trait Bijection[T, S] extends Function1[T, S] { self =>
  def apply(t: T): S
  
  def unapply(s: S): T
  
  def inverse: Bijection[S, T] = new Bijection[S, T] {
    def apply(s: S): T   = self.unapply(s)
    def unapply(t: T): S = self.apply(t)
  }
}

trait DataTranscoder[T, S] {
  def transcode: Bijection[T, S]
  def mimeType: MimeType;
}

class DataTranscoderImpl[T, S](val transcode: Bijection[T, S], val mimeType: MimeType) extends DataTranscoder[T, S]

import blueeyes.json.JsonAST._
import blueeyes.json.Printer._
import blueeyes.json.JsonParser
import blueeyes.json.JsonAST.JValue
object JsonToTextBijection extends Bijection[JValue, String]{
  def unapply(t: String) = JsonParser.parse(t)
  def apply(s: JValue)   = compact(render(s))
}

object JsonToJsonBijection extends Bijection[JValue, JValue] {
  def unapply(t: JValue) = t
  def apply(s: JValue)   = s
}

object ByteArrayToByteArray extends Bijection[Array[Byte], Array[Byte]] {
  def unapply(t: Array[Byte]) = t
  def apply(s: Array[Byte])   = s
}

object JsonToByteArrayBijection extends Bijection[JValue, Array[Byte]] {
  def unapply(t: Array[Byte]) = JsonParser.parse(t.map(_.toChar).mkString)
  def apply(s: JValue)         = compact(render(s)).toArray.map(_.toByte)
}

import xml.NodeSeq
import xml.XML
object XMLToTextBijection extends Bijection[NodeSeq, String]{
  def apply(s: NodeSeq)  = s.toString
  def unapply(t: String) = XML.loadString(t)
}

object TextToTextBijection extends Bijection[String, String]{
  def unapply(s: String) = s
  def apply(t: String)   = t
}
