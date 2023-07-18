package skywolf46.atmospherereentry.packetbridge

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClientConnection
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeHost
import skywolf46.atmospherereentry.api.packetbridge.data.ListenerType
import skywolf46.atmospherereentry.api.packetbridge.data.PacketWrapper
import skywolf46.atmospherereentry.common.UnregisterTrigger
import skywolf46.atmospherereentry.events.api.EventManager
import skywolf46.atmospherereentry.packetbridge.handler.ErrorHandler
import skywolf46.atmospherereentry.packetbridge.handler.PacketDecoder
import skywolf46.atmospherereentry.packetbridge.handler.PacketEncoder
import skywolf46.atmospherereentry.packetbridge.handler.PacketTriggerHandler
import skywolf46.atmospherereentry.packetbridge.packets.client.PacketRequestIdentify
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketIdentifyComplete
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketIdentifyDenied
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketRequireIdentify
import skywolf46.atmospherereentry.packetbridge.util.JWTUtil
import skywolf46.atmospherereentry.packetbridge.util.log
import java.util.*

class PacketBridgeServerImpl(
    private val port: Int,
    private val listenerType: ListenerType = ListenerType.Reflective.asServer(),
    val bossGroup: EventLoopGroup = NioEventLoopGroup(),
    val childGroup: EventLoopGroup = NioEventLoopGroup()
) : PacketBridgeHost, KoinComponent {
    private val handlers = mutableMapOf<UUID, PacketBridgeProxy>()
    private val identifiedHandlers = mutableMapOf<String, PacketBridgeProxy>()
    private val eventManager = get<EventManager>().createEventManager()
    private val serverUUID = UUID.randomUUID()

    init {
        init()
        start()
    }

    private fun init() {
        addWrappedListener(PacketRequestIdentify::class.java, 0) {
            JWTUtil.checkIdentifier(it.packet.authenticateKey).onLeft { e ->
                println("Client failed to identify: ${e.message} (UUID ${it.from})")
                it.reply(PacketIdentifyDenied(e.message!!))
            }.onRight { key ->
                println("Client identified as $key (UUID ${it.from})")
                it.reply(PacketIdentifyComplete(key))
            }
        }
    }

    private fun start() {
        val bootStrap = ServerBootstrap().apply {
            group(bossGroup, childGroup)
            channel(NioServerSocketChannel::class.java)
            option(ChannelOption.SO_BACKLOG, 128)
            childHandler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(
                        ErrorHandler(),
                        LengthFieldPrepender(4),
                        PacketEncoder(),
                        LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                        PacketDecoder(),
                        PacketTriggerHandler(this@PacketBridgeServerImpl)
                    )
                    val connection = PacketBridgeProxy(UUID.randomUUID(), ch)
                    handlers[connection.uuid.apply {
                        log("New incoming connection detected as $this (${ch.remoteAddress()}), waiting for identification")
                    }] = connection
                    connection.send(PacketRequireIdentify(connection.uuid))
                }
            })
        }
        listenerType.onRegister(this@PacketBridgeServerImpl)
        bootStrap.bind(port).sync()
    }

    override fun broadcast(vararg packetBase: PacketBase) {
        identifiedHandlers.values.forEach {
            it.send(*packetBase)
        }
    }

    override fun sendTo(target: PacketBridgeClientConnection, vararg packets: PacketBase) {
        for (x in packets) {
            target.send(x)
        }
    }

    override fun <T : PacketBase> addListener(type: Class<T>, priority: Int, listener: (T) -> Unit): UnregisterTrigger {
        return eventManager.registerObserver(type.kotlin, 0, listener as (PacketBase) -> Unit)
    }

    fun <T : PacketBase> addWrappedListener(
        type: Class<T>,
        priority: Int,
        listener: (PacketWrapper<T>) -> Unit
    ): UnregisterTrigger {
        return eventManager.registerObserverWithKey(
            PacketWrapper::class,
            type.name,
            priority,
            listener as (PacketBase) -> Unit
        )
    }


    override fun getPacketListener(): EventManager {
        return eventManager
    }

    override fun trigger(packetBase: PacketBase) {
        if (packetBase is PacketWrapper<*>) {
            // Wave 1 - Trigger with wrapped packet
            eventManager.callEvent(packetBase.apply {
                updateReplier { packetWrapper, packetBase ->
                    // TODO - Add packet send verifier
                    val target = handlers[packetWrapper.from] ?: return@updateReplier
                    sendTo(target, PacketWrapper(packetBase, serverUUID, packetWrapper.packetId))
                }
            }, packetBase.packet.javaClass.name)
            // Wave 2 - Trigger original packet
            eventManager.callEvent(packetBase.packet)
        } else {
            eventManager.callEvent(packetBase)
        }
    }
}