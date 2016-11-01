package gogo.server

import scala.actors.Actor
import java.net.ServerSocket
import java.net.Socket
import java.io.OutputStreamWriter
import java.io.LineNumberReader
import java.io.InputStreamReader
import scala.xml.Utility
import java.io.BufferedOutputStream
import java.util.Date
import java.io.File
import java.util.StringTokenizer
import gogo.server2.SocketWorker
import gogo.server2.Server
import gogo.server2.Idle
import gogo.server2.Dispatcher
import gogo.server2.Connection

case class Idle(worker: SocketWorker)
case class Connection(socket: Socket, id: Int)
case class Status(code: Int, text: String);

class SocketWorker(val id: Int, val dispatcher: Dispatcher) extends Actor {
	val lineSep = File.separator;
	val encoding = "UTF-8";
	var content = xml.Xhtml.toXhtml(
		<HTML>
			<HEAD><TITLE>xxxx</TITLE></HEAD>
			<BODY>
				<h2>Hello world</h2>
			</BODY>
		</HTML>).getBytes("UTF-8")

	def act() {
		loop {
			react {
				case Connection(socket, id) =>
					handleConnection(socket)
					socket.close();
					dispatcher ! Idle(this)
			}
		}
	}

	override def hashCode(): Int = id;
	override def equals(other: Any): Boolean =
		other match {
			case that: SocketWorker => this.id == that.id
			case _ => false
		}

	def handleConnection(socket: Socket) = {
		val header = s"""
      |HTTP/1.1 200 SUCCESS
      |Server: Scala HTTP Server 1.0
      |Date: ${new Date()}
      |Content-type: text/html
      |Content-length: ${content.length}
    """.trim.stripMargin + lineSep + lineSep

		val out = new BufferedOutputStream(socket.getOutputStream())
		val reader = new LineNumberReader(new InputStreamReader(socket.getInputStream()))

		val result = reader.readLine();
		//println(result)
		val tokens = new StringTokenizer(reader.readLine());
		tokens.nextToken().toUpperCase() match {
			case method @ ("GET" | "HEAD") =>
				//print("xxxxxxxx")
				//Thread.sleep(100);
				out.write(header.getBytes("UTF-8"));
				out.flush();

				out.write(content);
				out.flush();

			case method =>
				out.write(header.getBytes("UTF-8"));
				out.flush();
		}

	}
}

class Dispatcher() extends Actor {
	import scala.collection.mutable.{ Map, ListBuffer }
	import _root_.java.util.Random

	val idleWorkers = new ListBuffer[SocketWorker]
	val busyWorkers = Map[Int, SocketWorker]()
	val rng = new Random()

	println("processor number is " + Runtime.getRuntime().availableProcessors())

	val total: Int = Runtime.getRuntime().availableProcessors() * 4;
	var index: Int = 0;

	for (i <- 1 to total) {
		val w = new SocketWorker(i, this)
		w.start;
		idleWorkers += w;
	}

	def act() {
		loop {
			react {
				case Idle(worker) =>
					busyWorkers -= worker.id
					idleWorkers += worker
				case conn: Connection =>
					val worker =
						if (idleWorkers.length == 0) {
							busyWorkers.get(rng.nextInt(busyWorkers.size) + 1).get
							//busyWorkers.get((index % total) + 1).get
						} else {
							val w = idleWorkers.remove(0)
							busyWorkers += w.id -> w
							w
						}
					worker ! conn
			}
		}
	}
}

class Server() {
	val name: String = "Main";
	def run() = {
		val socket = new ServerSocket(8888);
		val dispatcher = new Dispatcher();
		var i = 0;

		dispatcher.start;

		while (true) {
			val clientConn = socket.accept();
			i += 1;
			dispatcher ! Connection(clientConn, i)
		}
	}
}

object WebServerMain extends Application {
	println("This is ScalaWebServer running on port " + 8888);
	new Server().run;
}