package lectures.part1as

import scala.annotation.tailrec

object Recap extends App {
  val aCondition: Boolean = false
  val aConditionVal = if (aCondition) 42 else 65

  // the compiler infers the types of variables
  val aCodeBlock = {
    if (aCondition) 42
    65
  }

  // Unit = void
  val theUnit = println("Hello, Scala")

  // function
  def aFunciton(x: Int): Int = x+1

  // recursion
  @tailrec def factorial(n: Int, accumulator: Int): Int =
    if (n <= 0) accumulator
    else factorial(n-1, n * accumulator)

  // OOP
  class Animal
  class Dog extends Animal
  val aDog: Animal = new Dog

  trait Carnivore {
    def eat(a: Animal) : Unit = ???
  }

  class Crocodile extends Animal with Carnivore {
    override def eat(a: Animal): Unit = println("Crunch crunch")
  }

  // method notations
  val aCroc = new Crocodile
  aCroc.eat(aDog)
  aCroc eat aDog

  // anonymous classes
  val aCarnivore = new Carnivore {
    override def eat(a: Animal): Unit = println("Roar")
  }

  // generics
  abstract class MyList[+A] // variance and variance problem

  // singletons and companions
  object MyList

  // case classes
  case class Person(name: String, age: Int)

  // exceptions and try/catch/finally
  val throwExpression = throw new RuntimeException // Nothing

  val aPotentialFailure = try {
    throw new RuntimeException
  } catch {
    case e: Exception => "I caught exceptions"
  } finally {
    println("Some logs")
  }

  // functional programming
  val incrementor = new Function1[Int, Int] {
    override def apply(v1: Int): Int = v1 + 1
  }

  incrementor(1)

  val annonymousIncrementor = (x: Int) => x + 1
  // map, flatMap, filter
  List(1,2,3).map(annonymousIncrementor) // HDF

  // for comprehensions
  val pairs = for {
    num <- List(1,2,3)
    char <- List('a', 'b', 'c')
  } yield num + "-" + char

  // Collections: Seqs, Arrays, Lists, Vectors, Maps, tuples
  val aMap = Map(
    "Daniel" -> 789,
    "Jess" -> 555
  )

  // "collections": Options, Try
  val anOption = Some(5)

  // pattern matching
  val x = 2
  val order = x match {
    case 1 => "first"
    case 2 => "second"
    case 3 => "third"
    case _ => x+"th"
  }

  val bob = Person("Bob", 22)
  val greeting = bob match {
    case Person(n,_) => s"Hi my name is $n"
  }

}
