package gogo

class ChecksumAccumulator {
	private var sum = 0;
	def add(b: Byte): Unit = {
		sum += b;
		();
	}

	def checksum(): Int = {
		~(sum & 0xFF) + 1;
	}

	def oncePerSecond(callback: () => Unit) {
		while (true) {
			callback();
			Thread.sleep(1000);
		}
	}

	def main(args: Array[String]) {
		oncePerSecond(() => println("Hello world"));
	}
}