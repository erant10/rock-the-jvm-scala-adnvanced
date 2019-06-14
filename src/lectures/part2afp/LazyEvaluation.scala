package lectures.part2afp

object LazyEvaluation extends App {
  lazy val x: Int = throw new RuntimeException
  //println(x) // this will force the evaluation

  lazy val y: Int = {
    println("hello")
    42
  }
  println(y)
  println(y)


  // implications
  def sideEffectCondition: Boolean = {
    println("Boo")
    true
  }
  def simpleCondition: Boolean = false
  lazy val lazyCondition = sideEffectCondition
  println(if (simpleCondition && lazyCondition) "yes" else "no") // runtime doesn't evaluate lazyCondition

  // in conjunction with call by name
  def byNameMethod(n: => Int): Int = {
    lazy val t = n // only evaluated once!
    t + t + t + 1
  }
  def retrieveMagicValue = {
    // side effect or a long computation
    println("waiting")
    Thread.sleep(1000)
    42
  }
  println(byNameMethod(retrieveMagicValue))

  // CALL BY NEED

  // filtering with lazy vals
  def lessThan30(i: Int): Boolean = {
    println(s"$i is less than 30?")
    i < 30
  }
  def greaterThan20(i: Int): Boolean = {
    println(s"$i is greater than 20?")
    i > 20
  }
  val numbers = List(1,25,40,5,23)
  val lt30 = numbers.filter(lessThan30) // List(1,25,5,23)
  val gt20 = lt30.filter(greaterThan20)
  println(gt20)
  val lt30lazy = numbers.withFilter(lessThan30) // lazy vals under the hood
  val gt20lazy = lt30lazy.withFilter(greaterThan20)
  println
  println(gt20lazy) // scala.collection.TraversableLike$WithFilter@3bd94634
  println(gt20lazy.foreach(println))

  // for-comprehensions use withFilter with guard
  for {
    a <- List(1,2,3) if a % 2 == 0 // use lazy vals
  } yield a + 1
  List(1,2,3).withFilter(_ % 2 == 0).map(_ + 1)

  /*
  Exercise: implement a lazily evaluated singly linked STREAM of elements
   */
}
