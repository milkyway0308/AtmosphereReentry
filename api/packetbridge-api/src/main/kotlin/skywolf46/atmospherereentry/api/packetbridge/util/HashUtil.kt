package skywolf46.atmospherereentry.api.packetbridge.util

object HashUtil {
    fun getDoubleHash(string: String): Pair<Int, Int> {
        return string.hashCode() to string.reversed().hashCode()
    }
}