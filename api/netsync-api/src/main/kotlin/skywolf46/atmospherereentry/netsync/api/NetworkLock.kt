package skywolf46.atmospherereentry.netsync.api

interface NetworkLock {
    suspend fun acquire()

    suspend fun acquireOrFail(): Boolean

    suspend fun release()

    suspend fun getLockId(): Int
}