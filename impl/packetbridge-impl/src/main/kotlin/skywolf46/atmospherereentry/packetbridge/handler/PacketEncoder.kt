package skywolf46.atmospherereentry.packetbridge.handler

import arrow.core.getOrElse
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerRegistry
import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.util.serializeRootTo

class PacketEncoder : MessageToByteEncoder<PacketBase>(), KoinComponent {

    override fun encode(ctx: ChannelHandlerContext, msg: PacketBase, out: ByteBuf) {
        // Packet start with 4-byte length integer identifier.
        // After packet length, 1 + Optional byte packet identifier will be sent.
        // Packet identifier has the following structure :
        // First byte is packet type. Byte will flag as byte type.
        //  0x00 : Default packet.
        //         Packet header will be 12 bytes long. (4 byte for packet length, 8 byte for packet identifier)
        //  0x01 : Packet is system packet.
        //         Packet header will be 6 bytes long. (4 byte for packet length, 2 byte for packet identifier)
        //  0x02 : Packet has fixed packet identifier.
        //         Packet header will be 6 bytes long. (4 byte for packet length, 2 byte for packet identifier)
        //  0x03 : Packet size is unknown. (Packet will be sent as stream)
        //         No packet header will be sent.
        //
        // After packet identifier, packet data will be sent.
        // Packet will be deserialized as each deserializer from DataSerializerRegistry.
        val buffer = ByteBufAllocator.DEFAULT.buffer()
        try {
            msg.serializeRootTo(out, buffer, byteArrayOf(0x00))
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            buffer.release()
        }
    }
}