package skywolf46.atmospherereentry.events.api

import java.lang.reflect.Field
import java.lang.reflect.Method

class EventReflectionFilter<T: Any>(val targetClass: Class<T>, val customRegistrar: EventManager.(Class<T>, Method, (T) -> Unit) -> Unit)