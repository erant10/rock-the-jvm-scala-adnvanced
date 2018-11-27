# Rock the JVM - Advanced Scala Programming

##  Section 1 - Introduction

### Reminders

* Instructions are executed in sequence and used specifically with imperative style languages (like java, python, etc).

* In functional programming we build a program by building expressions on top of other expressions.

### Dark Syntax Sugar

1. Single argument methods:
    calling methods with single arguments can be done by passing a code block with a complex expression:
    
    ```scala
    def aSingleArgMethod(arg: Int): String = s"$arg little ducks"
    val description = aSingleArgMethod {
       // write some complex code
       42
    }
    ```

2. An instance of a trait with a single abstract method can be reduced to a lambda in the following manner:
    
    ```scala
    trait Action {
       def act(x: Int): Int
    }
    val aFunkyInstance: Action = (x: Int) => x + 1
    ```
    
    A popular example is `Runnable`s:
    
    ```scala
    // the Java way:
    val aThread = new Thread(new Runnable {
       override def run(): Unit = println("Hello Scala")
    })
    
    // the scala way
    val aSweeterThread = new Thread(() => println("Sweet Scala"))
    ``` 
    
    * Note: This also works for abstract classes that have multiple implemented methods and only 1 unimplemented method:
    
        ```scala
        abstract class AnAbstractType {
           def implemented: Int = 23
           def f(a: Int): Unit
        }
        val AnAbstractInstance: AnAbstractType = (a: Int) => println("sweet")
        ```

3. The `::` and `#::` methods
    Scala spec: last char decides associativity of method.
    If the method ends in a colon `:`, that means it's right associative, otherwise it is left associative.
    
    ```scala
    val prependedList = 2 :: List(3,4) // List(3,4).::(2)
    1 :: 2 :: 3 :: List(4,5)
    // equivalent to
    List(4,5).::(3).::(2).::(1)
    // another example
    class MyStream[T] {
       def -->:(value: T): MyStream[T] = this // actual implementation here
    }
    val myStream = 1 -->: 2 -->: 3 -->: new MyStream[Int]
    ```


4. Multi-word Naming:
    
    ```scala
    class TeenGirl(name: String) {
       def `and then said`(gossip: String) = println(s"$name said $gossip")
    }
    val lilly = new TeenGirl("Lilly")
    lilly `and then said` "Scala is so sweet"
    ```

5. Infix Types
    
    ```scala
    class Composite[A,B]
    val normalComposite: Composite[Int,String] = ???
    val sweetComposite: Int Composite String = ???
  
    class -->[A,B]
    val towards: Int --> String = ???
    ```

6. The `update` method is very special - much like `apply`
    The `update` method is used with mutable collections
    The following code: `anArray(2) = 7` is rewritten by the compiler to `anArray.update(2, 7)`.

7. Setters for mutable containers

    ```scala
    class Mutable {
       private var internalMember: Int = 0  
       def member = internalMember // the "getter"
       def member_=(value: Int): Unit = internalMember = value // the "setter"
    }
    val aMutableContainer = new Mutable
    aMutableContainer.member = 42 // rewritten as aMutableContainer.member_=(42)
    ```

### Advanced Patten Matching

#### Custom Patterns

In some cases we might want to make our own structures compatible with pattern matching.

For example, we might have a `class Person` which cannot be implemented as a `case class`, however, we still want to 
deconstruct an instance using pattern matching.

To do so, we need to create a companion `object` to `Person` and implement the `unapply` method:

```scala
class Person(val name: String, val age: Int)

object Person {
  def unapply(person: Person): Option[(String, Int)] =
    if (person.age < 21) None
    else Some((person.name, person.age))
}

val bob = new Person("Bob", 25)
val greeting = bob match {
  case Person(n,a) => s"Hi, my name is $n and I am $a years old."
  case _ =>
}
println(greeting) // Hi, my name is Bob and I am 25 years old.
```

A useful technique for using this is to create objects with unapply methods that return `Boolean`s and use these as 
"test" cases for pattern matching against special properties of an instance. For example:

```scala
object even {
  def unapply(n: Int): Boolean = { n % 2 == 0 }
}

object singleDigit {
  def unapply(n: Int): Boolean = { n > -10 && n <10 }
}

val n: Int = 45
val mathProperty = n match {
  case singleDigit() => "single digit"
  case even() => "an even number"
  case _ => "no property"
}
```

