package lectures.part4implicits

object TypeClasses extends App {

  trait HTMLWritable {
    def toHTML: String
  }

  // option 1
  case class User(name: String, age: Int, email: String)
  // extends HTMLWritable {
  //  override def toHTML: String = s"<div>$name ($age yo) <a href='$email'/></div>"
  //}

  //User("John", 32, "john@rockthejvm.com").toHTML

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

  object HTMLSerializer {
    def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String  =
      serializer.serialize(value)

    def apply[T](implicit serializer: HTMLSerializer[T]): HTMLSerializer[T] = serializer
  }

  implicit object IntSerializer extends HTMLSerializer[Int] {
    override def serialize(value: Int): String = s"<div style='color:blue'>$value</div>"
  }

  println(HTMLSerializer.serialize(42)) // IntSerializer is implied
  println(HTMLSerializer.serialize(john)) // UserSerializer is implied

  // access to the entire class interface
  println(HTMLSerializer[User].serialize(john))

  implicit class HTMLEnrichment[T](value: T) {
    def toHTML(implicit serializer: HTMLSerializer[T]): String = serializer.serialize(value)
  }
  val arthur = User("Arthur", 43, "arthur@rockthejvm.com")
  println(arthur.toHTML(UserSerializer)) // println(new HTMLEnrichment[User](arthur).toHTML(UserSerializer))

  println(2.toHTML) // println(new HTMLEnrichment[Int](42).toHTML(IntSerializer))

  // context bounds
  def htmlBoilerPlate[T](content: T)(implicit serializer: HTMLSerializer[T]): String =
    s"<html><body>${content.toHTML(serializer)}</body></html>"

  def htmlSugar[T: HTMLSerializer](content: T): String = {
    val serializer = implicitly[HTMLSerializer[T]]
    s"<html><body>${content.toHTML}</body></html>"
  }

  // implicitly
  case class Permissions(mask: String)
  implicit val defaultPermissions = Permissions("0744")

  // in some other part of the code
  val standardPerms = implicitly[Permissions]
}

