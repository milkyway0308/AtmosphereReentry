package skywolf46.atmospherereentry.api.packetbridge


abstract class ReflectedSerializerBase<T : Any>(val target: Class<T>, val type: ReflectType) :
    DataSerializerBase<T>() {

    enum class ReflectType {
        /**
         * 생성자 주입 (Constructor Injection)을 통해서 리플렉션을 사용합니다.
         * 이 경우, 생성자와 일치하는 필드만이 리플렉션을 통한 직렬화 대상에 포함됩니다.
         *
         * **이 옵션은 자바에서는 원활히 작동되지 않습니다. Kotlin val을 사용하십시오.**
         */
        CONSTRUCTOR_INJECTION,

        /**
         * 필드 인젝션을 통해 리플렉션을 사용합니다.
         * 이 경우, var로 선언된 값만이 직렬화 대상에 포함됩니다.
         *
         * **이 옵션은 빈 생성자로만 동작합니다.**
         */
        FIELD_INJECTION,

        /**
         * 생성자 주입과 필드 인젝션을 모두 수행합니다.
         *
         * 이 경우, 생성자 주입이 우선적으로 수행됩니다.
         */
        MIXED_INJECTION
    }
}