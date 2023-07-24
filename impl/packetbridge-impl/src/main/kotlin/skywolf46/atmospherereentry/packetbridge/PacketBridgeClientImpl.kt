package skywolf46.atmospherereentry.packetbridge

import arrow.core.Option
import arrow.core.getOrElse
import arrow.core.toOption
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LengthFieldBasedFrameDecoder
import io.netty.handler.codec.LengthFieldPrepender
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import skywolf46.atmospherereentry.api.packetbridge.*
import skywolf46.atmospherereentry.api.packetbridge.data.ListenerType
import skywolf46.atmospherereentry.api.packetbridge.data.PacketWrapper
import skywolf46.atmospherereentry.common.api.UnregisterTrigger
import skywolf46.atmospherereentry.events.api.EventManager
import skywolf46.atmospherereentry.packetbridge.data.ReplyMetadata
import skywolf46.atmospherereentry.packetbridge.handler.*
import skywolf46.atmospherereentry.api.packetbridge.packets.WrappedPacket
import skywolf46.atmospherereentry.packetbridge.packets.client.PacketRequestIdentify
import skywolf46.atmospherereentry.packetbridge.packets.server.PacketRequireIdentify
import skywolf46.atmospherereentry.packetbridge.util.TriggerableFuture
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write
import kotlin.reflect.KClass


class PacketBridgeClientImpl(
    private val host: String,
    private val port: Int,
    private val identifyKey: String,
    val listenerType: ListenerType = ListenerType.Reflective.asClient(),
    val group: EventLoopGroup = NioEventLoopGroup()
) : PacketBridgeClient, KoinComponent {

    private lateinit var channel: io.netty.channel.Channel

    private val replyHandle = mutableMapOf<Long, ReplyMetadata>()

    private val lock = ReentrantReadWriteLock()

    private val packetIdIncremental = AtomicLong(0L)

    private val eventManager = get<EventManager>().createEventManager()


    var identifiedUUID: Option<UUID> = Option.fromNullable(null)
        private set

    init {
        reconnect()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun init() {
        addWrappedListener(PacketRequireIdentify::class.java, 0) {
            this.identifiedUUID = it.packet.uuid.toOption()
            println("PacketBridgeClientImpl - Received identify request")
            println("Approved as stranger (${it.packet.uuid})")
            it.reply(PacketRequestIdentify(identifyKey))
        }
    }

    private fun reconnect() {
        val bootstrap = Bootstrap().apply {
            group(group)
            channel(NioSocketChannel::class.java)
            option(ChannelOption.SO_KEEPALIVE, true)
            handler(object : ChannelInitializer<SocketChannel>() {
                override fun initChannel(ch: SocketChannel) {
                    ch.pipeline().addLast(
                        ErrorHandler(),
                        LengthFieldPrepender(4),
                        PacketEncoder(),
                        LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4),
                        PacketDecoder(),
                        IdentificationHandler(this@PacketBridgeClientImpl),
                        PacketTriggerHandler(this@PacketBridgeClientImpl),
                    )
                }
            })
        }
        init()
        listenerType.onRegister(this)

        channel = bootstrap.connect(host, port).sync().channel()
    }


    override fun send(vararg wrapper: PacketWrapper<*>) {
        for (x in wrapper) {
            channel.write(x)
        }
        channel.flush()
    }

    override fun <T : PacketBase> waitReply(
        packetBase: PacketBase,
        limitation: KClass<T>,
        listener: (T) -> Unit
    ) {
        val id = packetIdIncremental.incrementAndGet()
        lock.write {
            replyHandle[id] = ReplyMetadata(action = listener as (PacketBase) -> Unit)
        }
        send(WrappedPacket(id, packetBase))
    }

    override suspend fun <T : PacketBase> waitReply(packetBase: PacketBase, limitation: KClass<T>): T {
        val id = packetIdIncremental.incrementAndGet()
        val channel = Channel<PacketBase>(1)
        lock.write {
            replyHandle[id] = ReplyMetadata {
                runBlocking {
                    channel.send(it)
                }
            }
        }
        return channel.receive() as T
    }

    override fun <T : PacketBase> waitReplyAsync(packetBase: PacketBase, limitation: KClass<T>): Future<T> {
        val future = TriggerableFuture<T>()
        val id = packetIdIncremental.incrementAndGet()
        lock.write {
            replyHandle[id] = ReplyMetadata {
                future.trigger(it as T)
            }
        }
        send(WrappedPacket(id, packetBase))
        return future
    }

    override fun getInfo(): PacketBridgeClientConnection {
        return this
    }

    override fun getIdentify(): UUID {
        return identifiedUUID.getOrElse {
            throw IllegalStateException("Client is not identified")
        }
    }

    override fun <T : PacketBase> addListener(type: Class<T>, priority: Int, listener: (T) -> Unit): UnregisterTrigger {
        return eventManager.registerObserver(type.kotlin, 0, listener)
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
                    send(PacketWrapper(packetToReply, getIdentify(), packetWrapper.packetId))
                }
            }, packetBase.packet.javaClass.name)
            // Wave 2 - Trigger original packet
            eventManager.callEvent(packetBase.packet)
        } else {
            eventManager.callEvent(packetBase)
        }
    }

    internal fun identify(serverId: String) {
        println("PacketBridgeClientImpl - Identified as $serverId")
    }
}