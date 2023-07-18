package skywolf46.atmospherereentry.packetbridge.serializers

import arrow.core.Option
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerBase
import skywolf46.atmospherereentry.api.packetbridge.DataSerializerRegistry
import skywolf46.atmospherereentry.api.packetbridge.data.DoubleHashedType

class SerializerRegistryImpl : DataSerializerRegistry {
    private val serializerMap = mutableMapOf<DoubleHashedType, DataSerializerBase<Any>>()

    override fun registerSerializer(cls: Class<out Any>, serializerBase: DataSerializerBase<out Any>): Boolean {
        serializerMap[DoubleHashedType(cls)] = serializerBase as DataSerializerBase<Any>
        return true
    }

    override fun acquireSerializer(cls: Class<out Any>): Option<DataSerializerBase<Any>> {
        return Option.fromNullable(serializerMap[DoubleHashedType(cls)])
    }

    override fun acquireSerializer(hash: DoubleHashedType): Option<DataSerializerBase<Any>> {
        return Option.fromNullable(serializerMap[hash])
    }

}