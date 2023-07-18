package skywolf46.atmospherereentry.api.packetbridge.packets

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer

@ReflectedSerializer
class GenericPacketResult(val success: Boolean) : PacketBase