#### Infix Patterns (Custom)

```scala
case class Or[A,B](a: A, b:B) // Either

val either = Or(2,"two")
val humanDescription = either match {
  // case Or(number, string) => s"$number is written as $string"   // the normal way
  case number Or string => s"$number is written as $string"   // the Either way
}
println(humanDescription)
```

needless to say, infix patterns only work when we have 2 things in the pattern.

#### Decomposing sequences

The standard techniques for unapplying don't work for sequences with multiple values that can be decomposed.

Instead, we can use the `unapplySeq`:

```scala
abstract class MyList[+A] {
  def head: A = ???
  def tail: MyList[A] = ???
}
case object Empty extends MyList[Nothing]
case class Cons[+A](override val head: A, override val tail: MyList[A]) extends MyList[A]

object MyList {
  def unapplySeq[A](list: MyList[A]): Option[Seq[A]] =
    if (list == Empty) Some(Seq.empty)
    else unapplySeq(list.tail).map(list.head +: _)
}

val myList: MyList[Int] = new Cons(1, new Cons(2, new Cons(3, Empty)))

val decomposed = myList match {
  case MyList(1,2,_*) => s"Starting with 1 and 2"
  case _ => "something else"
}
println(decomposed) // Starting with 1 and 2
```

#### Custom return types for unapply

The return type for `unapply` and `unapplySeq` does'nt have to be an `Option`. 

The return data structure only needs to have 2 methods: 

1. `isEmpty` which returns a `Boolean`
2. `get` which returns something

For example:

```scala
class Person(val name: String, val age: Int)

abstract class Wrapper[T] {
  def isEmpty: Boolean
  def get: T
}
object PersonWrapper {
  def unapply(person: Person): Wrapper[String] = new Wrapper[String] {
    override def isEmpty: Boolean = false
    override def get: String = person.name
  }
}
val bob = new Person("Bob", 25)
println(bob match {
    case PersonWrapper(n) => s"This persons name is $n"
    case _ => s"An alien"
}) // prints "This persons name is Bob" 
```

## Section 2 - Advanced Functional Programming

### Partial Functions

Sometimes we might want functions that accept only certain types of the input domain.

Functions that accept a subset of the input domain are called **Partial Functions**.

For example, the following function only accepts the set {1,2,5} which is a subset of `Int`.

```scala
val aFussyFunction = (x: Int) => x match {
  case 1 => 42
  case 2 => 56
  case 5 => 999
}
```

A well known class for writing partial functions is `PartialFunction`:

```scala
val aPartialFunction: PartialFunction[Int,Int] = {
  case 1 => 42
  case 2 => 56
  case 5 => 999
}
```

**Partial Function Utilities**

* `isDefinedAt` can check whether a partial function can be run with a certain argument

    ```scala
    val aPartialFunction: PartialFunction[Int,Int] = {
      case 1 => 42
      case 2 => 56
      case 5 => 999
    }
    println(aPartialFunction.isDefinedAt(34)) // prints false
    ```

* `lift` can turn a partial function to a complete function, where instead of throwing an error for non applicable 
values, the function will return `None`. So if the return type was `Int` before applying `lift`, it will become 
`Option[Int]` after applying `lift`. 

    ```scala
    val aPartialFunction: PartialFunction[Int,Int] = {
      case 1 => 42
      case 2 => 56
      case 5 => 999
    }
    val lifted = aPartialFunction.lift // Int => Option[Int]
    println(lifted(2)) // Some(56)
    println(lifted(34)) // None
    ```

* `orElse` can be used when chaining functions.

    ```scala
    val aPartialFunction: PartialFunction[Int,Int] = {
      case 1 => 42
      case 2 => 56
      case 5 => 999
    }
    val pfChain = aPartialFunction.orElse[Int,Int] {
      case 45 => 67
    }
    println(pfChain(2)) // 56 (from the original partial function)
    println(pfChain(45)) // 67
    ```

* Partial functions **extend** "normal" functions

    ```scala
    val aTotalFunction: Int => Int = {
      case 1 => 99
    }
    // HOFs accept partial functions as well
    val aMappedList = List(1,2,3).map {
      case 1 => 42
      case 2 => 78
      case 3 => 1000
    }
    println(aMappedList) // prints List(42, 78, 1000)
    ```

