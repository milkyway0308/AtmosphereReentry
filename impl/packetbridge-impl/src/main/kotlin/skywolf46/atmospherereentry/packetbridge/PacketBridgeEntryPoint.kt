package skywolf46.atmospherereentry.packetbridge

import io.github.classgraph.ClassInfoList
import io.netty.buffer.ByteBuf
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerRegistry
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClient
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeHost
import skywolf46.atmospherereentry.api.packetbridge.annotations.NetworkSerializer
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.readString
import skywolf46.atmospherereentry.api.packetbridge.util.serializeTo
import skywolf46.atmospherereentry.api.packetbridge.util.writeString
import skywolf46.atmospherereentry.common.api.annotations.EntryPointContainer
import skywolf46.atmospherereentry.common.api.annotations.EntryPointWorker
import skywolf46.atmospherereentry.packetbridge.serializers.AutoReflectedSerializer
import skywolf46.atmospherereentry.packetbridge.serializers.SerializerRegistryImpl
import skywolf46.atmospherereentry.packetbridge.util.log
import skywolf46.atmospherereentry.packetbridge.util.logError
import kotlin.reflect.KClass

@EntryPointContainer
class PacketBridgeEntryPoint : KoinComponent {
    val version = javaClass.classLoader?.getResourceAsStream("version.txt")?.use {
        it.bufferedReader().readLine()
    } ?: "Unknown"

    @EntryPointWorker
    fun onPacketBridgeLoad() {
        val loadStartTime = System.currentTimeMillis()
        log("..PacketBridge version $version")
        log("Starting initialization...")
        log("Initializing default components...")
        initializeDefaultComponents()
        log("Registering default serializers...")
        registerDefaultSerializers()
        log("Registering user-defined serializers...")
        registerSerializers()
        log("Initialization completed in ${System.currentTimeMillis() - loadStartTime}ms")
    }

    private fun initializeDefaultComponents() {
        loadKoinModules(module {
            single<DataSerializerRegistry> { SerializerRegistryImpl() }
            factory { AutoReflectedSerializer<Any>(it[0], it[1]) }
            factory<PacketBridgeClient> { PacketBridgeClientImpl(it[0], it[1], it[2], it[3]) }
            factory<PacketBridgeHost> { PacketBridgeServerImpl(it[0], it[1], it[2]) }
        })
    }

    private fun registerDefaultSerializers() {
        get<DataSerializerRegistry>().apply {
            registerSerializer(String::class.java, object : DataSerializerBase<String>() {
                override fun serialize(buf: ByteBuf, dataBase: String) {
                    buf.writeString(dataBase)
                }

                override fun deserialize(buf: ByteBuf): String {
                    return buf.readString()
                }
            })
            registerSerializer(Int::class.java, object : DataSerializerBase<Int>() {
                override fun serialize(buf: ByteBuf, dataBase: Int) {
                    buf.writeInt(dataBase)
                }

                override fun deserialize(buf: ByteBuf): Int {
                    return buf.readInt()
                }
            })
            registerSerializer(Long::class.java, object : DataSerializerBase<Long>() {
                override fun serialize(buf: ByteBuf, dataBase: Long) {
                    buf.writeLong(dataBase)
                }

                override fun deserialize(buf: ByteBuf): Long {
                    return buf.readLong()
                }
            })
            registerSerializer(Short::class.java, object : DataSerializerBase<Short>() {
                override fun serialize(buf: ByteBuf, dataBase: Short) {
                    buf.writeShort(dataBase.toInt())
                }

                override fun deserialize(buf: ByteBuf): Short {
                    return buf.readShort()
                }
            })
            registerSerializer(Byte::class.java, object : DataSerializerBase<Byte>() {
                override fun serialize(buf: ByteBuf, dataBase: Byte) {
                    buf.writeByte(dataBase.toInt())
                }

                override fun deserialize(buf: ByteBuf): Byte {
                    return buf.readByte()
                }
            })
            registerSerializer(Float::class.java, object : DataSerializerBase<Float>() {
                override fun serialize(buf: ByteBuf, dataBase: Float) {
                    buf.writeFloat(dataBase)
                }

                override fun deserialize(buf: ByteBuf): Float {
                    return buf.readFloat()
                }
            })
            registerSerializer(Double::class.java, object : DataSerializerBase<Double>() {
                override fun serialize(buf: ByteBuf, dataBase: Double) {
                    buf.writeDouble(dataBase)
                }

                override fun deserialize(buf: ByteBuf): Double {
                    return buf.readDouble()
                }
            })
            registerSerializer(Boolean::class.java, object : DataSerializerBase<Boolean>() {
                override fun serialize(buf: ByteBuf, dataBase: Boolean) {
                    buf.writeBoolean(dataBase)
                }

                override fun deserialize(buf: ByteBuf): Boolean {
                    return buf.readBoolean()
                }
            })
        }

    }

    private fun registerSerializers() {
        log("..Scanning user-defined serializers")
        val definedSerializer = scanDefinedSerializers()
        log("..Scanning reflected serializers")
        val reflectedSerializer = scanReflectiveSerializer()
        log("..Serializer registration completed. ($definedSerializer defined, $reflectedSerializer reflected)")
        log("..Initializing lazy serializers")
        get<DataSerializerRegistry>().wakeLazySerializers()
    }

    private fun scanDefinedSerializers(): Int {
        return get<ClassInfoList>().filter { it.getAnnotationInfo(NetworkSerializer::class.java) != null }.count {
            runCatching {
                val targetClass = it.loadClass()
                if (!DataSerializerBase::class.java.isAssignableFrom(targetClass)) {
                    logError("Class ${targetClass.name} is annotated with @NetworkSerializer but not implementing DataSerializerBase")
                    return@runCatching false
                }
                return@runCatching checkAndRegisterSerializer(targetClass)
            }.onFailure { exception ->
                logError("Failed to register serializer for ${it.name} : ${exception.message}")
            }.getOrDefault(false)
        }
    }

    private fun checkAndRegisterSerializer(cls: Class<*>): Boolean {
        return registerObjectSerializer(cls) || registerClassSerializer(cls)
    }

    private fun registerObjectSerializer(cls: Class<*>): Boolean {
        get<DataSerializerRegistry>().registerSerializer(
            cls,
            cls.kotlin.objectInstance as? DataSerializerBase<out Any>
                ?: return false
        )
        return true
    }

    private fun registerClassSerializer(cls: Class<*>): Boolean {
        // Check class has empty constructor
        if (cls.constructors.none { it.parameterCount == 0 }) {
            logError("Class ${cls.name} is annotated with @NetworkSerializer but do not have empty constructor")
            return false
        }
        val targetClass =
            cls.kotlin.supertypes.find { it.classifier is KClass<*> && it.classifier == DataSerializerBase::class }!!.arguments[0].type!!.classifier!! as KClass<*>
        get<DataSerializerRegistry>().registerSerializer(
            targetClass.java,
            cls.getConstructor().newInstance() as DataSerializerBase<out Any>
        )
        return true
    }

    private fun scanReflectiveSerializer(): Int {
        return get<ClassInfoList>().filter { it.getAnnotationInfo(ReflectedSerializer::class.java) != null }.count {
            runCatching {
                val targetClass = it.loadClass()
                get<DataSerializerRegistry>().registerSerializer(
                    targetClass,
                    lazy {
                        AutoReflectedSerializer(
                            targetClass,
                            targetClass.getAnnotation(ReflectedSerializer::class.java).type
                        )
                    }
                )
            }.onFailure { exception ->
                logError("Failed to register serializer for ${it.name} : ${exception.message}")
            }.isSuccess
        }
    }


}