package skywolf46.atmospherereentry.api.packetbridge.packets.server

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer

@ReflectedSerializer
class PacketActionRejected(val cause: String) : PacketBase