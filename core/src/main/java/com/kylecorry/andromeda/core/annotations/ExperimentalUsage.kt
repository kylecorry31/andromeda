package com.kylecorry.andromeda.core.annotations

@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.TYPE,
    AnnotationTarget.TYPEALIAS
)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class ExperimentalUsage(val message: String)
