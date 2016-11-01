package gogo

object Entry {
	def main(args: Array[String]) {
		val up = new Upper;
		println(up.upper("A", "First", "Scala", "Program"))
	}
}