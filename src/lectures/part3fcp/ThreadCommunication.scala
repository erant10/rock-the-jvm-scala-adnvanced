package lectures.part3fcp

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
  smartProdCons()
}
