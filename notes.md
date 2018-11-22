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


**Executors**

Threads are really expensive to start an kill, and a simple solution to this is to reuse them. 

The standard library offers a standard API to reuse threads with executors and thread pools.

**Race Conditions**

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

