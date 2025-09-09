package typesystem

object Variance {

    abstract class Pet
    class Dog(name: String) : Pet()
    class Cat(name: String) : Pet()

    // Dog extends Pet => List<Dog> "extends" List<Pet>?
    // typesystem.Variance question for the List type: A extends B => List<A> extends List<B>?
    // yes => List is a COVARIANT TYPE
    // Dog is a Pet => List<Dog> is a List<Pet>

    val lassie = Dog("Lassie")
    val hachi = Dog("Hachi")
    val laika = Dog("Laika")
    val myDogs: List<Dog> = listOf(lassie, hachi, laika)
    val myPets: List<Pet> = myDogs // legal

    // Covariant generic types - values as a more general type
    class MyList<out A> // <out A> => COVARIANT IN A

    val aListOfPets: MyList<Pet> = MyList<Cat>() // legal

    // no - INVARIANT
    interface Combiner<A> { // semigroup
        fun combine(x: A, y: A): A
    }

    // java standard library - all Java generics are invariant
    // val aJavaList: java.util.List<Pet> = java.util.ArrayList<Dog>() // type mismatch

    // Contravariance - Variance in the other direction - values as a more specific type
    // Dog is a Pet, then Vet<Pet> is a Vet<Dog>
    class Vet<in A> {
        fun heal(pet: A): Boolean = true
    }

    val myVet: Vet<Dog> = Vet<Pet>()

    // covariant types "produce" or "get" elements => "output" elements
    // contravariant types "consume" or "act on" elements => "input" elements

    /**
        Rule of thumb, how to decide variance:
        - if it "outputs" elements => COVARIANT (out)
        - if it "consumes" elements => CONTRAVARIANT (in)
        - otherwise, INVARIANT (no modifier) -- no relation
     */

    /**
        Exercise: add variance modifiers
     */
    class RandomGenerator<out A> //covariant in type A
    class MyOption<out A> // holds at most one item or nothing -- covariant in A
    class JSONSerialiser<in A> // turns values of type A into JSONs -- contravariant in A
    interface MyFunction<in A, out B> // Takes a value of type A and returns a B -- contravariant in B and covariant in A

    /**
     * Exercise:
     * 1) add variance modifiers where appropriate
     * 2) EmptyList should be empty regardless of the type - try making it an object --- empty type is the bottom in Kotlin and....
     * 3) add an "add" method to the generic list type
     *      fun add(element: A): LList<A>
     */

    //starting code
    abstract class LList<out A> {
        abstract fun head(): A // first item in the list
        abstract fun tail(): LList<A>
        fun add(elem: @UnsafeVariance A): LList<A> = TODO()
    }

    fun <B, A:B> LList<A>.add(elem: B): LList<B> =
        Cons(elem, this)

    data object EmptyList: LList<Nothing>() { // This is a subtype of all possible lists
        override fun head(): Nothing = throw NoSuchElementException()
        override fun tail(): LList<Nothing> = throw NoSuchElementException()
    }

    data class Cons<out A>(val h: A, val t: LList<A>): LList<A>() {
        override fun head(): A = h
        override fun tail(): LList<A> = t
    }

    val myLPets: LList<Pet> = EmptyList
    val myStrings: LList<String> = EmptyList

    @JvmStatic
    fun main(args: Array<String>) {

    }

}