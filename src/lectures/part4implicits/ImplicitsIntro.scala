package lectures.part4implicits

object ImplicitsIntro extends App {
  val pair = "Daniel" -> "555"
  val intPair = 5 -> 3

  case class Person(name: String) {
    def greet = s"Hi, my name is $name"
  }

  // convert a string to a person
  implicit def fromStringToPerson(str: String): Person = Person(str)

  println("Peter".greet) // compiler converts to println(fromStringToPerson("Peter").greet)

//  class A {
//    def greet: Int = 4
//  }
//  implicit def fromStringToA(str: String): A = new A

  // implicit parameters
  def increment(x: Int)(implicit amount: Int) = x + amount
  implicit val defaultAmount = 10 // NOT the same as default arguments
  increment(2)

}
