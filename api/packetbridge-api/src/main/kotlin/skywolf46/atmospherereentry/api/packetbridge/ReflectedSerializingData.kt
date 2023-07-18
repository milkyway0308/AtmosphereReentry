package skywolf46.atmospherereentry.api.packetbridge

import skywolf46.atmospherereentry.common.annotations.KotlinOnly
import skywolf46.atmospherereentry.common.util.create

@KotlinOnly
interface ReflectedSerializingData : PacketBase {
    /**
     * 사용될 리플렉션 타입입니다.
     */
    fun getReflectType(): ReflectedSerializerBase.ReflectType {
        return ReflectedSerializerBase.ReflectType.CONSTRUCTOR_INJECTION
    }


    /**
     * 클래스 이중 해시 검증을 무시할지의 여부를 반환합니다.
     * 이중 해시 검증을 무시함으로써 약간의 속도 증진을 기대할 수 있으나,
     * 해시 충돌시 문제가 발생할 수 있습니다.
     */
    fun ignoreVerifier(): Boolean {
        return false
    }
}