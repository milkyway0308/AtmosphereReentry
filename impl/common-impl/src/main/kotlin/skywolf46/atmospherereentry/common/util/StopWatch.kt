package skywolf46.atmospherereentry.common.util

class StopWatch {
    private val loggedTime = mutableMapOf<String, Long>()
    private val stackedTime = mutableMapOf<String, Long>()

    fun start(key: String) {
        loggedTime[key] = System.currentTimeMillis()
        stackedTime.remove(key)
    }

    fun log(key: String) {
        loggedTime[key]?.let {
            loggedTime[key] = System.currentTimeMillis()
            stackedTime.put(key, stackedTime.getOrElse(key) { 0L } + (System.currentTimeMillis() - it))
        }
    }

    fun stop(key: String) {
        stackedTime.remove(key)
        loggedTime.remove(key)
    }

    fun get(key: String): Long {
        return stackedTime[key] ?: 0L
    }

    fun logAndGet(key: String) : Long {
        log(key)
        return get(key)
    }
}