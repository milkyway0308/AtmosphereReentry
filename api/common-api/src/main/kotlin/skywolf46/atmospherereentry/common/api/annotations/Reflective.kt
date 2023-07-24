package skywolf46.atmospherereentry.common.api.annotations

@RequiresOptIn(
    message = "This function use reflection. This may cause performance issue, so use this class carefully.",
    level = RequiresOptIn.Level.WARNING
)
@Target(AnnotationTarget.FUNCTION)
annotation class Reflective