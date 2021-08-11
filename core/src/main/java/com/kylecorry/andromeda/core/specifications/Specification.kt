package com.kylecorry.andromeda.core.specifications

abstract class Specification<T> {

    abstract fun isSatisfiedBy(value: T): Boolean

    fun and(other: Specification<T>): Specification<T> {
        return AndSpecification(this, other)
    }

    fun or(other: Specification<T>): Specification<T> {
        return OrSpecification(this, other)
    }

    fun not(): Specification<T> {
        return NotSpecification(this)
    }

}