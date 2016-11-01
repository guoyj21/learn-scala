package gogo.server2

import scala.actors.Actor
import java.net.ServerSocket
import java.net.Socket
import java.io.LineNumberReader
import java.io.InputStreamReader
import java.io.BufferedOutputStream
import java.util.Date
import java.io.File
import java.util.StringTokenizer
import java.util.Random
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map
import java.nio.channels.SocketChannel
import java.nio.ByteBuffer
import java.nio.channels.spi.SelectorProvider
import java.nio.channels.ServerSocketChannel
import java.net.InetSocketAddress
import java.nio.channels.SelectionKey
import java.nio.channels.Selector

case class Idle(worker: SocketWorker)
case class Connection(socket: Socket, id: Int)
case class Status(code: Int, text: String);
case class Read(channel: SocketChannel, id: Int)

class SocketWorker(val id: Int, val dispatcher: Dispatcher) extends Actor {
	val lineSep = File.separator;
	val encoding = "UTF-8";
	var content = xml.Xhtml.toXhtml(<h2>What are you waiting for</h2>).getBytes("UTF-8")

	val header = s"""
      |HTTP/1.1 200 SUCCESS
      |Server: Scala HTTP Server 1.0
      |Date: ${new Date()}
      |Content-type: text/html
      |Content-length: ${content.length}
    """.trim.stripMargin + lineSep + lineSep

	var buffer: ByteBuffer = ByteBuffer.allocate(1024)

	def act() {
		loop {
			react {
				case Read(channel, id) =>
					channel.write(buffer.put(header.getBytes()))
					channel.close()
					buffer.clear()
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

		val tokens = new StringTokenizer(reader.readLine());
		tokens.nextToken().toUpperCase() match {
			case method @ ("GET" | "HEAD") =>

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

	val total: Int = Runtime.getRuntime().availableProcessors() * 4;
	println("processor number is " + total)

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
				case read: Read =>
					val worker = if (idleWorkers.length == 0)
						busyWorkers.get(rng.nextInt(busyWorkers.size) + 1).get
					else {
						val w = idleWorkers.remove(0)
						busyWorkers += w.id -> w
						w
					}
					worker ! read
			}
		}
	}
}

class SelectingRunnable() {
	val selector: Selector = initSelector(8888)

	def run() = {
		val dispatcher = new Dispatcher()
		var i = 0

		dispatcher.start

		while (true) {
			selector.select()

			val selectedKeysItr = selector.selectedKeys().iterator()

			while (selectedKeysItr.hasNext()) {
				val key = selectedKeysItr.next().asInstanceOf[SelectionKey]
				selectedKeysItr.remove

				if (key.isValid) {
					if (key.isAcceptable)
						accept(key)
					else if (key.isReadable) {
						i += 1
						dispatcher ! Read(key.channel().asInstanceOf[SocketChannel], i)
						key.cancel
					}
				}
			}
		}
	}

	def initSelector(port: Int): Selector = {
		val socketSelector = SelectorProvider.provider().openSelector()
		val serverChannel = ServerSocketChannel.open
		val serverAddress = new InetSocketAddress(port)

		serverChannel.configureBlocking(false)
		serverChannel.socket().bind(serverAddress)
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT)

		return socketSelector
	}

	def accept(key: SelectionKey) = {
		val serverSocketChannel = key.channel().asInstanceOf[ServerSocketChannel]
		val socketChannel = serverSocketChannel.accept

		socketChannel.configureBlocking(false)
		socketChannel.register(selector, SelectionKey.OP_READ)
	}
}

object WebServerMain extends Application {
	println("This is ScalaWebServer running on port " + 8888);
	new SelectingRunnable().run
}