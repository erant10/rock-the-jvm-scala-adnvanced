package lectures.part4implicits

object TypeClasses extends App {
  trait HTMLWritable {
    def toHTML: String
  }
  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    def toHTML: String = s"<div>$name ($age yo) <a href='$email'/></div>"
  }

  User("John", 32, "john@rockthejvm.com").toHTML

  /*
    There are 2 big disadvantages to this approach:
    1. Only works for the types WE write (to apply on other types we would have to implement converters)
    2. Only ONE implementation out of quite a number
   */

  // option 2 - pattern matching
  object HTMLSerializePM {
    def serializeToHtml(value: Any)= value match {
      case User(n,a,e) =>  s"<div>$n ($a yo) <a href='$e'/></div>"
      case _ =>
    }
  }

  // option 3
  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }
  implicit object UserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div>${user.name} (${user.age} yo) <a href='${user.email}'/></div>"
  }
  val john = User("John", 32, "john@rockthejvm.com")
  println(UserSerializer.serialize(john))

  /* Advantages */
  // 1. We can define serializers for other types
  import java.util.Date
  object DateSerializer extends HTMLSerializer[Date] {
    override def serialize(date: Date): String = s"<div>${date.toString()}</div>"
  }

  //2. we can define MULTIPLE serializers
  object PartialUserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String = s"<div>${user.name}</div>"
  }

  trait MyTypeClassTemplate[T] {
    def action(value: T): String
  }
  object MyTypeClassTemplate {
    def apply[T](implicit instance: MyTypeClassTemplate[T]) = instance
  }

  object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String  = serializer.serialize(value)

    def apply[T](implicit serializer: HTMLSerializer[T]) = serializer
  }

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div style='color:blue'>$value</div>"
  }

  println(HTMLSerializer.serialize(42)) // IntSerializer is implied
  println(HTMLSerializer.serialize(john)) // UserSerializer is implied

  // access to the entire class interface
  println(HTMLSerializer[User].serialize(john))


  /*
    Exercise:
   */
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

  val anotherJohn = User("John", 45, "john@gmail.com")
  println(Equal(john,anotherJohn)) // AD-HOC polymorphism


}
