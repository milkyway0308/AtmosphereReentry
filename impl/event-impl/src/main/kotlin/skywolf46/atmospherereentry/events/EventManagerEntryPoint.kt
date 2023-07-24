package skywolf46.atmospherereentry.events

import org.koin.core.component.KoinComponent
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import skywolf46.atmospherereentry.common.api.annotations.EntryPointWorker
import skywolf46.atmospherereentry.common.api.annotations.EntryPointContainer
import skywolf46.atmospherereentry.common.api.annotations.Reflective
import skywolf46.atmospherereentry.events.api.EventManager
import skywolf46.atmospherereentry.events.api.annotations.EventContainer

@EntryPointContainer
class EventManagerEntryPoint : KoinComponent {
    private val eventManager = EventManagerImpl()

    @OptIn(Reflective::class)
    @EntryPointWorker
    private fun initEventManager() {
        loadKoinModules(module {
            factory<EventManager> { EventManagerImpl() }
        })
        eventManager.scanAndRegister(EventContainer::class.java, EventContainer::class.java) { annotation ->
            annotation.priority
        }
    }
}