* **Note**: Partial Functions can only have 1 parameter type  

* A partial function can be instantiated manually by implementing the `apply` and `isDefinedAt` functions:

    ```scala
    val aManualFussyFunction = new PartialFunction[Int, Int] {
      override def apply(x: Int): Int = x match {
        case 1 => 42
        case 2 => 56
        case 5 => 999
      }
      override def isDefinedAt(x: Int): Boolean =
        x == 1 || x == 2 || x ==5
    }
    ```

### Functional Collections

The `Set` Collection is actually implemented as a function!
```scala
trait Set[A] extends ((A) => Boolean) // with ...
```

Sequences are "callable" through an  integer index, therefore we can think of sequences as partial functions from Int 
to some other type A.

Similarly, `Map`s are callable through their keys, therefore we can think of `Map`s as partial functions from some type
A to some type B. 

### Currying and Partially Applied Functions

When calling a curried function without all expected arguments (example below), the compiler expects a value type to be
supplied. 

```scala
def curriedAdder(x: Int)(y: Int): Int = x + y
val add4: Int => Int = curriedAdder(4) // type annotation must be supplied
``` 

Behind the scenes, **lifting** is performed.

**lifting** is the idea of transforming a method to a function, also known as **ETA-EXPANSIONS**.

This can also be done directly by using the `_` annotation like so:

```scala
def curriedAdder(x: Int)(y: Int): Int = x + y
val add5 = curriedAdder(5) _ // do ETA-expansion and convert to Int => Int
```

**Note**: Underscores (`_`) are a powerful tool in scala: 

```scala
def concatenator(a: String, b: String, c: String) = a + b + c
val insertName = concatenator("Hello, I'm ", _: String, ", how are you?")
println(insertName("Daniel")) // prints "Hello, I'm Daniel, how are you?"
``` 

### Lazy Evaluations

the `lazy` keyword allows to delay the evaluation of values.

The following code will not throw an error until `x` is used:

```scala
lazy val x: Int = throw new RuntimeException
```

Once a lazy value is evaluated, the same value will stay assigned to that same name.

A useful example for this is when the evaluation of a value if "heavy". We can use `lazy val` to make sure that the 
evaluation is done only once.

```scala
def byNameMethod(n: => Int): Int = {
  lazy val t = n // only evaluated once!
  t + t + t + 1
}
def retrieveMagicValue = {
  // side effect or a long computation
  println("waiting")
  Thread.sleep(1000)
  42
}
println(byNameMethod(retrieveMagicValue)) // "waiting is only printed once"
```

Another example is `withFilter`, which is a filtering method that evaluated the predicates on a **BY NEED** basis.

```scala
// filtering with lazy vals
def lessThan30(i: Int): Boolean = {
  println(s"$i is less than 30?")
  i < 30
}
def greaterThan20(i: Int): Boolean = {
  println(s"$i is greater than 20?")
  i > 20
}
val numbers = List(1,25,40,5,23)
val lt30lazy = numbers.withFilter(lessThan30) // lazy vals under the hood
val gt20lazy = lt30lazy.withFilter(greaterThan20)
println(gt20lazy.foreach(println))
```

this will print:
```
1 is less than 30?
1 is greater than 20?
25 is less than 30?
25 is greater than 20?
25
40 is less than 30?
5 is less than 30?
5 is greater than 20?
23 is less than 30?
23 is greater than 20?
23
```

Note that both conditions are evaluated one after the other for each list item.

The **Call-by-need** technique refers to creating a **by-name** method, and then using if as a lazy val:

```scala
def byName = "hi"
lazy val res = byName 
```

### Monads

`Monad`s are a kind of types which have some fundamental operations

```scala
trait MonadTemplate[A] {
  def unit(value: A): MonadTemplate[A]
  def flatMap[B](f: A => MonadTemplate[B]): MonadTemplate[B]
}
```

The 2 fundamental operations of monads are:

- The `unit` operation, AKA *pure* or *apply*, constructs a monad out of a single (or many) values.

- The `flatMap` operation, AKA *bind*, transforms a monad of a certain type parameter to a monad of another type 
parameters.

These 2 operations must satisfy the *monad laws*:

1. **Left Identity**:

    `unit(x).flatMap(f) == f(x)`.

2. **Right Identity**:

    `aMonadInstance.flatMap(unit) == aMonadInstance`.

