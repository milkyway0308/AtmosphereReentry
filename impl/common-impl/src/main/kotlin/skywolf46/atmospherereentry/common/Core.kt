package skywolf46.atmospherereentry.common

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ClassInfoList
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module
import skywolf46.atmospherereentry.common.api.annotations.*
import skywolf46.atmospherereentry.common.api.util.printError
import skywolf46.atmospherereentry.common.api.util.waitStackTrace
import skywolf46.atmospherereentry.common.util.StopWatch
import java.util.concurrent.atomic.AtomicBoolean

@EntryPointContainer
object Core : KoinComponent {
    private val entryPoints = mutableListOf<String>()
    private val settingInstances = mutableMapOf<String, Any>()
    private val loadedSettingInstances = mutableSetOf<String>()
    private val classLoaders = mutableMapOf<String, MutableList<ClassLoader>>()
    private val stopWatch = StopWatch()
    private val isStarted = AtomicBoolean(false)

    private val classGraph by lazy {
        ClassGraph().enableClassInfo().enableAnnotationInfo().enableMethodInfo()
    }

    @JvmStatic
    fun main(args: Array<String>) {
        initialize()
    }

    @Synchronized
    @JvmStatic
    fun initialize() {
        if (isStarted.get()) return
        isStarted.set(true)
        println("AtmosphereReentry - Deep dive into atmosphere")
        startKoin { }
        println("Starting core heartbeat impulse...")
        stopWatch.start("Impulse")
        impulse()
        println(
            "Impulse completed. Found total ${settingInstances.size} class loader container and ${classLoaders.size} class loaders. (${
                stopWatch.logAndGet(
                    "Impulse"
                )
            }ms)"
        )
        println("Rerouting entry points...")
        println("Applying scanned class graph for optimization...")
        loadKoinModules(module {
            single<ClassInfoList> { classGraph.scan().allClasses }
        })
        rerouteEntrypoint(get<ClassInfoList>())
        println("Awakening...")
        stopWatch.start("Awakening")
        awakeAll()
        println("Awakening completed. (${stopWatch.logAndGet("Awakening")}ms)")
        println("All system online.")
    }

    private fun impulse() {
        classLoaders["Core"] = mutableListOf(Core::class.java.classLoader)
        println("..Impulse #0 : Initial impulse.")
        impulse(1)
    }


    private tailrec fun impulse(iteration: Int) {
        if (settingInstances.size != gatherAllInstanceFrom().run { settingInstances.size }) {
            extractClassLoader()
            println("..Impulse #$iteration: Found total ${settingInstances.size} class loader containers and ${classLoaders.size} class loaders.")
            impulse(iteration + 1)
        }
    }

    private fun extractClassLoader() {
        settingInstances.forEach {
            if (it.key in loadedSettingInstances) return@forEach
            loadedSettingInstances.add(it.key)
            fetchClassLoaders(it.key, it.value)
        }
    }

    private fun fetchClassLoaders(className: String, instance: Any) {
        for (x in instance.javaClass.declaredMethods) {
            if (x.getAnnotation(ClassLoaderWorker::class.java) != null) {
                if (x.parameterCount != 0) {
                    printError("..ClassLoaderFunction ${className}#${x.name} require no parameter, but has ${x.parameterCount} parameters. Skipping...")
                    continue
                }
                if (x.returnType != ClassLoader::class.java && x.returnType != List::class.java) {
                    printError("..ClassLoaderFunction ${className}#${x.name} has invalid return type. Skipping...")
                    continue
                }
                x.isAccessible = true
                addClassLoaderTo(
                    className, try {
                        x.invoke(instance)
                    } catch (e: Exception) {
                        val ignoreInfo = x.getAnnotation(IgnoreException::class.java)
                        if (ignoreInfo == null || (ignoreInfo.type.isNotEmpty() && ignoreInfo.type.none { type ->
                                type.java.isAssignableFrom(e.javaClass)
                            })) {
                            println("..ClassLoaderFunction ${className}#${x.name} has thrown exception. Skipping...")
                            e.waitStackTrace()
                        }
                        continue
                    }
                )
            }
        }
    }

    private fun addClassLoaderTo(key: String, classLoaders: Any) {
        when (classLoaders) {
            is ClassLoader -> {
                this.classLoaders.getOrPut(key) { mutableListOf() }.add(classLoaders)
            }

            is List<*> -> {
                classLoaders.forEach {
                    if (it is ClassLoader) {
                        this.classLoaders.getOrPut(key) { mutableListOf() }.add(it)
                    } else {
                        printError("..ClassLoaderFunction $key has invalid return type. Skipping...")
                    }
                }
            }

            else -> {
                printError("..ClassLoaderFunction $key has invalid return type. Skipping...")
            }
        }
    }

    private fun gatherAllInstanceFrom() {
        stopWatch.start("Impulse wave")
        classGraph.overrideClassLoaders(*classLoaders.values.flatten().toTypedArray()).scan().use { scan ->
            for (classData in scan.allClasses) {
                if (classData.hasAnnotation(ClassLoaderContainer::class.java)) {
                    if (classData.name !in settingInstances) {
                        verifyAndLoadClassLoaderContainer(classData)
                    }
                    if (classData.hasAnnotation(EntryPointContainer::class.java)) {
                        printError("..Impulse wave: Found new entry point ${classData.name}, but defined as class loader container.")
                    }
                } else if (classData.hasAnnotation(EntryPointContainer::class.java) && classData.name !in entryPoints) {
                    entryPoints += classData.name
                    println("..Impulse wave: Found new entry point ${classData.name}")
                }
            }
        }
        println("....Impulse wave completed. (${stopWatch.logAndGet("Impulse wave")}ms)")
    }

    private fun rerouteEntrypoint(classList: ClassInfoList) {
        val entryPointDepends = entryPoints.associateWith { className ->
            runCatching {
                classList.get(className).getAnnotationInfo(EntryPointContainer::class.java).loadClassAndInstantiate()
            }.onFailure {
                printError("Failed to load entry point $className due to unsatisfied dependency. Skipping...")
            }.getOrNull()
        }.filter { it.value != null }.map { it.key to it.value!! as EntryPointContainer }.toMap()
        // Verify circular dependency of entry point
        for (x in entryPointDepends.keys) {
            if (checkCircularDependency(x, entryPointDepends)) {
                printError("Circular dependency detected for class $x. Skipping...")
            } else {
                entryPoints += x
            }
        }
        // Sort entry points
        entryPoints.sortWith(object : Comparator<String> {
            override fun compare(first: String, second: String): Int {
                val firstDepends = entryPointDepends[first]?.dependsOn?.map { it.qualifiedName!! } ?: emptyList()
                val secondDepends = entryPointDepends[second]?.dependsOn?.map { it.qualifiedName!! } ?: emptyList()
                if (firstDepends.contains(second)) return 1
                if (secondDepends.contains(first)) return -1
                return 0
            }
        })

        for (x in entryPoints) {
            verifyAndLoadEntryPoint(classList.get(x))
        }
    }

    private fun checkCircularDependency(target: String, entryPoints: Map<String, EntryPointContainer>): Boolean {
        val visited = mutableSetOf<String>()
        val stack = mutableListOf<String>()
        stack += target
        while (stack.isNotEmpty()) {
            val current = stack.removeAt(stack.size - 1)
            if (current in visited) return true
            visited += current
            stack += entryPoints[current]?.dependsOn?.map { it.qualifiedName!! } ?: continue
        }
        return false
    }

    private fun verifyAndLoadClassLoaderContainer(classData: ClassInfo) {
        runCatching {
            val cls = classData.loadClass()
            if (cls.kotlin.objectInstance != null) {
                settingInstances[classData.name] = cls.kotlin.objectInstance!!
            } else {
                if (classData.constructorInfo.isEmpty()) throw IllegalStateException("......Failed to load class loader container ${classData.name} : Class ${classData.name} has no constructor.")
                if (classData.constructorInfo.size > 1) throw IllegalStateException("......Failed to load class loader container ${classData.name} : Class ${classData.name} has multiple constructor.")
                if (classData.constructorInfo[0].parameterInfo.isNotEmpty()) throw IllegalStateException("......Failed to load class loader container ${classData.name} : Class ${classData.name} has constructor with parameter.")
                settingInstances[classData.name] =
                    classData.constructorInfo[0].loadClassAndGetConstructor().newInstance()
            }
        }.onFailure {
            printError("......Failed to load class loader container ${classData.name} : ${it.javaClass.name} (${it.message})")
            it.waitStackTrace()
        }
    }

    private fun verifyAndLoadEntryPoint(classData: ClassInfo) {
        println("....Found entry point ${classData.name}")
        runCatching {
            val cls = classData.loadClass()
            if (cls.kotlin.objectInstance != null) {
                settingInstances[classData.name] = cls.kotlin.objectInstance!!
            } else {
                if (classData.constructorInfo.isEmpty()) throw IllegalStateException("......Failed to load entry point ${classData.name} : Class ${classData.name} has no constructor.")
                if (classData.constructorInfo.size > 1) throw IllegalStateException("......Failed to load entry point ${classData.name} : Class ${classData.name} has multiple constructor.")
                if (classData.constructorInfo[0].parameterInfo.isNotEmpty()) throw IllegalStateException("......Failed to load entry point ${classData.name} : Class ${classData.name} has constructor with parameter.")
                settingInstances[classData.name] =
                    classData.constructorInfo[0].loadClassAndGetConstructor().newInstance()
            }
        }.onFailure { cause ->
            val ignoreInfo =
                classData.getAnnotationInfo(IgnoreException::class.java)?.loadClassAndInstantiate() as? IgnoreException
            if (ignoreInfo == null || (!isExceptionMatchedFor(
                    cause, ignoreInfo
                ) && cause.cause?.let { isExceptionMatchedFor(it, ignoreInfo) } == false)
            ) {
                printError("......Failed to load entry point ${classData.name} : ${cause.javaClass.name} (${cause.message})")
                cause.waitStackTrace()
            } else {
                println("......Ignoring ${classData.name} : Registration failed with ${cause.cause?.javaClass ?: cause.javaClass}")
            }
        }
    }

    private fun isExceptionMatchedFor(exception: Throwable, ignoreInfo: IgnoreException): Boolean {
        return ignoreInfo.type.isNotEmpty() && ignoreInfo.type.any { it.java.isAssignableFrom(exception.javaClass) }
    }

    private fun awakeAll() {
        settingInstances.values.forEach { instance ->
            for (method in instance.javaClass.declaredMethods) {
                if (method.getAnnotation(EntryPointWorker::class.java) != null) {
                    if (method.parameterCount != 0) {
                        printError("..EntryPointFunction ${instance.javaClass.name}#${method.name} requires no parameter, but has ${method.parameterCount} parameters. Skipping...")
                        continue
                    }
                    method.isAccessible = true
                    runCatching {
                        method.invoke(instance)
                    }.onFailure { cause ->
                        val ignoreInfo = method.getAnnotation(IgnoreException::class.java)
                        if (ignoreInfo == null || (!isExceptionMatchedFor(
                                cause, ignoreInfo
                            ) && cause.cause?.let { isExceptionMatchedFor(it, ignoreInfo) } == false)
                        ) {
                            printError("..EntryPointFunction ${instance.javaClass.name}#${method.name} failed to invoke.")
                            cause.waitStackTrace()
                        } else {
                            println("..Ignoring EntryPointFunction ${instance.javaClass.name}#${method.name} : Invoke failed with ${cause.javaClass}")
                        }
                    }
                }
            }
        }
    }
}