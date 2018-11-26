package exercises

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Random, Success, Try}

object Promises extends App {
  // 1. fulfill a future immediately with a value
  def fulfillImmediately[T](value: T): Future[T] = Future(value)
  println(fulfillImmediately(99))

  // 2. inSequence(fa, fb) // make sure fb run after fa is fulfilled
  def inSequence[A,B](first: Future[A], second: Future[B]): Future[B] = first.flatMap(_ => second)

  // 3. first(fa, fb) -> new future with the first value of the 2
  def first[A](fa: Future[A], fb: Future[A]): Future[A] = {
    val promise = Promise[A]

    /*
    // method already exists in promise class
    def tryComplete(promise: Promise[A], result: Try[A]) = result match {
      case Success(result) => try {
        promise.success(result)
      } catch {
        case _ =>
      }
      case Failure(throwable) => try {
        promise.failure(throwable)
      } catch {
        case _ =>
      }
    }
    */
    fa.onComplete(promise.tryComplete)
    fb.onComplete(promise.tryComplete)
    promise.future
  }

  // 4. last(fa,fb) -> new future with the last value
  def last[A](fa: Future[A], fb: Future[A]): Future[A] = {
    // 1 promise which both futures will try to fulfill
    // 2 prime which the last future will complete
    val bothPromises = Promise[A]
    val lastPromise = Promise[A]
    val checkAndComplete = (result: Try[A]) =>
      if (!bothPromises.tryComplete(result))
        lastPromise.complete(result)

    fa.onComplete(checkAndComplete)
    fb.onComplete(checkAndComplete)

    lastPromise.future
  }

  val fast = Future {
    Thread.sleep(100)
    42
  }
  val slow = Future {
    Thread.sleep(200)
    45
  }
  first(fast,slow).foreach(f => println(s"FIRST: $f"))
  last(fast,slow).foreach(f => println(s"LAST: $f"))


  // 5. retryUntil(action: () => Future[T], condition: T => Boolean): Future[T] // run the action until the condition is met
  def retryUntil[A](action: () => Future[A], condition: A => Boolean): Future[A] = {
    action()
      .filter(condition)
      .recoverWith {
        case _ => retryUntil(action, condition) // in case the action fails
      }
  }

  val random = new Random()
  val action = () => Future {
    Thread.sleep(100)
    val nextValue = random.nextInt(100)
    println("generated " + nextValue)
    nextValue
  }

  retryUntil(action, (x: Int) => x < 10).foreach(result => println(s"settled at $result"))

  Thread.sleep(10000)
}
