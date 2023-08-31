package skywolf46.atmospherereentry.api.packetbridge

import arrow.core.Option
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType

interface DataSerializerRegistry {
    fun registerSerializer(cls: Class<out Any>, serializerBase: DataSerializerBase<out Any>): Boolean

    fun registerSerializer(cls: Class<out Any>, serializerBase: Lazy<DataSerializerBase<out Any>>): Boolean

    fun acquireSerializer(cls: Class<out Any>): Option<DataSerializerBase<Any>>

    fun acquireSerializer(hash: DoubleHashedType): Option<DataSerializerBase<Any>>

    fun wakeLazySerializers()
}