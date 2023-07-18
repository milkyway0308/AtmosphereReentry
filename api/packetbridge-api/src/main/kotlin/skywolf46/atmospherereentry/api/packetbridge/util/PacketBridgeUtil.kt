package skywolf46.atmospherereentry.api.packetbridge.util

import arrow.core.Either
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClient
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClientConnection
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeHost
import java.net.InetAddress

interface PacketBridgeUtil {

    fun locator(): Locator

    suspend fun startHost(port: Int): Either<Exception, PacketBridgeHost>

    suspend fun connectClient(address: InetAddress): suspend () -> Either<Exception, PacketBridgeClient>

    interface Locator {
        fun ofName(serverName: String): PacketBridgeClientConnection

        fun ofPort(port: Int): PacketBridgeClientConnection

        fun ofAddress(remoteAddr: String): PacketBridgeClientConnection
    }
}