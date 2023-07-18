package skywolf46.atmospherereentry.netsync.packets

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClient
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType
import skywolf46.atmospherereentry.netsync.api.NetworkLock
import skywolf46.atmospherereentry.netsync.packets.request.RequestNetworkLock

class NetworkLockImpl(val types: List<Pair<DoubleHashedType, String?>>) : NetworkLock, KoinComponent {
    private val client = get<PacketBridgeClient>()

    override suspend fun acquire() {
        TODO()
    }

    override suspend fun acquireOrFail(): Boolean {
        TODO()
    }

    override suspend fun release() {
        TODO()
    }

    override suspend fun getLockId(): Int {
        TODO("Not yet implemented")
    }
}