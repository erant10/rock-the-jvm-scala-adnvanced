package lectures.part3fcp

import scala.concurrent.{Await, Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}

object FuturesPromises extends App {

  def calculateTheMeaningOfLife: Int = {
    // long computation
    Thread.sleep(2000)
    42
  }

  val aFuture = Future {
    calculateTheMeaningOfLife
  }

  println(aFuture.value) // Option[Try[Int]]
  println("Waiting on the future")
  aFuture.onComplete {
    case Success(meaningOfLife) => println(s"The meaning of life is $meaningOfLife")
    case Failure(e) => println(s"I have failed with $e")
  } // will be called by SOME thread

  Thread.sleep(3000 ) // prevent the main thread from completing before the future

  // mini social network
  case class Profile(id: String, name: String) {
    def poke(anotherProfile: Profile): Unit = println(s"${this.name} poking ${anotherProfile.name}")
  }
  object SocialNetwork {
    val names = Map(
      "fd.id.1-zuck" -> "Mark",
      "fd.id.2-bill" -> "Bill",
      "fd.id.3-dummy" -> "Dummy"
    )
    val friends = Map(
      "fd.id.1-zuck" -> "fd.id.2-bill"
    )

    val random = new Random

    // API
    def fetchProfile(id: String): Future[Profile] = Future {
      // simulate fetching from a DB
      Thread.sleep(random.nextInt(300))
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bfId = friends(profile.id)
      Profile(bfId, names(bfId))
    }
  }

  // mark to poke bill
  val mark = SocialNetwork.fetchProfile("fd.id.1-zuck")
//  mark.onComplete {
//    case Success(markProfile) => {
//      val bill = SocialNetwork.fetchBestFriend(markProfile)
//      bill.onComplete {
//        case Success(billProfile) => markProfile.poke(billProfile)
//        case Failure(e) => e.printStackTrace()
//      }
//    }
//    case Failure(ex) => ex.printStackTrace()
//  }


  // functional composition of futures
  val nameOnTheWall = mark.map(profile => profile.name)

  val marksBestFriend = mark.flatMap(profile => SocialNetwork.fetchBestFriend(profile))

  val zuchsBestFriendRestricted = marksBestFriend.filter(profile => profile.name.startsWith("Z"))



  for {
    mark <- SocialNetwork.fetchProfile("fd.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
  } mark.poke(bill)

  Thread.sleep(1000)

  val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("fb.id.0-dummy", "Forever alone")
  }

  val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")
  }

  val fallbackResult = SocialNetwork.fetchProfile("unknown id").fallbackTo(
    SocialNetwork.fetchProfile("fb.id.0-dummy")
  )

  // online banking app
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    val name = "Rock the JVM Banking"

    def fetchUser(name: String): Future[User] = Future {
      // simulate fetching from DB
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate some processes
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "SUCCESS")
    }

    def purchase(username: String, item: String, merchantName: String, cost: Double): String = {
      // fetch the user from the DB
      // create a transaction
      // WAIT for the transaction to finish
      val transactionStatusFuture = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, cost)
      } yield transaction.status

      val result = Await.result(transactionStatusFuture, 2.seconds) // implicit conversions -> pimp my library
      println(s"Finished proccessing with result: ${result}")
      result
    }
  }
  println(BankingApp.purchase("Daniel", "iPhone12", "Rock the JVM store", 3000))


  // promises
  val promise = Promise[Int]() // a controller over a Future
  val future = promise.future

  /* Prod Cons using Promises */

  // thread 1 - "consumer"
  future.onComplete {
    case Success(r) => println(s"[consumer] I've received ${r}")
  }

  // thread 2 - "producer"
  val producer = new Thread(() => {
    println("[producer] crunching numbers...")
    Thread.sleep(1000) // long computation
    promise.success(42) // "Fulfilling" the promise
    // promise.failure(new RuntimeException) // "rejecting" the promise
    println("[producer] done!")
  })

  producer.start()
  Thread.sleep(1000)


}
