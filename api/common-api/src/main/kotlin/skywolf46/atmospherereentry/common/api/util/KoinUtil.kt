package skywolf46.atmospherereentry.common.api.util

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.ParametersHolder

inline fun <reified T : Any> KoinComponent.create(vararg parameters: Any): T {
    return get { ParametersHolder(parameters.toMutableList()) }
}