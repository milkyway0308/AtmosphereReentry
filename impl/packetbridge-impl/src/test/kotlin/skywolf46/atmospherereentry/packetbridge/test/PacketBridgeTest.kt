package skywolf46.atmospherereentry.packetbridge.test

import io.netty.buffer.ByteBufAllocator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeAs
import skywolf46.atmospherereentry.api.packetbridge.util.deserializeRoot
import skywolf46.atmospherereentry.api.packetbridge.util.serializeRootTo
import skywolf46.atmospherereentry.api.packetbridge.util.serializeTo
import skywolf46.atmospherereentry.common.Core
import skywolf46.atmospherereentry.packetbridge.PacketBridgeClientImpl
import skywolf46.atmospherereentry.packetbridge.PacketBridgeServerImpl
import skywolf46.atmospherereentry.packetbridge.test.data.TestData
import skywolf46.atmospherereentry.api.packetbridge.util.JwtProvider
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class PacketBridgeTest {
    init {
        Core.initialize()
    }

    @Test
    fun test1() {
        val data = TestData(UUID.randomUUID(), "Hello world", 1234)
        println("Serialized $data")
        val buf = ByteBufAllocator.DEFAULT.buffer()
        data.serializeRootTo(buf, ByteBufAllocator.DEFAULT.buffer())
        assertEquals(data, buf.deserializeRoot<Any>().apply {
            println(println("Deserialized $this"))
        })
    }


    @Test
    fun test2() {
        val data = TestData(UUID.randomUUID(), "Hello world", 1234)
        println("Serialized $data")
        val buf = ByteBufAllocator.DEFAULT.buffer()
        data.serializeTo(buf)
        assertEquals(data, buf.deserializeAs<TestData>().apply {
            println(println("Deserialized $this"))
        })
    }

    @Test
    fun test3() {
        val port = 38922
        val newJwtProvider = JwtProvider().apply {
            this.initializeKey()
        }
        val server = PacketBridgeServerImpl(port, newJwtProvider)
        val client = PacketBridgeClientImpl("localhost", port, newJwtProvider.createIdentifier("Hello World"))
        Thread.sleep(TimeUnit.SECONDS.toMillis(10))
    }


    @Test
    fun test4() {
        val data = mutableListOf<TestData>()
        val amount = 500000
        val repeatTime = 5
        val wave = 3
        repeat(amount) {
            data.add(TestData(UUID.randomUUID(), "Test", Random.nextInt(Integer.MAX_VALUE)))
        }
        val buf = ByteBufAllocator.DEFAULT.buffer()
        repeat(wave) { wave ->
            println("Wave $wave")
            buf.clear()
            val start = System.currentTimeMillis()
            data.forEach {
                it.serializeTo(buf)
            }
            println("Serialized ${data.size} data in ${System.currentTimeMillis() - start}ms")
        }
    }
}

fun main() {
    Core.initialize()
}