3. **Associativity**:

    `m.flatMap(f).flatMap(g) == m.flatMap(x => f(x).flatMap(g))`


**Example 1: `List`**

1. **Left Identity**:

    `List(x).flatMap(f) == f(x) ++ Nil.flatMap(f) == f(x)`.

2. **Right Identity**:

    `list.flatMap(x => List(x)) == list `.

3. **Associativity**:
 
    `[a b c].flatMap(f).flatMap(g) == (f(a) ++ f(b) ++ f(c)).flatMap(g)`
    
    `== f(a).flatMap(g) ++ f(b).flatMap(g) ++ f(c)).flatMap(g)`
    
    `== [a b c].flatMap(f(_).flatMap(g)) == [a b c].flatMap(x => f(x).flatMap(g))`

    
**Example 2: `Option`**

We will focus on the `Some` case because the `None` case is trivial.

1. **Left Identity**:

    `Option(x).flatMap(f) == f(x)`
    
    `Some(x).flatMap(f) == f(x)`

2. **Right Identity**:
 
    `opt.flatMap(x => Option(x)) == opt`
    
    `Some(v).flatMap(x => Option(x)) == Option(v) == Some(v)`

3. **Associativity**:
 
    `o.flatMap(f).flatMap(g) == o.flatMap(x => f(x).flatMap(g))`
    
    `Some(v).flatMap(f).flatMap(g) == f(v).flatMap(g))`
    
    `Some(v).flatMap(x => f(x).flatMap(g)) == f(v).flatMap(g))`

## Section 3 - Functional Concurrent Programming

### Introduction

One of the critical pieces of the parallel programming on the JVM is a `Thread` which takes an implementation of the 
`Runnable` interface as a parameter.

Running a thread in parallel is done by calling the `start` method. 

Starting a thread will create a JVM thread which runs on top of an OS thread.

```scala
val aRunnable = new Runnable {
  override def run(): Unit = println("Running in parallel")
}
val aThread = new Thread(aRunnable)
aThread.start() // gives the signal to the JVM thread 
aThread.join() // blocks until aThread finishes running
aRunnable.run() // doesn't do anything in parallel!
```

There is an important distinction between the thread instance which we work with and the actual JVM thread.

If we want to execute some code in parallel, we should call the `start` method on the thread instance, and **not** the 
`run` method on the `Runnuble`instance.

* Unless configured otherwise, threads will behave differently and produce different results on each run.


#### Executors

Threads are really expensive to start an kill, and a simple solution to this is to reuse them. 

The standard library offers a standard API to reuse threads with executors and thread pools.

#### Race Conditions

A race condition is a situation when multiple threads are attempting the set the same memory zone at the same time.

```scala
class BankAccount(var balance: Int) {
  override def toString: String = "" + balance
}
/* UNSAFE buy */
def buy(account: BankAccount, thing: String, price: Int) = {
  account.balance -= price // rewritten to account.balance = account.balance - price
  println(s"I've bought $thing")
  println(s"My account is now $account")
}
/*
thread1 (shoes): balance == 50000
  - account = 50000 - 3000 = 47000
thread2 (shoes): balance == 50000
  - account = 50000 - 4000 = 46000 // overwrites the memory of account.balance
*/
```

Race conditions are bad because they can introduce bugs in multi threaded code. 

**Possible Solutions**:

1. Use `syncronized()`. The main property of the `syncronized` method no 2 threads can enter the expression that is 
passed to it at the same time. 

    ```scala
    class BankAccount(var balance: Int) {
     override def toString: String = "" + balance
    } 
    def buySafe(account: BankAccount, thing: String, price: Int) = {
        account.synchronized {
          // never 2 thread will evaluate this at the same time
          account.balance -= price
          println(s"I've bought $thing")
          println(s"My account is now $account")
        }
      }
    ```

2. use the `@volatile` annotation.
    `@volatile` annotated on a `val` or `var` means that any reads and writes to it are thread safe.
    
The more powerful and the more used option is the `synchronized` option, because it allows to put more expressions in 
the same synchronized block.

#### Synchronized

entering a synchronized exrpession on an object *locks* the object.

Only `AnyRef`s can have synchronized blocks.

**General Principles**:

- Make no assumptions about who gets the lock first

- Keep locking to a minimum

- Maintain *Thread Safety* at ALL times in parallel applications.

