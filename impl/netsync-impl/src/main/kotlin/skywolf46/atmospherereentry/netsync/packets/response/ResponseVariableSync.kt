package skywolf46.atmospherereentry.netsync.packets.response

import arrow.core.Option
import skywolf46.atmospherereentry.api.packetbridge.ReflectedSerializingData

class ResponseVariableSync<T: Any>(val data: Option<T>) : ReflectedSerializingData