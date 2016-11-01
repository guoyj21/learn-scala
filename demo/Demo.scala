package gogo.demo

object Demo extends Application {
	def equalsign2(s: String): Unit = {
		println("equalsign2: " + s);
		()
	}

	equalsign2("Hello")
	
	val array: Array[String] = new Array(5)
	import StringUtil._
	println(joiner(List("Programming", "Scala")))
}