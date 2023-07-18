package skywolf46.atmospherereentry.packetbridge.packets.client

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer

@ReflectedSerializer
class PacketRequestIdentify(val authenticateKey: String) : PacketBase