package exercises
/*
  A functional set
*/
trait MySet[A] extends (A => Boolean) {


  def apply(elem: A): Boolean = contains(elem)
  def contains(elem: A) : Boolean
  def +(elem: A): MySet[A]
  def ++(anotherSet: MySet[A]): MySet[A]

  def map[B](f: A=> B): MySet[B]
  def flatMap[B](f: A => MySet[B]): MySet[B]
  def filter(predicate: A => Boolean): MySet[A]
  def foreach(f: A=> Unit): Unit

  def -(elem: A): MySet[A]
  def &(set: MySet[A]): MySet[A]
  def --(set: MySet[A]): MySet[A]

  def unary_! : MySet[A]
}

class EmptySet[A] extends MySet[A] {
  def contains(elem: A): Boolean = false
  def +(elem: A): MySet[A] = new NonEmptySet[A](elem, this)
  def ++(anotherSet: MySet[A]): MySet[A] = anotherSet

  def map[B](f: A => B): MySet[B] = new EmptySet[B]
  def flatMap[B](f: A => MySet[B]): MySet[B] = new EmptySet[B]
  def filter(predicate: A => Boolean): MySet[A] =  this
  def foreach(f: A => Unit): Unit = ()

  def -(elem: A): MySet[A] = this
  def --(set: MySet[A]): MySet[A] = this
  def &(set: MySet[A]): MySet[A] = set

  def unary_! : MySet[A] = new PropertyBasedSet[A](_ => true)
}

// all elements of type A that satisfy a property
class PropertyBasedSet[A](property: A => Boolean) extends MySet[A] {
  def contains(elem: A): Boolean = property(elem)
  def +(elem: A): MySet[A] = new PropertyBasedSet[A](x => property(x) ||  x == elem)
  def ++(anotherSet: MySet[A]): MySet[A] = new PropertyBasedSet[A](x => property(x) && anotherSet(x))

  def map[B](f: A => B): MySet[B] = politelyFail
  def flatMap[B](f: A => MySet[B]): MySet[B] = politelyFail
  def foreach(f: A => Unit): Unit = politelyFail
  def filter(predicate: A => Boolean): MySet[A] = new PropertyBasedSet[A](x => property(x) && predicate(x))

  def -(elem: A): MySet[A] = filter(_ != elem)
  def --(set: MySet[A]): MySet[A] = filter(!set)
  def &(set: MySet[A]): MySet[A] = filter(set)
  def unary_! : MySet[A] = new PropertyBasedSet[A](x => !property(x))

  def politelyFail = throw new IllegalArgumentException("Really deep rabbit hole")
}

class NonEmptySet[A](head: A, tail: MySet[A]) extends MySet[A] {
  def contains(elem: A): Boolean = elem == head || tail.contains(elem)
  def +(elem: A): MySet[A] =
    if (this contains elem) this
    else new NonEmptySet[A](elem, this)
  def ++(anotherSet: MySet[A]): MySet[A] = tail ++ anotherSet + head

  def map[B](f: A => B): MySet[B] = (tail map f) + f(head)
  def flatMap[B](f: A => MySet[B]): MySet[B] = (tail flatMap f) ++ f(head)
  def filter(predicate: A => Boolean): MySet[A] = {
    val filteredTail = tail filter predicate
    if (predicate(head)) filteredTail + head
    else filteredTail
  }
  def foreach(f: A => Unit): Unit = {
    f(head)
    tail foreach f
  }

  def -(elem: A): MySet[A] =
    if (!this.contains(elem)) this
    else if (elem == head) tail
    else tail - elem + head

  def &(set: MySet[A]): MySet[A] = filter(set)

  def --(set: MySet[A]): MySet[A] = filter(!set)

  def unary_! : MySet[A] = new PropertyBasedSet[A](x => !this.contains(x))
}

object MySet {
  def apply[A](values: A*): MySet[A] = {
    def buildSet(valSeq: Seq[A], acc: MySet[A]): MySet[A] =
      if (valSeq.isEmpty) acc
      else buildSet(valSeq.tail, acc + valSeq.head)

    buildSet(values.toSeq, new EmptySet[A])
  }
}

object MySetPlayground extends App {
  val s = MySet(1,2,3,4)
  s + 5 ++ MySet(-1,-2) + 3 flatMap (x => MySet(x,x*10)) filter(_ % 2 == 0) foreach println

  val negative = !s // s.unary_! = all naturals not equal to 1,2,3,4
  println(negative(2)) // false
  println(negative(5)) // true

  val negativeEven = negative.filter(_ % 2 == 0)
  println(negativeEven(5)) // false
  val negativeEven5 = negativeEven + 5
  println(negativeEven5(5)) // true
}