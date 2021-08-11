package com.kylecorry.andromeda.core.specifications

class AndSpecification<T>(
    private val spec1: Specification<T>,
    private val spec2: Specification<T>
) : Specification<T>() {
    override fun isSatisfiedBy(value: T): Boolean {
        return spec1.isSatisfiedBy(value) && spec2.isSatisfiedBy(value)
    }
}

class OrSpecification<T>(private val spec1: Specification<T>, private val spec2: Specification<T>) :
    Specification<T>() {
    override fun isSatisfiedBy(value: T): Boolean {
        return spec1.isSatisfiedBy(value) || spec2.isSatisfiedBy(value)
    }
}

class NotSpecification<T>(private val spec: Specification<T>) : Specification<T>() {
    override fun isSatisfiedBy(value: T): Boolean {
        return !spec.isSatisfiedBy(value)
    }
}

class BooleanSpecification<T>(private val value: Boolean): Specification<T>() {
    override fun isSatisfiedBy(value: T): Boolean {
        return this.value
    }
}

class ConditionalSpecification<T>(private val condition: Specification<T>, private val ifTrue: Specification<T>, private val ifFalse: Specification<T>): Specification<T>() {
    override fun isSatisfiedBy(value: T): Boolean {
        return if (condition.isSatisfiedBy(value)){
            ifTrue.isSatisfiedBy(value)
        } else {
            ifFalse.isSatisfiedBy(value)
        }
    }

}