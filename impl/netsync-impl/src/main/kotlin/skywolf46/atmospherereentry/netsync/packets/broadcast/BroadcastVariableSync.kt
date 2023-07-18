package skywolf46.atmospherereentry.netsync.packets.broadcast

import arrow.core.Option
import skywolf46.atmospherereentry.api.packetbridge.ReflectedSerializingData
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType

class BroadcastVariableSync (val doubleBuffer: DoubleHashedType, val fieldName: Option<String>, val data: Option<Any>) :
    ReflectedSerializingData