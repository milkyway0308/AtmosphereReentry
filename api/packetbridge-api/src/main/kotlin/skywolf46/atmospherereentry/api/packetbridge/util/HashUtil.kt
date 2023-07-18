package skywolf46.atmospherereentry.api.packetbridge.util

object HashUtil {
    fun getDoubleHash(string: String): Pair<Int, Int> {
        return hashCode() to string.reversed().hashCode()
    }
}