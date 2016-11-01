package gogo

object Demo2 {

	def oncePerSecond(callback:() => Unit) {
		while (true) {
			callback();
			Thread.sleep(1000);
		}
	}

	def main(args: Array[String]) {
		for (a <- args) {
			a match {
				case "-h" =>
					println("Hello world")
			}
		}

		oncePerSecond(() => println("Hello world"));
	}

}