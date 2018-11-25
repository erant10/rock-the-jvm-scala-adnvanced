package exercises

object Threads extends App {
  // Exercise 1: 50 inception threads (threads that construct other threads) in reverse order
  def inceptionThreads(maxThreads: Int, i: Int = 1): Thread = new Thread(() => {
    if (i < maxThreads) {
      val innerThread = inceptionThreads(maxThreads, i+1)
      innerThread.start() // start the inner thread
      innerThread.join() // wait for the inner thread to finish
    }
    println(s"hello from thread $i")
  })
  //inceptionThreads(50).start()

  // Exercise 2:
  var x = 0
  val threads = (1 to 100).map(_ => new Thread(() => x += 1))
  //threads.foreach(_.start())
  println(x)
  // the biggest possible value for x = 100
  // the smallest possible  value for x = 1


  // Exercise 3: The sleep fallacy
  var message = ""
  val awesomeThread = new Thread(() => {
    Thread.sleep(1000)
    message = "Scala is awesome"
  })
  message = "Scala sucks"
  awesomeThread.start()

  // awesomeThread.join() // wait for the awesomeThread to join

  Thread.sleep(2000)
  println(message) // almost always "scala is awesome" but its not guaranteed
  /*
    A possible Scenario:
      (main thread)
        message = "scala sucks"
        awesomeThread.start()
        sleep() - relieves execution for 2 seconds
      (awesome thread)
        sleep() - relieves execution for 1 second
      (OS gives the CPU to some important thread - takes the CPU for more than 2 seconds)
      (OS gives the CPU back to the MAIN thread)
        println("scala sucks)
      (OS gives the CPU to awesomeThread)
        message = "Scala is awesome)
   */
  // How to fix the Sleep Fallacy?
  // synchronizing will not work!
  // this is a sequential problem, and not a concurrency problem

}
