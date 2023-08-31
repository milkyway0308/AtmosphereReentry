package skywolf46.atmospherereentry.api.packetbridge.util

import arrow.core.getOrElse
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import org.koin.mp.KoinPlatformTools
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerRegistry
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType

private val EMPTY_ARRAY = byteArrayOf()

fun Any.serializeRootTo(target: ByteBuf, buffer: ByteBuf, header: ByteArray = EMPTY_ARRAY) {
    KoinPlatformTools.defaultContext().get().get<DataSerializerRegistry>()
        .acquireSerializer(this::class.java)
        .getOrElse {
            throw IllegalStateException("Cannot serialize ${this::class.java.name} : No serializer found")
        }.also {
            buffer.clear()
            buffer.writeString(this::class.java.name)
            it.serialize(buffer, this)
        }
    target.writeBytes(header)
    target.writeInt(buffer.readableBytes())
    target.writeBytes(buffer)
}

fun Any.serializeRootTo(target: ByteBuf, header: ByteArray = EMPTY_ARRAY) {
    val buffer = ByteBufAllocator.DEFAULT.buffer()
    try {
        serializeRootTo(target, buffer, header)
    } finally {
        buffer.release()
    }
}

fun <T : Any> ByteBuf.deserializeRoot(): T {
    runCatching {
        val container = readBytes(readInt())
        try {
            val targetClass = Class.forName(container.readString())
            return KoinPlatformTools.defaultContext().get().get<DataSerializerRegistry>()
                .acquireSerializer(targetClass)
                .getOrElse {
                    throw IllegalStateException("Cannot deserialize ${targetClass.name} : No serializer found")
                }.deserialize(container) as T
        } finally {
            container.release()
        }
    }.getOrElse {
        println("error - ${it.javaClass.name} ( {${it.message})")
        it.printStackTrace()
        throw it
    }
}


fun Any.serializeTo(target: ByteBuf) {
    KoinPlatformTools.defaultContext().get().get<DataSerializerRegistry>()
        .acquireSerializer(this::class.java)
        .getOrElse {
            throw IllegalStateException("Cannot serialize ${this::class.java.name} : No serializer found")
        }.also {
            it.serialize(target, this)
        }
}

fun <T : Any> ByteBuf.deserializeAs(target: Class<*>): T {
    return KoinPlatformTools.defaultContext().get().get<DataSerializerRegistry>()
        .acquireSerializer(target)
        .getOrElse {
            throw IllegalStateException("Cannot deserialize ${target.name} : No serializer found")
        }.deserialize(this) as T
}


fun <T : Any> ByteBuf.deserializeAs(target: DoubleHashedType): T {
    return KoinPlatformTools.defaultContext().get().get<DataSerializerRegistry>()
        .acquireSerializer(target)
        .getOrElse {
            throw IllegalStateException("Cannot deserialize ${target} : No serializer found")
        }.deserialize(this) as T
}

inline fun <reified T : Any> ByteBuf.deserializeAs(): T {
    return deserializeAs(T::class.java)
}