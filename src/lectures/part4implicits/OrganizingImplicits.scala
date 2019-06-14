package lectures.part4implicits

object OrganizingImplicits extends App {
  implicit val reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
  //implicit val normalOrdering: Ordering[Int] = Ordering.fromLessThan(_ < _) // does not compile
  println(List(1,4,5,3,2).sorted)

  // scala.Predef

  case class Person(name: String, age: Int)
/*
  object Person {
     implicit val alphanumericOrdering: Ordering[Person] =
       Ordering.fromLessThan((a,b) => a.name.compareTo(b.name) < 0) // "Loses"
  }
  implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a,b) => a.age < b.age) // "Wins"
*/
  val persons = List(
    Person("Steve", 30),
    Person("Amy", 22),
    Person("John", 66)
  )

  //println(persons.sorted)

  object AgeOrdering {
    implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a,b) => a.age < b.age)
  }
  import AgeOrdering._
  println(persons.sorted) // this works

  /*
    orderings:
     - totalPrice - most used (50%)
     - by unit count - 25%
     - by unit price - 25%
   */

  case class Purchase(nUnits: Int, unitPrice: Double) {
    val totalPrice = unitPrice * nUnits
  }
  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a,b) => a.totalPrice < b.totalPrice)
  }
  object UnitCountOrdering {
    implicit val unitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan((a,b) => a.nUnits < b.nUnits)
  }
  object UnitPriceOrdering {
    implicit val unitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a,b) => a.unitPrice < b.unitPrice)
  }

  val purchases = List (
    Purchase(5, 9.99),
    Purchase(1, 1000),
    Purchase(10, 25),
  )

  import UnitPriceOrdering._
  println(purchases.sorted)
}
