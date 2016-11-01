package gogo.demo

object Foo {

	def click(name: String, callback: () => Int) = {
		println("name is " + callback());
	}

	def main(args: Array[String]) {
		click("gogo", () => 2 + 3)
	}
}