### Thread Communication

As mentioned before, we cannot really enforce a certain order of execution between threads, however, we can manage it. 

#### The Producer-Consumer Problem

**The scenario**: 2 threads are running in parallel: 

1. The *Producer* - Has the sole purpose of setting some value x inside a container. 

2. The *Consumer* - Has the sole purpose of extracting the value from inside the container.

The problem is that both threads are working in parallel, so they don't know when the other has finished working.

Somehow, we have to force the consumer to wait for the producer to finish it's job.
 

#### *wait()* and *notify()*

`wait()`-ing on an object monitor suspends you (the thread) indefinitely.

```scala
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
```

Waiting and notifying only work in synchronized expressions.

#### The *sleep* fallacy

```scala
var message = ""
val awesomeThread = new Thread(() => {
Thread.sleep(1000)
  message = "Scala is awesome"
})
message = "Scala sucks"
awesomeThread.start()
Thread.sleep(2000)
println(message)
```

The code above will almost always print "Scala is awesome", however that is **NOT** guaranteed.

The following possible scenario may occur:

- (main thread):

    - message = "scala sucks"

    - awesomeThread.start()

    - sleep() - relieves execution for 2 seconds

- (awesome thread)

    - sleep() - relieves execution for 1 second

- (OS gives the CPU to some important thread - takes the CPU for more than 2 seconds)

