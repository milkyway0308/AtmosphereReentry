package skywolf46.atmospherereentry.api.packetbridge.annotations

/**
 * 해당 어노테이션이 사용된 클래스를 코어 클래스로 지정합니다.
 *
 * 코어 클래스는 8바이트의 클래스 헤더를 무시하며, 2바이트의 고정된 클래스 헤더를 사용합니다.
 *
 * 고정 클래스 헤더가 중첩되는 경우, 문제가 발생할 수 있습니다.
 *
 * 일반적으로, AtmosphereReentry는 Short.MIN_VALUE ~ Short.MIN_VALUE + 50을 시스템 구별자로 사용합니다.
 */
annotation class CoreType(val id: Short)