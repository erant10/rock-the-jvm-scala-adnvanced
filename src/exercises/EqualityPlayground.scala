package exercises

import lectures.part4implicits.TypeClasses.User

object EqualityPlayground extends App {

  trait Equal[T] {
    def apply(a: T, b: T): Boolean
  }
  object Equal {
    def apply[T](a:T, b:T)(implicit equalizer: Equal[T]) = equalizer.apply(a,b)
  }

  implicit object NameEquality extends Equal[User] {
    def apply(a: User, b: User): Boolean = a.name == b.name
  }

  object UserEqualByNameAndEmail extends Equal[User] {
    def apply(a: User, b: User): Boolean = a.name == b.name && a.email == b.email
  }
  val john = User("John", 32, "john@rockthejvm.com")

  val anotherJohn = User("John", 45, "john@gmail.com")
  println(Equal(john,anotherJohn)) // AD-HOC polymorphism


  implicit class TypeSafeEqual[T](value: T) {
    def ===(other: T)(implicit equalizer: Equal[T]): Boolean = equalizer(value, other)
    def !==(other: T)(implicit equalizer: Equal[T]): Boolean = ! equalizer(value, other)
  }

  println(john === anotherJohn) // true
  println((john === anotherJohn)(UserEqualByNameAndEmail)) // false

  println(john !== anotherJohn) // false
  println((john !== anotherJohn)(UserEqualByNameAndEmail)) // true

  /*
    TYPE SAFE!
    john == 42 // compiles
    john === 42 // does not compiles
   */

}
