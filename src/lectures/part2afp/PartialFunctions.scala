package lectures.part2afp

object PartialFunctions extends App {
  val aFunction = (x: Int) => x + 1 // Function1[Int, Int] === Int => Int

  class FunctionNotApplicableException extends RuntimeException

  val aFussyFunction = (x: Int) =>
    if (x == 1) 42
    else if (x == 2) 56
    else if (x == 5) 999
    else throw new FunctionNotApplicableException

  val aNicerFussyFunction = (x: Int) => x match {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  }

  val aPartialFunction: PartialFunction[Int,Int] = {
    case 1 => 42
    case 2 => 56
    case 5 => 999
  } // partial function value

  println(aPartialFunction(2)) // 56
  // println(aPartialFunction(34)) // throws an error

  println(aPartialFunction.isDefinedAt(34)) // prints false

  // lift
  val lifted = aPartialFunction.lift // Int => Option[Int]
  println(lifted(2)) // Some(56)
  println(lifted(34)) // None

  val pfChain = aPartialFunction.orElse[Int,Int] {
    case 45 => 67
  }

  println(pfChain(2)) // 56 (from the original partial function)
  println(pfChain(45)) // 67

  val aTotalFunction: Int => Int = {
    case 1 => 99
  }

  // HOFs accept partial functions as well
  val aMappedList = List(1,2,3).map {
    case 1 => 42
    case 2 => 78
    case 3 => 1000
  }
  println(aMappedList)


  val aManualFussyFunction = new PartialFunction[Int, Int] {
    override def apply(x: Int): Int = x match {
      case 1 => 42
      case 2 => 56
      case 5 => 999
    }
    override def isDefinedAt(x: Int): Boolean =
      x == 1 || x == 2 || x ==5
  }

  val chatbot: PartialFunction[String, String] = {
    case "hello" => "Hi, my name is HAL9000"
    case "goodbye" => "Once you start talking to me, there is no return"
    case "call mom" => "Unable to find you phone without your credit card"
    case _ => "I don't know what you mean"
  }

  scala.io.Source.stdin.getLines().map(chatbot).foreach(println)

}

