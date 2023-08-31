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
import skywolf46.atmospherereentry.api.packetbridge.packets.client.PacketBroadcast
import skywolf46.atmospherereentry.api.packetbridge.packets.server.PacketActionRejected
import skywolf46.atmospherereentry.api.packetbridge.packets.server.PacketRelay
import skywolf46.atmospherereentry.api.packetbridge.packets.server.event.PacketEventServerAuthRejected
import skywolf46.atmospherereentry.api.packetbridge.packets.server.event.PacketEventServerIdentified
import skywolf46.atmospherereentry.api.packetbridge.util.JwtProvider
import skywolf46.atmospherereentry.common.api.UnregisterTrigger
import skywolf46.atmospherereentry.events.api.EventManager
import skywolf46.atmospherereentry.packetbridge.handler.ErrorHandler
import skywolf46.atmospherereentry.packetbridge.handler.PacketDecoder
import skywolf46.atmospherereentry.packetbridge.handler.PacketEncoder
import skywolf46.atmospherereentry.packetbridge.handler.PacketTriggerHandler
import skywolf46.atmospherereentry.packetbridge.packets.client.PacketRequestIdentify
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketIdentifyComplete
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketIdentifyDenied
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketRequireIdentify
import skywolf46.atmospherereentry.packetbridge.util.log
import java.util.*
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

class PacketBridgeServerImpl(
    private val port: Int,
    private val jwtProvider: JwtProvider?,
    private val listenerType: ListenerType = ListenerType.Reflective.asServer(),
    val bossGroup: EventLoopGroup = NioEventLoopGroup(),
    val childGroup: EventLoopGroup = NioEventLoopGroup()
) : PacketBridgeHost, KoinComponent {
    private val handlers = mutableMapOf<UUID, PacketBridgeProxy>()
    private val identifiedHandlers = mutableMapOf<String, PacketBridgeProxy>()
    private val eventManager = get<EventManager>().createEventManager()
    private val serverUUID = UUID.randomUUID()
    private val lock = ReentrantReadWriteLock()

    init {
        init()
        start()
    }

    private fun init() {
        addWrappedListener(PacketRequestIdentify::class.java, 0) {
            if (jwtProvider == null) {
                lock.write {
                    identifiedHandlers[it.packet.authenticateKey] = handlers[it.from]!!.apply {
                        this.identifiedId = it.packet.authenticateKey
                    }
                }
                println("Skipping verification. Using authentication key as name instead.")
                it.reply(PacketIdentifyComplete(it.packet.authenticateKey))
                trigger(PacketEventServerIdentified(it.packet.authenticateKey, handlers[it.from]!!))
            } else {
                jwtProvider.checkIdentifier(it.packet.authenticateKey).onLeft { e ->
                    println("Client failed to identify: ${e.message} (UUID ${it.from})")
                    it.reply(PacketIdentifyDenied(e.message!!))
                    trigger(PacketEventServerAuthRejected(handlers[it.from]!!))
                }.onRight { key ->
                    lock.write {
                        identifiedHandlers[it.packet.authenticateKey] = handlers[it.from]!!.apply {
                            this.identifiedId = key
                        }
                    }
                    println("Client identified as $key (UUID ${it.from})")
                    it.reply(PacketIdentifyComplete(key))
                    trigger(PacketEventServerIdentified(key, handlers[it.from]!!))
                }
            }
        }

        addWrappedListener(PacketBroadcast::class.java, 0) {
            getProxy(it.from)?.apply {
                if (!this.isIdentified()) {
                    it.reply(PacketActionRejected("Client not identified yet."))
                } else {
                    lock.read {
                        identifiedHandlers.filter { x -> x.key != this.identifiedId }.values
                    }.forEach { proxy ->
                        proxy.send(PacketWrapper(PacketRelay(it.packet.packetContainer), it.from))
                    }
                }
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
                    lock.write {
                        handlers[connection.uuid.apply {
                            log("New incoming connection detected as $this (${ch.remoteAddress()}), waiting for identification")
                        }] = connection
                    }
                    connection.send(PacketRequireIdentify(connection.uuid))
                }
            })
        }
        listenerType.onRegister(this@PacketBridgeServerImpl)
        bootStrap.bind(port).sync()
    }

    override fun getProvider(): JwtProvider? {
        return jwtProvider
    }

    override fun broadcast(vararg packetBase: PacketBase) {
        val remapped = packetBase.map { PacketWrapper(it, this.serverUUID, -1L) }.toTypedArray()
        identifiedHandlers.values.forEach {
            it.send(*remapped)
        }
    }

    override fun sendTo(target: PacketBridgeClientConnection, vararg packets: PacketBase) {
        for (x in packets) {
            target.send(x)
        }
    }

    fun getProxy(uuid: UUID): PacketBridgeProxy? {
        return lock.read {
            handlers[uuid]
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
                updateReplier { packetWrapper, packetToReply ->
                    // TODO - Add packet send verifier
                    val target = handlers[packetWrapper.from] ?: return@updateReplier
                    sendTo(target, PacketWrapper(packetToReply, serverUUID, packetWrapper.packetId))
                }
            }, packetBase.packet.javaClass.name)
            // Wave 2 - Trigger original packet
            eventManager.callEvent(packetBase.packet)
        } else {
            eventManager.callEvent(packetBase)
        }
    }
}