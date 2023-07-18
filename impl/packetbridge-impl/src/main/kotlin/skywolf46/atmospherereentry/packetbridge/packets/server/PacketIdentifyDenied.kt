package skywolf46.atmospherereentry.packetbridge.packets.server

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer

@ReflectedSerializer
class PacketIdentifyDenied(val cause: String) : PacketBase