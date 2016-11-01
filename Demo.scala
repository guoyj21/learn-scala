package gogo

object Demo {

	def sort(xs: Array[Int]) {
		def swap(i: Int, j: Int) {
			val t = xs(i);
			xs(i) = xs(j);
			xs(j) = t;
		}
		def sort1(l: Int, r: Int) {
			val pivot = xs((l + r) / 2)
			var i = l; var j = r;
			while (i <= j) {
				while (xs(i) < pivot) i += 1;
				while (xs(j) > pivot) j -= 1;
				if (i <= j) {
					swap(i, j);
					i += 1;
					j -= 1;
				}
			}
			if (l < j) sort1(l, j);
			if (j < r) sort1(i, r);
		}
		sort1(0, xs.length - 1);
	}

	def mySort(xs: Array[Int]): Array[Int] = {
		if (xs.length <= 1) xs;
		else {
			val pivot = xs(xs.length / 2);
			Array.concat(
				mySort(xs filter (pivot >)),
				xs filter (pivot ==),
				mySort(xs filter (pivot <)));
		}
	}

	def sum(f: Int => Int): (Int, Int) => Int = {
		def sumF(a: Int, b: Int): Int = if (a > b) 0 else f(a) + sumF(a + 1, b);
		sumF;
	}

	def main(args: Array[String]) {
		println("Hello world");

		var name: String = "GoGo";

		println(name);

		val age: Int = 100;

		val time = System.currentTimeMillis();
		println("time is " + time);

		var i: Float = 1.6f;

		i = i round;

		println(i);

		var nums: Array[Int] = Array(1, 0, 8, 3, 2, 13, 4, 5, 64, 3, 2, 998, 12, 34);
		nums = mySort(nums);

		nums.foreach(num => print(num + " "));
		
		//println("xxxx " + sum(100));
	}
}