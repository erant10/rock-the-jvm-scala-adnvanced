package lectures.part2afp

object CurriesAndPAFs extends App {
  val superAdder: Int => Int => Int = x => y => x + y

  val add3 = superAdder(3) // Int => Int = y => 3 + y
  println(add3(5)) // 8
  println(superAdder(3)(5)) // curried function

  def curriedAdder(x: Int)(y: Int): Int = x + y

  val add4: Int => Int = curriedAdder(4) // type annotation must be supplied

  // lifting & ETA-expansions
  def inc(x:Int) = x + 1
  List(1,2,3).map(inc) // converted to List(1,2,3).map(x => inc(x))

  // Partial function applications
  val add5 = curriedAdder(5) _ // do ETA-expansion and convert to Int => Int

  val simpleAddFunction = (x: Int, y: Int) => x + y
  def simpleAddMethod(x: Int, y: Int) = x + y
  def curriedAddMethod(x: Int)(y: Int) = x + y

  // add7: Int => Int = y => 7 + y
  val add7_1 = curriedAddMethod(7) _
  val add7_2: Int => Int = curriedAddMethod(7)
  val add7_3= (x: Int) => simpleAddFunction(7, x)
  val add7_4: Int => Int = x => simpleAddMethod(7, x)
  val add7_5 = simpleAddFunction.curried(7)
  val add7_6 = curriedAddMethod(7)(_)
  val add7_7 = simpleAddMethod(7, _: Int) // alternative syntax for turning methods into function values
  val add7_8 = simpleAddFunction(7, _: Int) // y => simpleAddFunction(7,y)

  // underscores are powerful
  def concatenator(a: String, b: String, c: String) = a + b + c
  val insertName = concatenator("Hello, I'm ", _: String, ", how are you?")
  println(insertName("Daniel")) // prints "Hello, I'm Daniel, how are you?"

  val fillInBlanks = concatenator("Hello, ", _: String, _: String) // (x,y) => concatenator("hello", x, y)
  println(fillInBlanks("Daniel",". Scala is awesome!")) //prints "Hello, Daniel. Scala is awesome!"

  // "%4.2f", "%8.6f", "%14.12f"
  def curriedFormatter(s: String)(number: Double): String = s.format(number)
  val numbers = List(Math.PI, Math.E, 1, 9.8, 1.3e-12)
  val simpleFormat = curriedFormatter("%4.2f") _
  val seriousFormat = curriedFormatter("%8.6f") _
  val preciseFormat = curriedFormatter("%14.12f") _
  println(numbers.map(simpleFormat))
  println(numbers.map(seriousFormat))
  println(numbers.map(preciseFormat))

  def byName(n: Int) = n + 1
  def byFunction(f: () => Int) = f() + 1

  def method: Int = 42 // evaluated immediately
  def parenMethod(): Int = 42 // not evaluated immediately
  /*
  call byName & byFunction with
    - int
    - method
    - parenMethod
    - lambda
    - PAF
   */
  byName(23) // ok
  byName(method) // ok
  byName(parenMethod()) // ok
  byName(parenMethod) // ok but beware ==> byName(parenMethod())
  // byName(() => 42) // not ok
  byName((() => 42)()) // ok
  // byName(parenMethod _) // not ok

  // byFunction(45) // not ok
  // byFunction(method) // not ok!!!! compiler does not do eta expansion
  byFunction(parenMethod) // compiler does eta-expansion
  byFunction(parenMethod _) // also works, but the _ is unnecessary
  byFunction(() => 46) // ok

}
