package skywolf46.atmospherereentry.netsync.packets.request

import arrow.core.Option
import skywolf46.atmospherereentry.api.packetbridge.ReflectedSerializingData
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType

class RequestVariableSync(val doubleBuffer: DoubleHashedType, val fieldName: Option<String>) : ReflectedSerializingData