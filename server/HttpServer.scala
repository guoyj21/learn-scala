package gogo.server

import java.io.File
import java.net.ServerSocket
import scala.actors.threadpool.Executors
import java.net.Socket
import com.sun.org.apache.xml.internal.utils.StringToIntTable
import java.util.StringTokenizer
import org.omg.IOP.Encoding
import java.io.BufferedOutputStream
import java.util.Date

object HttpServer {
	val encoding = "UTF-8";
	val webRoot = new File(".");
	val defaultFile = "index.html";
	val lineSep = File.separator;

	case class Status(code: Int, text: String);

	def main(args: Array[String]) {
		val port = 8888;
		val serverSocket = new ServerSocket(port);
		println("Lisening for connections on port " + port);

		val pool = Executors.newCachedThreadPool();
		while (true)
			pool.execute(new HttpServer(serverSocket.accept()));
	}
}

class HttpServer(socket: Socket) extends Runnable() {
	import HttpServer._;

	def respond(status: Status, contentType: String = "text/html", content: Array[Byte]) {
		val out = new BufferedOutputStream(socket.getOutputStream())

		val header = s"""
      |HTTP/1.1 ${status.code} ${status.text}
      |Server: Scala HTTP Server 1.0
      |Date: ${new Date()}
      |Content-type: ${contentType}
      |Content-length: ${content.length}
    """.trim.stripMargin + lineSep + lineSep

		try {
			out.write(header.getBytes(encoding))
			out.flush()

			out.write(content)
			out.flush()
		} finally {
			out.close()
		}
	}

	def respondWithHtml(status: Status, title: String, body: xml.NodeSeq) =
		respond(
			status = status,
			content = xml.Xhtml.toXhtml(
				<HTML>
					<HEAD><TITLE>{ title }</TITLE></HEAD>
					<BODY>
						{ body }
					</BODY>
				</HTML>).getBytes(encoding))

	def run() {
		//println("Connection opened.")
		val source = io.Source.fromInputStream(socket.getInputStream(), "UTF-8");

		val line = source.getLines.next;
		val tokens = new StringTokenizer(line);
		//println(line);

		tokens.nextToken().toUpperCase() match {
			case method @ ("GET" | "HEAD") =>
				//print("xxxxxxxx")
				//Thread.sleep(100);
				respondWithHtml(
					Status(501, "Not Implemented"),
					title = "501 Not Implemented",
					body = <H2>501 Not Implemented: { method } method</H2>)
			case method =>
				//print("yyyyyyy")
				respondWithHtml(
					Status(501, "Not Implemented"),
					title = "501 Not Implemented",
					body = <H2>501 Not Implemented: { method } method</H2>)
		}
	}
}