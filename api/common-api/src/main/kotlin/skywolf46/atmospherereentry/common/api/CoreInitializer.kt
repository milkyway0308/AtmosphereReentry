package skywolf46.atmospherereentry.common.api

object CoreInitializer {
    fun init() {
        Class.forName("skywolf46.atmospherereentry.common.Core").getMethod("initialize").invoke(null)
    }
}