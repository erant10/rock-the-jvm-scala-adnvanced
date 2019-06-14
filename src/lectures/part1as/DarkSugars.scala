package lectures.part1as

import scala.util.Try

object DarkSugars {


  // 1. methods with single parameters
  def aSingleArgMethod(arg: Int): String = s"$arg little ducks"

  val description = aSingleArgMethod {
    // write some complex code
    42
  }

  val aTryInstance = Try {
    throw new RuntimeException
  }
  /*
    is equivalent to:
    val anException = throw new RuntimeException
    val aTryInstance = Try(anException)
  */
  List(1,2,3).map { x =>
    x + 1
  }


  // 2. Single abstract method
  trait Action {
    def act(x: Int): Int
  }

  val anInstance: Action = new Action {
    override def act(x: Int): Int = x + 1
  }

  val aFunkyInstance: Action = (x: Int) => x + 1

  // example: Runnables
  val aThread = new Thread(new Runnable {
    override def run(): Unit = println("Hello Scala")
  })

  val aSweeterThread = new Thread(() => println("Sweet Scala"))

  abstract class AnAbstractType {
    def implemented: Int = 23
    def f(a: Int): Unit
  }

  val AnAbstractInstance: AnAbstractType = (a: Int) => println("sweet")


  // 3. the :: and #:: methods
  val prependedList = 2 :: List(3,4)
  // 2.::(List(3,4)) OR List(3,4).::(2) ?!

  // scala spec: last char decides associativity of method

  1 :: 2 :: 3 :: List(4,5)
  // equivalent to
  List(4,5).::(3).::(2).::(1)

  class MyStream[T] {
    def -->:(value: T): MyStream[T] = this // actual implementation here
  }
  val myStream = 1 -->: 2 -->: 3 -->: new MyStream[Int]


  // 4. multi-word method naming
  class TeenGirl(name: String) {
    def `and then said`(gossip: String) = println(s"$name said $gossip")
  }

  val lilly = new TeenGirl("Lilly")
  lilly `and then said` "Scala is so sweet"


  // 5. infix types
  class Composite[A,B]
  val normalComposite: Composite[Int,String] = ???
  val sweetComposite: Int Composite String = ???

  class -->[A,B]
  val towards: Int --> String = ???


  // 6. the update
  val anArray = Array(1,2,3)
  anArray(2) = 7 // rewritten to anArray.update(2, 7)
  // used in mutable collections


  // 7. setters for mutable containers
  class Mutable {
    private var internalMember: Int = 0
    def member = internalMember // the "getter"
    def member_=(value: Int): Unit = internalMember = value // the "setter"
  }
  val aMutableContainer = new Mutable
  aMutableContainer.member = 42 // rewritten as aMutableContainer.member_=(42)


}
