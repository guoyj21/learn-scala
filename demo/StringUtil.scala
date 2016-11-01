package gogo.demo

object StringUtil {
	def joiner(strings: List[String], separator: String = " "): String =
		strings.mkString(separator)
}