package lectures.part3fcp

import scala.collection.mutable
import scala.util.Random

object ThreadCommunication extends App {
  /*
   The producer - consumer problem
   producer -> ( x ) -> consumer
  */

  class SimpleContainer {
    private var value: Int = 0

    def isEmpty: Boolean = value == 0

    // producing method
    def set(newValue: Int) = value = newValue

    // consuming method
    def get: Int = {
      val result = value
      value = 0
      result
    }
  }

  def naiveProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting")
      while(container.isEmpty) {
        println("[consumer] actively waiting")
      }
      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] computing...")
      Thread.sleep(500)
      val value = 42
      println("[producer] I have produced, after long work, the value " + value)
      container.set(value)
    })

    consumer.start()
    producer.start()

  }

  // naiveProdCons()
/*
  // wait and notify
  val someObject = "Hello"
  // thread 1
  someObject.synchronized { // lock the object's monitor
    // code part 1
    someObject.wait() // release the lock and wait
    // code part 2    // when allowed to proceed lock the monitor again and continue
  }
  // thread 2
  someObject.synchronized { // lock the object's monitor
    // code
    someObject.notify() // signal ONE sleeping thread they may continue
    // more code
  } // but only after I'm done and unlock the monitor
*/

  def smartProdCons(): Unit = {
    val container = new SimpleContainer

    val consumer = new Thread(() => {
      println("[consumer] waiting...")
      container.synchronized {
        container.wait() // wait for the producers signal
      }

      // container must have some value
      println("[consumer] I have consumed " + container.get)
    })

    val producer = new Thread(() => {
      println("[producer] Hard at work...")
      Thread.sleep(2000)
      val value = 42

      container.synchronized {
        println("[producer] I'm producing " + value)
        container.set(value)
        container.notify()
      }
    })

    consumer.start()
    producer.start()
  }
  // smartProdCons()

  /*
    producer -> [ ? ? ? ] -> consumer
   */

  def prodConsLargeBuffer() = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 3

    val consumer = new Thread(() => {
      val random = new Random()

      // try extracting values forever
      while (true) {
        buffer.synchronized {
          if (buffer.isEmpty) {
            println("[consumer] buffer empty. waiting...")
            buffer.wait()
          }
          // there must be at least one value in the buffer
          val x = buffer.dequeue()
          println(s"[consumer] consumed $x")

          buffer.notify() // in case the producer is sleeping, send a signal "hey producer, there's empty space"
        }
        // simulate some work that should be done with the value
        Thread.sleep(random.nextInt(500))
      }
    })

    val producer = new Thread(() => {
      val random = new Random()
      var i = 0

      while (true) {
        buffer.synchronized {
          if (buffer.size == capacity) {
            println("[producer] buffer is full. waiting...")
            buffer.wait()
          }
          // there must be at least 1 empty space in the buffer
          println("[producer] producing " + i)
          buffer.enqueue(i)

          buffer.notify() // in case the consumer is waiting - send signal "hey consumer - there are values to consume"

          i += 1
        }
        Thread.sleep(random.nextInt(500))
      }
    })

    consumer.start()
    producer.start()
  }

  // prodConsLargeBuffer()

  // Multiple consumers and producers

  class Consumer(id: Int, buffer: mutable.Queue[Int]) extends Thread {
    override def run(): Unit = {
      val random = new Random()

      while (true) {
        buffer.synchronized {
          while (buffer.isEmpty) {
            println(s"[consumer $id] buffer empty. waiting...")
            buffer.wait()
          }
          // there must be at least one value in the buffer
          val x = buffer.dequeue()
          println(s"[consumer $id] consumed $x")

          buffer.notify() // notify SOMEBODY (not necessarily the producer)
        }
        // simulate some work that should be done with the value
        Thread.sleep(random.nextInt(250))
      }
    }
  }

  class Producer(id: Int, buffer: mutable.Queue[Int], capacity: Int) extends Thread {
    override def run(): Unit = {
      val random = new Random()
      var i = 0

      while (true) {
        buffer.synchronized {
          while (buffer.size == capacity) {
            println(s"[producer $id] buffer is full. waiting...")
            buffer.wait()
          }
          // there must be at least 1 empty space in the buffer
          println(s"[producer $id] producing " + i)
          buffer.enqueue(i)

          buffer.notify() // notify SOMEBODY (not necessarily the consumer)

          i += 1
        }
        Thread.sleep(random.nextInt(500))
      }
    }
  }

  def multiProdCons(nConsumers: Int, nProducers: Int): Unit = {
    val buffer: mutable.Queue[Int] = new mutable.Queue[Int]
    val capacity = 20

    (1 to nConsumers).foreach(i => new Consumer(i, buffer).start())
    (1 to nProducers).foreach(i => new Producer(i, buffer, capacity).start())
  }

  // multiProdCons(3,6)

  // notifyAll
  def testNotifyAll() = {
    val bell = new Object
    (1 to 10).foreach(i => new Thread(() => {
      bell.synchronized {
        println(s"[thread $i] waiting...")
        bell.wait()
        println(s"[thread $i] Hooray")
      }
    }).start())

    new Thread(() => {
      Thread.sleep(2000)
      println("[announcer] Rock n Roll")
      bell.synchronized {
        bell.notifyAll()
      }
    }).start()
  }

  // testNotifyAll()


  // deadlock
  case class Friend(name: String) {
    def bow(other: Friend) = {
      this.synchronized {
        println(s"$this: I am bowing to my friend $other")
        other.rise(this)
        println(s"$this: My fiend $other has risen")
      }
    }

    def rise(other: Friend) = {
      this.synchronized {
        println(s"$this: I am rising to my friend $other")
      }
    }

    var side = "right"
    def switchSides(): Unit = {
      if (side == "right") side = "left"
      else side = "right"
    }
    def pass(other: Friend): Unit = {
      while (this.side == other.side) {
        println(s"$this: Oh, but please, $other, feel free to pass...")
        switchSides()
        Thread.sleep(1000)
      }
    }
  }
  val sam = Friend("Sam")
  val pierre = Friend("Pierre")

  new Thread(() => sam.pass(pierre)).start()
  new Thread(() => pierre.pass(sam)).start()

  // livelock

}
