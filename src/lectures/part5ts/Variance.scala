package lectures.part5ts

class Variance extends App {
  trait Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  // what is variance?
  // "inheritance" - type substitution of generics

  class Cage[T]

  // covariance
  class CCage[+T]
  val ccage: CCage[Animal] = new CCage[Cat]

  // invariance
  class ICage[T]
  //  val icage: ICage[Animal] = new ICage[Cat] - Does not compile

  // contravariance
  class XCage[-T]
  val xCage: XCage[Cat] = new XCage[Animal]


  class InvariantCage[T](val animal: T) // invariant

  // covariant positions
  class CovariantCage[+T](val animal: T) // the animal val in of a covariant position

  // class ContravariantCage[-T](val animal: T) // Does NOT compile
  // Error:(32, 35) contravariant type T occurs in covariant position in type => T of value animal

  /*
  val catCage: XCage[Cat] = new CovariantCage[Animal](new Crocodile)
   */

  // Also applies to vars
  // class CovariantVariableCage[-T](var animal: T) // types of vars are in contravariant position
  /*
  val cCage: CCage[Animal] = new CCage[Cat](new Cat)
  cCage.animal = new Crocodile
   */

  // class ContraVariantVariableCage[-T](var animal: T) // also doesn't compile

  class InvariantVariableCage[T](var animal: T) // OK

  //  trait AnotherCovariantCage[+T] {
  //    def addAnimal(animal: T) // does not compile - Contravariant position
  //  }

  /*
  val ccage: CCage[Animal] = new CCage[Dog]
  ccage.add(new Cat) // cats and dogs in the same cage?! No No
   */

  class AnotherContraVariantCage[-T] {
    def addAnimal(animal: T) = true
  }
  val acc: AnotherContraVariantCage[Cat] = new AnotherContraVariantCage[Animal]
  // acc.addAnimal(new Dog) - No Good
   acc.addAnimal(new Cat)
  class Kitty extends Cat
  acc.addAnimal(new Kitty)

  class MyList[+A] {
    def add[B >: A](elemet: B): MyList[B] = new MyList[B] // widening the type
  }

  val emptyList = new MyList[Kitty]
  val animals: MyList[Kitty] = emptyList.add(new Kitty) // fine - new Kitty is of the type Kitty
  val moreAnimals = animals.add(new Cat) // The Compiler is happy because cat is a supertype of Kitty
  val evenMoreAnimals = moreAnimals.add(new Dog) // compiler widens MyList[Cat] to MyList[Animal]

  // Method arguments are in contravariant positions

  // return types
  class PetShop[-T] {
    // def get(isItAPuppy: Boolean): T // Method return types are in covariant positions
    /*
    cal catShop = new PetShop[Animal] {
      def get(isItAPuppy: Boolean): Animal = new Cat
    }
    val dogShop: PetShop[Dog] = catShop
    dogShop.get(true) // EVIL CAT
     */

    // How do we solve this
    def get[S <: T](isItAPuppy: Boolean, defaultAnimal: S): S = defaultAnimal
  }

  val shop: PetShop[Dog] = new PetShop[Animal]
  // val evilCat = shop.get(true, new Cat) // illegal
  class TerraNova extends Dog
  val bigFurry = shop.get(true, new TerraNova)

  /*
  Big Rule:
   - method arguments are in contravariant position
   - return types are in covariant position
   */

}
