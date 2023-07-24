package skywolf46.atmospherereentry.packetbridge.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.PacketListenable
import skywolf46.atmospherereentry.common.api.util.printError

class PacketTriggerHandler(private val listenable: PacketListenable) : ChannelInboundHandlerAdapter() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is PacketBase)
            listenable.trigger(msg)
        else {
            printError("Unknown packet type : ${msg::class.simpleName}")
        }
    }
}