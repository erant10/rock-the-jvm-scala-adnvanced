package lectures.part3fcp

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicReference

import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.immutable.ParVector

object ParallelUtils extends App {
  // 1 - Parallel Collections

  val parList = List(1,2,3).par
  val aParVector = ParVector[Int](1,2,3)

  def measure[T](operation: => T): Long = {
    val time = System.currentTimeMillis()
    operation
    System.currentTimeMillis() - time
  }

  /*
  val list = (1 to 10000).toList

  val serialTime = measure {
    list.map(_ + 1)
  }
  println(s"Serial time is $serialTime")

  val parallelTime = measure {
    list.par.map(_ + 1)
  }
  println(s"Parallel time is $parallelTime")
  */
  println(List(1,2,3).reduce(_ - _))
  println(List(1,2,3).par.reduce(_ - _))

  // synchronization
  var sum = 0
  List(1,2,3).par.foreach(sum += _)
  println(sum)

  // configuring parallel collections
  aParVector.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(2)) // 2 is the number of thread

  // Atomic
  val atomic = new AtomicReference[Int](2)
  val currentValue = atomic.get()
  atomic.set(4)
  atomic.getAndSet(6)
  atomic.compareAndSet(38, 56)
  atomic.updateAndGet(_  + 1)
  atomic.accumulateAndGet(12, _ + _)
}
