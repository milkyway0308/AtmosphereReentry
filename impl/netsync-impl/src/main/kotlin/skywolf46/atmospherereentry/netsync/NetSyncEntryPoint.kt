package skywolf46.atmospherereentry.netsync

import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.atmospherereentry.common.annotations.EntryPointContainer
import skywolf46.atmospherereentry.common.annotations.EntryPoinWorker

@EntryPointContainer
object NetSyncEntryPoint : KoinComponent {
    @EntryPoinWorker
    private fun init() {
        loadKoinModules(module {
            single { NetSyncEndPointImpl() }
            single { NetSyncPacketListenerRegistry() }
        })
    }
}