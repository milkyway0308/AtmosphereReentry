package skywolf46.atmospherereentry.packetbridge.packets.server

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer
import java.util.UUID

@ReflectedSerializer
class PacketRequireIdentify(val uuid: UUID) : PacketBase