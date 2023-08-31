package skywolf46.atmospherereentry.api.packetbridge.util

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator

fun ByteBuf.writeString(string: String) {
    string.toByteArray().apply {
        writeInt(size)
        writeBytes(this)
    }
}

fun ByteBuf.readString(): String {
    return String(ByteArray(readInt()).apply {
        readBytes(this)
    })
}

fun <R : Any> ByteBuf.use(unit: (ByteBuf) -> R): R {
    return unit(this).apply {
        release()
    }
}

fun <R : Any> useByteBuf(unit: (ByteBuf) -> R): R {
    return ByteBufAllocator.DEFAULT.heapBuffer().use(unit)
}

fun useAndReadByteBuf(unit: (ByteBuf) -> Unit) : ByteArray {
    return useByteBuf {
        unit(it)
        ByteArray(it.readableBytes()).apply {
            it.readBytes(this)
        }
    }
}