package skywolf46.atmospherereentry.api.packetbridge

import io.netty.buffer.ByteBuf
import org.koin.core.component.KoinComponent

interface DataSerializerBase<DATA : Any> : KoinComponent {
    fun serialize(buf: ByteBuf, dataBase: DATA)

    fun deserialize(buf: ByteBuf): DATA
}