- (OS gives the CPU back to the MAIN thread)

    - println("scala sucks)

- (OS gives the CPU to awesomeThread)

    - message = "Scala is awesome)

**How to fix the Sleep Fallacy?**

synchronizing will not work!

This is a sequential problem, and not a concurrency problem.

A possible solution is to add `join()` after the awesome thread start because this will force the main thread to wait 
for awesomeThread to finish. 

```scala
var message = ""
val awesomeThread = new Thread(() => {
  Thread.sleep(1000)
  message = "Scala is awesome"
})
message = "Scala sucks"
awesomeThread.start()

awesomeThread.join()

Thread.sleep(2000)
println(message)
```

#### The Producer Consumer Problem - Continued

Lets consider the scenario where the producer can produce 1 of 3 values inside a buffer, and the consumer consumes any 
new values inside the buffer.

The following observations can be made: 

1. If the buffer is full, the producer must block until the consumer has finished extracting some value.

2. If the buffer is empty, the consumer must block until the producer has finished setting some value

### Futures and Promises

#### Futures

A future is a computation that will hold a value which is computed by some thread at some point in time.

For example:

```scala
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

def calculateTheMeaningOfLife: Int = {
  // simulate long computation
  Thread.sleep(2000)
  42
}

val aFuture = Future {
  calculateTheMeaningOfLife
}

println(aFuture.value) // Option[Try[Int]]

aFuture.onComplete {
  case Success(meaningOfLife) => println(s"The meaning of life is $meaningOfLife")
  case Failure(e) => println(s"I have failed with $e")
} // will be called by SOME thread
```


##### Functional Composition of Futures

Chaining Futures using nested `onComplete` calls results in a code that is not readable and not sustainable. 

To solve this we will make use of `map`, `flatMap` and `filter`.

`map` can be used to create a `Future` with a certain return type to a `Future` with a different return type.
If the original future happens to fail with an exception, then the mapped future will fail with the same exception.

Therefore a chain of future can be written using for comprehension, which make the code much more readable.

```
val mark = SocialNetwork.fetchProfile("fd.id.1-zuck")
mark.onComplete {
  case Success(markProfile) => {
    val bill = SocialNetwork.fetchBestFriend(markProfile)
    bill.onComplete {
      case Success(billProfile) => markProfile.poke(billProfile)
      case Failure(e) => e.printStackTrace()
    }
  }
  case Failure(ex) => ex.printStackTrace()
}
/* Can be replaced with: */
for {
    mark <- SocialNetwork.fetchProfile("fd.id.1-zuck")
    bill <- SocialNetwork.fetchBestFriend(mark)
} mark.poke(bill)
```


##### Fallbacks

1. `recover` can be called to handle cases when the Future might fail, and return a dummy/default value instead.
    
    ```
    val aProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
      case e: Throwable => Profile("fb.id.0-dummy", "Forever alone")
    }
    ```

2. Instead of returning a default/dummy value, `recoverWith` can be used to return another future which will surely 
    succeed (or at least has a high probability of success).
    
    ```
    val aFetchedProfileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recoverWith {
      case e: Throwable => SocialNetwork.fetchProfile("fb.id.0-dummy")
    }
    ```
    
3. `fallbackTo` can also be used to achieve a similar logic:

    - if the original `Future` succeeds then its value will be used.
    
    - if it fails with an exception, then the argument `Future` will be run.
    
        - if the argument `Future` also fails then the result object will hold the original `Future`'s exception.
    
    ```
    val fallbackResult = SocialNetwork.fetchProfile("unknown id").fallbackTo(
      SocialNetwork.fetchProfile("fb.id.0-dummy")
    )
    ```

##### Blocking on a Future

Sometimes it makes sense to block on a Future for critical operations, and we might want to make sure that the operation
is fully complete before moving on.

This can be achieved using the `Await.result` method

```
Await.result(transactionStatusFuture, 2.seconds)
```

The first argument is the Future that need to finish computing, and the second argument is the timeout.

If the result is not returned before the timeout, a `Timeout` exception will be thrown.

#### Promises

`Future`s are the functional way of composing non-blocking computations which will return at some point. 

However, Notice that we can only read or manipulate the results on `Future` by using `onComplete` or by using functional
composition (`map`, `flatMap`, `filter`, `recover` etc..). 

This means that `Future`s are *read-only* when they are done, but sometimes we need to specifically set or complete a 
`Future` at a point of our choosing - this is where `Promise`s come in.

A `Promise` is a manager or container of one or more `Future`s.

##### The Producer Consumer Problem Revisited

```scala
import scala.concurrent.{Await, Future, Promise}
import scala.util.{Failure, Random, Success}
import scala.concurrent.ExecutionContext.Implicits.global

val promise = Promise[Int]() // a controller over a Future
val future = promise.future

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
```

The Future-Promise paradigm is more powerful as it separates the concern of reading, handling futures, and of writing 
to a promise while eliminating concurrency issues almost completely, as it give complete control of when and how to set
a value to a future. 

### Parallel Libraries

#### Parallel Collections

A parallel collection means that operations on the collection are handled by multiple threads at the same time.

The par method transforms the collection to a parallel version of it.

Among the collections that can have a parallel version are `Seq`,`Vector`, `Array`, `Map`, `Set` and many more.

Parallel collections operate on what is called the **Map-Reduce** model.

The Map-Reduce means that when an operation is performed on a parallel collection, it will be split the elements into 
chunks (using the `Splitter`) which will be processed independently by a separate thread (the `map` step), and finally the results are 
combined together (the *reduce* step, using a `Combiner`).

The parallel collections can be used in the same way that sequential collections are used (in the sense that they have 
`map`, `flatMap`, `filter`, `foreach`, `reduce`, `fold`).

**Important notes**:

1. some operations (especially `fold` and `reduce`) are not always safe to perform. For example: 

    ```scala
    println(List(1,2,3).reduce(_ - _)) // -4
    println(List(1,2,3).par.reduce(_ - _)) // 2
    ```
    
2. Sometimes you need synchronization not on the collections themselves but on the results they act upon due to race 
    conditions of the running threads. 
    
    For example, due to race condition, the following code does not guarantee that the result will be 6:
    
    ```scala
    var sum = 0
    List(1,2,3).par.foreach(sum += _)
    println(sum)
    ```

3. Configuring a parallel collection is done with a member called `tasksupport`. 

    For example:
    
    ```scala
    import java.util.concurrent.ForkJoinPool    
    import scala.collection.parallel.ForkJoinTaskSupport
    import scala.collection.parallel.immutable.ParVector

    val aParVector = ParVector[Int](1,2,3)
    aParVector.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(2))
    ```
    
    Most commonly, we will use `tasksupport` with a `new ForkJoinTaskSupport(new ForkJoinPool(n))` with the required
    number of threads.
    
    To implement a custom class that extends the `TaskSupport` trait, the following methods need to be implemented:
    
    - `def execute[R, Tp](fjtask: Task[R, Tp]): () => R`: Schedules a thread to run in parallel. 
    
    - `executeAndWaitResult[R, Tp](task: Task[R, Tp]): R`: Schedules a thread to run in parallel, blocking until a 
    result is available.
    
    - `def parallelismLevel: Int`: The number of CPU cores that this should run
    
    - `val environment: AnyRef`: The actual manager that manages the threads.

#### Atomic Ops and References

An atomic operation is one that cannot be divided - it either runs fully or not at all, meaning it cannot be intercepted
by another thread.

The atomic types type have the property that they have atomic operations. 

For example:

- the `get` method is thread safe. Meaning that when extracting the value inside the atomic reference, no other thread 
can read or write to it.

- the `set` method.

- `getAndSet` extracts the current value and setting it to a new value in a thread safe way.

- `compareAndSet` sets the value if the reference equality (shallow equality) is true.

- `updateAndGet` & `getAndUpdate` applies a function to the current value and sets the value to the result (in opposite
    order).

- `accumulateAndGet` & `getAndAccumulate` which applies an accumulator.

## Section 4 - Implicits

### Introduction

Implicits allows to implement robust frameworks and intuitive APIs and advanced functional programming concepts.

There are 2 main uses for implicits:

1. Implicit conversions:
    
    ```scala
    case class Person(name: String) {
     def greet = s"Hi, my name is $name"
    }
    
    // convert a string to a person
    implicit def fromStringToPerson(str: String): Person = Person(str)
    
    println("Peter".greet) // compiler converts to println(fromStringToPerson("Peter").greet)
    ``` 
    
    The compiler will look for any class that has a greet method and look for a conversion method from String to that 
    class and will use that to instantiate that class. 
    If a match is found with more than 1 class, the compiler will fail. 
    
2. Implicit parameters:

    ```scala
    def increment(x: Int)(implicit amount: Int) = x + amount
    implicit val defaultAmount = 10 // NOT the same as default arguments
    increment(2)
    ```

In this section we will discuss implicits in depth.

### Organizing Implicits

We will examine Implicits through an example of List sorting.

The `sorted` method takes an implicit parameter called `ordering`.

When we write `List(1,4,5,3,2).sorted` the compiler will look for the implicit `ordering` value inside the 
`scala.Predef` which is automatically imported to any scala application.

However any implicit that we define ourselves will take precedence over the implicits defined inside `scala.Predef`.

We will discuss:

1. How Does the compiler looks for implicits and where?

2. Which implicits have a priority over others?

**Note:** Implicit (used as implicit parameters) can be:

- val/var

- object

- accessor methods - defs with no parentheses

#### Implicit Scope

The implicit scope is composed of several parts and there are rules that prioritize some rules over others.

The scopes are (from highest to lowest priority):
 
1. Normal Scope = LOCAL SCOPE

2. Imported Scope

3. Companions of all types involved in the method signature


#### Best Practices

When defining an implicit cal

1. If: 
    
    - There is a single possible value for it.
    
    - The code for the type can be edited. 
    
    ----> define the implicit in the **companion object** for that type.

2. If: 
    
    - There are 2 possible values for it, but a single "good" one that applies to most cases
    
    - The code for the type can be edited.  
    
    ----> define the "good" implicit in the **companion object** 
    
    ----> define the other implicit elsewhere (preferably in the local scope).

3. If there are many possible values
    
    ----> package the implicits separately in different objects, and make the user explicitly import them.

### Type Classes

A type class is a trait that takes a type and describes what operations can be applied to that type.

Say we want to implement a trait `HTMLWritable` which has a `toHTML` method, and we want to explore different code 
design approaches to do this.

1. Create a class and implement `toHTML`:

    ```scala
    trait HTMLWritable {
       def toHTML: String
    }
    case class User(name: String, age: Int, email: String) extends HTMLWritable {
       def toHTML: String = s"<div>$name ($age yo) <a href='$email'/></div>"
    }
    User("John", 32, "john@rockthejvm.com").toHTML
    ```
    
    There are 2 big disadvantages to this approach:
     
    1. Only works for the types WE write (to apply on other types we would have to implement converters)
     
    2. Only ONE implementation out of quite a number


2. Pattern matching:

    ```scala
    trait HTMLWritable {
       def toHTML: String
    }
    case class User(name: String, age: Int, email: String) extends HTMLWritable {
       def toHTML: String = s"<div>$name ($age yo) <a href='$email'/></div>"
    }
    object HTMLSerializePM {
       def serializeToHtml(value: Any)= value match {
         case User(n,a,e) =>  s"<div>$n ($a yo) <a href='$e'/></div>"
         case java.util.Date => "This is a Date"
         case _ =>
       }
    }
    ```
    
    Disadvantages:
    
    1. Lost type safety
    
    2. Need to modify this code every time
    
    3. still ONE implementation

3. Use a trait with a type parameter and extend it to the `User` type:

    ```scala
    case class User(name: String, age: Int, email: String)
    trait HTMLSerializer[T] {
       def serialize(value: T): String
    }
    object UserSerializer extends HTMLSerializer[User] {
       def serialize(user: User): String = s"<div>${user.name} (${user.age} yo) <a href='${user.email}'/></div>"
    }
    val john = User("John", 32, "john@rockthejvm.com")
    ```
    
    Advantages:
    
    1. We can define serializers for other types (even ones that we didn't create).
        
        ```scala
        trait HTMLSerializer[T] {
           def serialize(value: T): String
        }
        import java.util.Date
        object DateSerializer extends HTMLSerializer[Date] {
            override def serialize(date: Date): String = s"<div>${date.toString()}</div>"
        }
        ```
        
    2. We can define MULTIPLE serializers:
    
        ```scala
        case class User(name: String, age: Int, email: String)
        trait HTMLSerializer[T] {
           def serialize(value: T): String
        }
        object PartialUserSerializer extends HTMLSerializer[User] {
           def serialize(user: User): String = s"<div>${user.name}</div>"
        }
        ```
        
    This `HTMLSerializer` is called a **Type Class**

A **Type Class** specifies a set of operations that can be applied to a given type.

All the implementors of a type class are called **Type Class Instances** (even though they are types themselves).

A **normal** class describes a collections of methods and properties that something **must have** in order 
to belong to that specific type.

A **Type Class** lifts this concept to a higher level applying it to **types**. 
So it describes a collection of properties or methods that a **type** must have in order to belong to that type class.

In general, a typical Type Class looks like this:

```scala
trait MyTypeClassTemplate[T] {
  def action(value: T): String
}
```

#### Implicits with Type Classes

Type Class instances can be used implicitly, meaning the compiler will infer which Type class instance is appropriate 
without having to say explicitly.

```scala
trait HTMLSerializer[T] {
  def serialize(value: T): String
}
object HTMLSerializer {
  def serialize[T](value: T)(implicit serializer: HTMLSerializer[T]): String  = serializer.serialize(value)
  def apply[T](implicit serializer: HTMLSerializer[T]) = serializer
}
case class User(name: String, age: Int, email: String)

implicit object IntSerializer extends HTMLSerializer[Int] {
  override def serialize(value: Int): String = s"<div style='color:blue'>$value</div>"
}
implicit object UserSerializer extends HTMLSerializer[User] {
  override def serialize(user: User): String = s"<div>${user.name} (${user.age} yo) <a href='${user.email}'/></div>"
}
val john = User("John", 32, "john@rockthejvm.com")
println(HTMLSerializer.serialize(42)) // IntSerializer is implied
println(HTMLSerializer.serialize(john)) // UserSerializer is implied 
```

### Type Enrichment (Pimping)

Declaring a class as `implicit` makes it an **implicit class**.

Implicit classes must have 1 argument.

Example: 

```scala
implicit class RichInt(value: Int) {
  def isEven: Boolean = value % 2 == 0
  def sqrt: Double = Math.sqrt(value)
}
new RichInt(42).sqrt // the "standard" way
42.isEven // the implicit way
```

Other examples of rich types that are used in practice:

```scala
1 to 10 // RichInt

import scala.concurrent.duration._
3.seconds
```

**Note**: The compiler doesn't do multiple searches.

```scala
implicit class RichInt(val value: Int) extends AnyVal {
  def isEven: Boolean = value % 2 == 0
  def sqrt: Double = Math.sqrt(value)
}
implicit class RicherInt(richInt: RichInt) {
  def isOdd: Boolean = richInt.value % 2 != 0
}
42.isEven // compiler converts to new RichInt(42).isEven
// 42.isOdd // does not compile
```

#### Pimp libraries Responsibly

Implicit conversions are discouraged because if they cause a bug, they are VERY hard to trace back.

##### General tips for type enrichments:

- Keep type enrichments to **implicit classes** and **type classes**.

- Avoid implicit defs as much as possible.

- Package implicits clearly, bring into scope only what you need.

- If you abslutely need conversions - make them as specific as possible.
 