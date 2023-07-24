package skywolf46.atmospherereentry.common.api.annotations

/**
 * 해당 클래스 혹은 펑션이 자바에서는 동작하지 않음을 뜻합니다.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
annotation class KotlinOnly
