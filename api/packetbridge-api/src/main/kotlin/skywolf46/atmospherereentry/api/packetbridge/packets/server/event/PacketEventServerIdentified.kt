package skywolf46.atmospherereentry.api.packetbridge.packets.server.event

import skywolf46.atmospherereentry.api.packetbridge.PacketBase
import skywolf46.atmospherereentry.api.packetbridge.PacketBridgeClientConnection

class PacketEventServerIdentified(val name: String, val connection: PacketBridgeClientConnection) : PacketBase