package skywolf46.atmospherereentry.api.packetbridge

import io.netty.buffer.ByteBuf
import org.koin.core.component.KoinComponent
import java.lang.reflect.Field

abstract class DataSerializerBase<DATA : Any> : KoinComponent {

    abstract fun serialize(buf: ByteBuf, dataBase: DATA)
    abstract fun deserialize(buf: ByteBuf): DATA

    override fun hashCode(): Int {
        return this.javaClass.name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return javaClass == other?.javaClass
    }
}