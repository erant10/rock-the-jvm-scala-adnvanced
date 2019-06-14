package lectures.part4implicits

object PimpMyLibrary extends App {

  implicit class RichInt(val value: Int) extends AnyVal {
    def isEven: Boolean = value % 2 == 0
    def sqrt: Double = Math.sqrt(value)

    def times(f: () => Unit): Unit = {
      def timesAux(n: Int): Unit =
        if (n <= 0) ()
        else {
          f()
          timesAux(n-1)
        }
      timesAux(value)
    }

    def *[T](l: List[T]): List[T] = {
      def concatenate(n: Int): List[T] =
        if (n <= 0) List()
        else concatenate(n-1) ++ l
      concatenate(value)
    }
  }
  implicit class RicherInt(richInt: RichInt) {
    def isOdd: Boolean = richInt.value % 2 != 0
  }

  new RichInt(42).sqrt // the "standard" way
  // the implicit way
  42.isEven // compiler converts to new RichInt(42).isEven

  // 42.isOdd // does not compile

  1 to 10

  import scala.concurrent.duration._
  3.seconds

  implicit class RichString(val string: String) extends AnyVal {
    def asInt: Int = Integer.valueOf(string)
    def encrypt(cypherDistance: Int)= string.map(c => (c + cypherDistance).asInstanceOf[Char])
  }
  println("3".asInt + 4)
  println("John".encrypt(2))

  3.times(() => println("scala rocks"))
  println(4 * List(1,2,3))

  // "3" / 4
  implicit def stringToInt(string: String): Int = Integer.valueOf(string)
  println("6" / 2) //complier rewrites to stringToInt("6") / 2

  // equivalent to: implicit class RichAltInt(value: Int)
  class RichAltInt(value: Int)
  implicit def enrich(value: Int): RichAltInt = new RichAltInt(value)

  // danger zone
  implicit def intToBoolean(i: Int): Boolean = i == 1

  /*
  if (n) do something
  else do something else
   */

  val aConditionValue = if (3) "OK" else "Something Wrong"
  println(aConditionValue)
}
