package zhttp.http

import io.netty.buffer.{ByteBuf, Unpooled}
import zhttp.experiment.ContentDecoder
import zio._

import java.net.InetAddress

trait Request extends HeadersHelpers { self =>
  def method: Method

  def url: URL

  def headers: List[Header]

  def path: Path = url.path

  def decodeContent[R, B](decoder: ContentDecoder[R, Throwable, B]): ZIO[R, Throwable, B]

  def remoteAddress: Option[InetAddress]

  def addHeader(header: Header): Request = self.copy(headers = header :: self.headers)

  def removeHeader(name: CharSequence): Request = self.copy(headers = self.headers.filter(_.name != name))

  def copy(method: Method = self.method, url: URL = self.url, headers: List[Header] = self.headers): Request = {
    val m = method
    val u = url
    val h = headers
    new Request {
      override def method: Method = m

      override def url: URL = u

      override def headers: List[Header] = h

      override def remoteAddress: Option[InetAddress] =
        self.remoteAddress

      override def decodeContent[R, B](decoder: ContentDecoder[R, Throwable, B]): ZIO[R, Throwable, B] =
        self.decodeContent(decoder)
    }
  }
}

object Request {
  def apply(method: Method = Method.GET, url: URL = URL.root, headers: List[Header] = Nil, content :ByteBuf =Unpooled.EMPTY_BUFFER): Request = {
    val m = method
    val u = url
    val h = headers
    new Request {
      override def method: Method = m

      override def url: URL = u

      override def headers: List[Header] = h

      override def remoteAddress: Option[InetAddress] = None

      override def decodeContent[R, B](decoder: ContentDecoder[R, Throwable, B]): ZIO[R, Throwable, B] =
        decoder.getContent(content)
    }
  }
}