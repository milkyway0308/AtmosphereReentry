package skywolf46.atmospherereentry.netsync.packets.request

import skywolf46.atmospherereentry.api.packetbridge.ReflectedSerializingData
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType

class RequestNetworkLock(val lockTarget: DoubleHashedType) : ReflectedSerializingData {

}