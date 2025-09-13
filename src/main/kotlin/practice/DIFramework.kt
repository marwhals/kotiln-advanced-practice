package practice

import javax.naming.ldap.Control
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

object DIFramework {
    // 1 - create 2 new annotations
    // Layer - runtime annotation, can be attached to classes and interfaces ...
    // Inject - runtime annotation, can be attached to property only

    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Layer

    @Target(AnnotationTarget.PROPERTY)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Inject

    // controller - HTTP requests
    // service - business logic
    // repository - data layer

    @Layer
    class Repository {
        fun getData(): String {
            return "data from repository"
        }
    }

    @Layer
    class Service {
        @Inject
        lateinit var repository: Repository

        fun performAction(): String {
            return repository.getData() + " - with some business logic"
        }
    }

    @Layer
    class Controller {
        @Inject
        lateinit var service: Service
        @Inject
        lateinit var users: UserManager
        fun processHTTPRequest(payload: String, username: String = "invalid@rockthejvm.com"): String {
            return if (users.isLoggedIn(username))
                "Processed request! Response: ${service.performAction()}"
            else "Not logged in, request denied"
        }
    }

    @Layer
    class UserManager {
        private val loggedUsers = mutableSetOf<String>()

        fun login(username: String) {
            loggedUsers.add(username)
            println("[log] Logged in as $username")
        }

        fun isLoggedIn(username: String) = username in loggedUsers

        fun logout(username: String) {
            loggedUsers.remove(username)
            println("[log] $username just logged out")
        }
    }

    class DIManager {
        val layers = mutableMapOf<KClass<*>, Any>()

        // Part 2.1 - add a function which registers a class into the layer map
        private fun <T: Any> register(clazz: KClass<T>): Unit {
            // if class has the annotation @Layer, instantiate the class and add the association
            // clazz -> that instance
            if (clazz.findAnnotation<Layer>() != null) {
                val instance = clazz.createInstance()
                layers[clazz] = instance
            }
        }

        // part 2.2 - a function which injects all the dependencies for particular type
        private fun <T: Any> injectDependencies(instance: T) {
            // for the class of T, find all the properties annotated with @Inject
            val clazz = instance::class
            clazz.declaredMemberProperties.forEach { prop ->
                if (prop.findAnnotation<Inject>() != null) {
                    val type = prop.returnType.classifier as KClass<*>
                    val dependency = layers[type]
                    if (dependency != null && prop is KMutableProperty<*>) {
                        prop.setter.call(instance, dependency) // inject
                    }
                    // figure out their types
                    // find the instances in the layers map associated to their type
                    // set that property to that instance
                }
            }
        }

        // part 2.3 - a function to initialise the DI framework
        fun initialise() {
            // find all the classes in the DIFramework object and register them (use .nestedClassed)
            DIFramework::class.nestedClasses.forEach {
                register(it)
            }
            // find all the values in the layers map and inject dependencies on them (use the injectDependencies )
            layers.values.forEach { injectDependencies(it) }
        }

        fun <T: Any> get(clazz: KClass<T>): T? =
            layers[clazz] as? T

        // alternative, but watch for exposing private properties
//        inline fun <reified T: Any> get(): T? =
//            layers[T::class] as? T

    }

    @JvmStatic
    fun main(args: Array<String>) {
        val diManager = DIManager()

        // initialise it
        diManager.initialise()

        // retrieve a service and use
        val controller = diManager.get(Controller::class)
        val users = diManager.get(UserManager::class)
        users?.login("daniel@rockthejvm.com")

        println(controller?.processHTTPRequest("{ \"source\": \"sensors/incremental\"}", "daniel@rockthejvm.com"))

    }
}