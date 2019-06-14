package lectures.part2afp

object Monad extends App {

  // our own try monad

  trait Attempt[+A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B]
  }

  object Attempt {
    // we don't want the argument to be evaluated immediately so we will pass a byName as argument
    def apply[A](a: => A): Attempt[A] =
      try {
        Success(a) // a will be evaluated here
      } catch {
        case e: Throwable => Fail(e)
      }
  }

  case class Success[+A](value: A) extends Attempt[A] {
    def flatMap[B](f: A => Attempt[B]): Attempt[B] =
      try {
        f(value)
      } catch {
        case e: Throwable => Fail(e)
      }
  }
  case class Fail(a: Throwable) extends Attempt[Nothing] {
    def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] = this
  }

  /*
   Law 1: Left Identity

   unit.flatMap(f) = f(x)
   Attempt(x).flatMap(f) = f(x) // Success Case
   Success(x).flatMap(f) = f(x) // Proved
   ---------
   Law 2: Right Identity

   attempt.flatMap(unit) = attempt
   Success(x).flatMap(x => Attempt(x)) = Attempt(x) = Success(x)
   Fail(e).flatMap(...) = Fail(e)
   --------
   Law 3: Associativity

   attempt.flatMap(f).flatMap(g) == attempt.flatMap(x => f(x).flatMap(g))
   Fail(e).flatMap(f).flatMap(g) = Fail(e)
   Fail(e).flatMap(x => f(x).flatMap(g)) = Fail(e)

   Success(v).flatMap(f).flatMap(g)
      = f(v).flatMap(g) OR Fail(e)

   Success(v).flatMap(x => f(x).flatMap(g))
      =  f(v).flatMap(g) OR Fail(e)
  */
  val attempt = Attempt {
    throw new RuntimeException("My own Monad")
  }
  println(attempt)

  class Lazy[+A](value: => A) {
    private lazy val internalValue = value //call by need
    def use: A = internalValue
    def flatMap[B](f: (=>A) => Lazy[B]): Lazy[B] = f(internalValue)
  }
  object Lazy {
    def apply[A](a: => A): Lazy[A] = new Lazy(a)
  }

  val lazyInstance = Lazy {
    println("Today I don't feel like doing anything")
    42
  }
  //println(lazyInstance.use)

  val flatMappedInstance = lazyInstance.flatMap(x => Lazy {
    x * 10
  })
  val flatMappedInstance2 = lazyInstance.flatMap(x => Lazy {
    x * 10
  })

  println(flatMappedInstance.use)
  println(flatMappedInstance2.use)

  /*
  left identity
  unit.flatMap(f) = f(x)
  Lazy(v).flatMap(f) = f(x)

  right identity
  l.flatMap(unit) = l
  Lazy(v).flatMap(x=> Lazy(x)) = Lazy(v)

  associativity
  Lazy(v).flatMap(f).flatMap(g) = f(v).flatMap(g)
  Lazy(v).flatMap(x => f(x).flatMap(g)) = f(v).flatMap(g)
   */


  // map and flatten in terms of flatMap

  /*
    Monad[T] {
      def flatMap[B](f: T => Monad[B] = ... (implemented)

      def map[B](f: T => B): Monad[B] = flatMap(x => unit(f(x))
      def flatten(m: Monad[Monad[T]]): Monad[T] = m.flatMap((x: Monad[T]) =>x)

      List(1,2,3).map(_ * 2) = List(1,2,3).flatMap(x => List(x * 2))
      List(List(1,2), List(3,4)).flatten = List(List(1,2), List(3,4)).flatMap(x => x) = List(1,2,3,4)
    }
   */


}
