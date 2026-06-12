package com.kylecorry.andromeda.core.cache

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AppServiceRegistryTest {

    @AfterEach
    fun cleanup() {
        AppServiceRegistry.unregister(TestService::class.java)
        AppServiceRegistry.unregister(OtherService::class.java)
    }

    @Test
    fun registerAndGetService() {
        val service = TestService("test")

        AppServiceRegistry.register(TestService::class.java, service)

        assertSame(service, AppServiceRegistry.get(TestService::class.java))
    }

    @Test
    fun registerAndGetServiceUsingReifiedType() {
        val service = TestService("test")

        AppServiceRegistry.register(service)

        assertSame(service, AppServiceRegistry.get<TestService>())
    }

    @Test
    fun registerLazyServiceOnlyCreatesItOnce() {
        var calls = 0
        AppServiceRegistry.register(TestService::class.java) {
            calls++
            TestService("test")
        }

        val first = AppServiceRegistry.get(TestService::class.java)
        val second = AppServiceRegistry.get(TestService::class.java)

        assertSame(first, second)
        assertEquals("test", first.value)
        assertEquals(1, calls)
    }

    @Test
    fun lazyServiceCanGetAnotherServiceDuringInstantiation() {
        AppServiceRegistry.register(TestService::class.java) {
            TestService(AppServiceRegistry.get<OtherService>().value)
        }
        AppServiceRegistry.register(OtherService("other"))

        val service = AppServiceRegistry.get<TestService>()

        assertEquals("other", service.value)
    }

    @Test
    fun registerReplacesExistingService() {
        val first = TestService("first")
        val second = TestService("second")

        AppServiceRegistry.register(TestService::class.java, first)
        AppServiceRegistry.register(TestService::class.java, second)

        assertSame(second, AppServiceRegistry.get(TestService::class.java))
    }

    @Test
    fun unregisterRemovesService() {
        AppServiceRegistry.register(TestService::class.java, TestService("test"))

        AppServiceRegistry.unregister(TestService::class.java)

        assertThrows(IllegalStateException::class.java) {
            AppServiceRegistry.get(TestService::class.java)
        }
    }

    @Test
    fun unregisterRemovesServiceUsingReifiedType() {
        AppServiceRegistry.register(OtherService("test"))

        AppServiceRegistry.unregister<OtherService>()

        assertThrows(IllegalStateException::class.java) {
            AppServiceRegistry.get<OtherService>()
        }
    }

    @Test
    fun getThrowsWhenServiceIsNotRegistered() {
        val exception = assertThrows(IllegalStateException::class.java) {
            AppServiceRegistry.get(TestService::class.java)
        }

        assertEquals(
            "No service registered for ${TestService::class.java.name}",
            exception.message
        )
    }

    private data class TestService(val value: String)

    private data class OtherService(val value: String)
}
