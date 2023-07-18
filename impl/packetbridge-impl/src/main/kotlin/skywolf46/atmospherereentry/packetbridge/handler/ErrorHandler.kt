package skywolf46.atmospherereentry.packetbridge.handler

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext

class ErrorHandler : ChannelDuplexHandler() {
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
    }
}