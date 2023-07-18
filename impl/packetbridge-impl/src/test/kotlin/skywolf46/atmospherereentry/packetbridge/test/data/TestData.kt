package skywolf46.atmospherereentry.packetbridge.test.data

import skywolf46.atmospherereentry.api.packetbridge.annotations.ReflectedSerializer
import java.util.UUID

@ReflectedSerializer
data class TestData(val uuid: UUID, val string: String, val data: Int)