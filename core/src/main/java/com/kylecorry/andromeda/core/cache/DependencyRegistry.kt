package com.kylecorry.andromeda.core.cache

object DependencyRegistry {

    private val singletons = mutableMapOf<Class<*>, Lazy<Any>>()
    private val transients = mutableMapOf<Class<*>, () -> Any>()

    inline fun <reified T : Any> addSingleton(service: T) {
        addSingleton(T::class.java, service)
    }

    fun <T : Any> addSingleton(`class`: Class<T>, service: T) {
        singletons[`class`] = lazyOf(service)
    }

    fun <T : Any> addSingleton(`class`: Class<T>, service: () -> T) {
        singletons[`class`] = lazy { service() }
    }

    inline fun <reified T : Any> addTransient(noinline service: () -> T) {
        addTransient(T::class.java, service)
    }

    fun <T : Any> addTransient(`class`: Class<T>, service: () -> T) {
        transients[`class`] = service
    }

    inline fun <reified T : Any> remove() {
        remove(T::class.java)
    }

    fun <T : Any> remove(`class`: Class<T>) {
        singletons.remove(`class`)
        transients.remove(`class`)
    }

    inline fun <reified T : Any> get(): T {
        return get(T::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(`class`: Class<T>): T {
        return (singletons[`class`]?.value ?: transients[`class`]?.invoke()) as? T
            ?: throw IllegalStateException("No service registered for ${`class`.name}")
    }

}
