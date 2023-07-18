package skywolf46.atmospherereentry.netsync.data

import arrow.core.Option
import skywolf46.atmospherereentry.api.packetbridge.ReflectedSerializingData
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType

class LockInfo(val type: DoubleHashedType, val id: Option<String>) : ReflectedSerializingData