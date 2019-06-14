package lectures.part5ts

object RockingInheritance extends App {

  // 1 - convenience
  trait Writer[T] {
    def write(value: T): Unit
  }
  trait Closeable {
    def close(status: Int): Unit
  }
  trait GenericStream[T] {
    // some methods
    def foreach[T](f: T => Unit): Unit
  }
  def processStream[T](stream: GenericStream[T] with Writer[T] with Closeable): Unit = {
    // access to the entire API
    stream.foreach(println)
    stream.close(0)
  }

  // 2 - the diamond problem
  trait Animal { def name: String }
  trait Lion extends Animal { override def name: String = "Lion" }
  trait Tiger extends Animal  { override def name: String = "Tiger" }
  class Mutant extends Lion with Tiger // { override def name: String = "ALIEN" }

  val m = new Mutant
  println(m.name) // prints "Tiger"

  /*
    Mutant extends Animal with { override def name: String = "Lion" }
    with { override def name: String = "Tiger" }
   */


  // 3 - The super problem + type linearization

  trait Cold {
    def print = println("cold")
  }
  trait Green extends Cold {
    override def print: Unit = {
      println("green")
      super.print // println("cold")
    }
  }
  trait Blue extends Cold {
    override def print: Unit = {
      println("blue")
      super.print // println("cold")
    }
  }
  class Red {
    def print = println("red")
  }
  class White extends Red with Green with Blue {
    override def print: Unit = {
      println("white")
      super.print
    }
  }

  val color = new White
  color.print // white blue green cold
  // WHERE is Red?!
}
