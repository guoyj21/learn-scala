package gogo.server

import scala.actors.Actor

case class Increment(amount: Int)
case class Balance

class Account extends Actor {
	var balance: Int = 0;
	def act() = {
		receive {
			case Increment(amount) =>
				balance += amount;
			case Balance =>
				println("Balance is " + balance)
				exit();
		}
	}
}