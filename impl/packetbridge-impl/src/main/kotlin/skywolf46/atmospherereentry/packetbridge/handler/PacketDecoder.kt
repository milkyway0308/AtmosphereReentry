package skywolf46.atmospherereentry.packetbridge.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeRoot

class PacketDecoder : ReplayingDecoder<ByteBuf>() {
    override fun decode(ctx: ChannelHandlerContext, input: ByteBuf, out: MutableList<Any>) {
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
        //         Packet header will be 8 bytes long.
        //
        // After packet identifier, packet data will be sent.
        // Packet will be deserialized as each deserializer from DataSerializerRegistry.
        val packetFlag = input.readByte()
        out.add(when(packetFlag) {
            0x00.toByte() -> readDefaultPacket(input)
            0x01.toByte() -> readSystemPacket(input)
            0x02.toByte() -> readFixedPacket(input)
            0x03.toByte() -> readStreamPacket(input)
            else -> throw IllegalArgumentException("Unknown packet flag $packetFlag")
        })
    }

    private fun readDefaultPacket(input: ByteBuf) : PacketBase {
        return input.deserializeRoot()
    }

    private fun readSystemPacket(input: ByteBuf) : PacketBase {
        val packetLength = input.readInt()
        val packetIdentifier = input.readShort()
        val packetData = ByteArray(packetLength)
        TODO()
    }

    private fun readFixedPacket(input: ByteBuf) : PacketBase {
        val packetLength = input.readInt()
        val packetIdentifier = input.readShort()
        val packetData = ByteArray(packetLength)
        TODO()
    }

    private fun readStreamPacket(input: ByteBuf) : PacketBase {
        TODO()
    }
}