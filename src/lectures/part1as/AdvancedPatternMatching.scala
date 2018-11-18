package lectures.part1as

object AdvancedPatternMatching extends App {
  val numbers = List(1)
  val description = numbers match {
    case head :: Nil => println(s"the only element is $head")
    case _ =>
  }

  class Person(val name: String, val age: Int)

  object Person {
    def unapply(person: Person): Option[(String, Int)] =
      if (person.age < 21) None
      else Some((person.name, person.age))

    def unapply(age: Int): Option[String] = Some(if (age<21) "minor" else "major")
  }

  val bob = new Person("Bob", 25)
  val greeting = bob match {
    case Person(n,a) => s"Hi, my name is $n and I am $a years old."
    case _ =>
  }
  println(greeting)

  val legalStatus = bob.age match {
    case Person(status) => s"My legal status is $status"
  }
  println(legalStatus)


  object even {
    def unapply(n: Int): Boolean = { n % 2 == 0 }
  }

  object singleDigit {
    def unapply(n: Int): Boolean = { n > -10 && n <10 }
  }

  val n: Int = 45
  val mathProperty = n match {
    case singleDigit() => "single digit"
    case even() => "an even number"
    case _ => "no property"
  }

  case class Or[A,B](a: A, b:B) // Either

  val either = Or(2,"two")
  val humanDescription = either match {
    // case Or(number, string) => s"$number is written as $string"   // the normal way
    case number Or string => s"$number is written as $string"   // the Either way
  }

  println(humanDescription)

  val vararg = numbers match {
    case List(1,_*) => "Starting with 1"
  }

  abstract class MyList[+A] {
    def head: A = ???
    def tail: MyList[A] = ???
  }
  case object Empty extends MyList[Nothing]
  case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

  object MyList {
    def unapplySeq[A](list: MyList[A]): Option[Seq[A]] =
      if (list == Empty) Some(Seq.empty)
      else unapplySeq(list.tail).map(list.head +: _)
  }

  val myList: MyList[Int] = new Cons(1, new Cons(2, new Cons(3, Empty)))

  val decomposed = myList match {
    case MyList(1,2,_*) => s"Starting with 1 and 2"
    case _ => "something else"
  }
  println(decomposed)

  abstract class Wrapper[T] {
    def isEmpty: Boolean
    def get: T
  }

  object PersonWrapper {
    def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
      override def isEmpty: Boolean = false
      override def get: String = person.name
    }
  }

  println(bob match {
    case PersonWrapper(n) => s"This persons name is $n"
    case _ => s"An alien"
  })
}
