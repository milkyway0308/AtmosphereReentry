package skywolf46.atmospherereentry.packetbridge.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import skywolf46.atmospherereentry.api.packetbridge.data.PacketWrapper
import skywolf46.atmospherereentry.packetbridge.PacketBridgeClientImpl
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketIdentifyComplete
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketIdentifyDenied

class IdentificationHandler(private val client: PacketBridgeClientImpl) :
    ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is PacketWrapper<*>) {
            if (msg.packet is PacketIdentifyComplete) {
                ctx.pipeline().remove(this)
                client.identify((msg.packet as PacketIdentifyComplete).serverId)
            }
            if (msg.packet is PacketIdentifyDenied) {
                ctx.pipeline().remove(this)
                println("Identification denied : ${(msg.packet as PacketIdentifyDenied).cause}")
            }
        }
        ctx.fireChannelRead(msg)
    }
}