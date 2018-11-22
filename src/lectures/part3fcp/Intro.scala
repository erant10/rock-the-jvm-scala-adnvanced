package lectures.part3fcp

import java.util.concurrent.Executors

object Intro extends App {

  /*
    interface Runnable {
      public void run()
    }
   */
  // JVM Threads

  val aRunnable = new Runnable {
    override def run(): Unit = println("Running in parallel")
  }
  val aThread = new Thread(aRunnable)
  aThread.start() // gives the signal to the JVM thread
  aRunnable.run() // doesn't do anything in parallel!
  aThread.join() // blocks until aThread finishes running

  val threadHello = new Thread(() => (1 to 5).foreach(_ => println("hello")))
  val threadGoodbye = new Thread(() => (1 to 5).foreach(_ => println("goodbye")))
  threadHello.start()
  threadGoodbye.start() // "hello"s and "goodbye"s will be printed in different orders in each run
  // different runs produce different results!

  // executors
  val pool = Executors.newFixedThreadPool(10)
  pool.execute(() => println("Something in the thread pool"))

  pool.execute(() => {
    Thread.sleep(1000)
    println("done after 1 second")
  })

  pool.execute(() => {
    Thread.sleep(1000)
    println("almost done")
    Thread.sleep(2000)
    println("done after 2 seconds")
  })

  // pool.shutdown() // after running shutdown, no more actions can be submitted
  // pool.execute(() => println("Should not appear")) // throws an exception in the calling thread

  // pool.shutdownNow() // this will shutdown any running threads
  println(pool.isShutdown()) // false
  pool.shutdown()

  def runInParallel = {
    var x = 0

    val thread1 = new Thread(() => {
      x = 1
    })

    val thread2 = new Thread(() => {
      x = 2
    })

    thread1.start()
    thread2.start()
    println(x)
  }
  for (_ <- 1 to 1000) runInParallel

  class BankAccount(var balance: Int) {
    override def toString: String = "" + balance
  }
  def buy(account: BankAccount, thing: String, price: Int) = {
    account.balance -= price // rewritten to account.balance = account.balance - price
//    println(s"I've bought $thing")
//    println(s"My account is now $account")
  }

  for (_ <- 1 to 10) {
    val account = new BankAccount(50000)
    val thread1 = new Thread(() => buy(account, "shoes", 3000))
    val thread2 = new Thread(() => buy(account, "iphone12", 4000))

    thread1.start()
    thread2.start()
    Thread.sleep((10))
    if (account.balance != 43000) println("AHA " + account.balance)
    println()
  }
  /*
    thread1 (shoes): balance == 50000
      - account = 50000 - 3000 = 47000
    thread2 (shoes): balance == 50000
      - account = 50000 - 4000 = 46000 // overwrites the memory of account.balance
   */
  def buySafe(account: BankAccount, thing: String, price: Int) = {
    account.synchronized {
      // never 2 thread will evaluate this at the same time
      account.balance -= price
      println(s"I've bought $thing")
      println(s"My account is now $account")
    }
  }

}
