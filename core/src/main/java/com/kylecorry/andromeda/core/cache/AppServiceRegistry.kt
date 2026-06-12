package com.kylecorry.andromeda.core.cache

object AppServiceRegistry {

    private val services = mutableMapOf<Class<*>, Lazy<Any>>()

    inline fun <reified T : Any> register(service: T) {
        register(T::class.java, service)
    }

    fun <T : Any> register(`class`: Class<T>, service: T) {
        services[`class`] = lazyOf(service)
    }

    fun <T : Any> register(`class`: Class<T>, service: () -> T) {
        services[`class`] = lazy { service() }
    }

    inline fun <reified T : Any> unregister() {
        unregister(T::class.java)
    }

    fun <T : Any> unregister(`class`: Class<T>) {
        services.remove(`class`)
    }

    inline fun <reified T : Any> get(): T {
        return get(T::class.java)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(`class`: Class<T>): T {
        return services[`class`]?.value as? T
            ?: throw IllegalStateException("No service registered for ${`class`.name}")
    }

}
