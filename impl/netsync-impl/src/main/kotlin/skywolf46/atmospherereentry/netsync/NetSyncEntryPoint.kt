package skywolf46.atmospherereentry.netsync

import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.atmospherereentry.common.api.annotations.EntryPointContainer
import skywolf46.atmospherereentry.common.api.annotations.EntryPointWorker

@EntryPointContainer
object NetSyncEntryPoint : KoinComponent {
    @EntryPointWorker
    private fun init() {
        loadKoinModules(module {
            single { NetSyncEndPointImpl() }
            single { NetSyncPacketListenerRegistry() }
        })
    }
}