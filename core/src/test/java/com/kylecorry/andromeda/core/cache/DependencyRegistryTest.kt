package com.kylecorry.andromeda.core.cache

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DependencyRegistryTest {

    @AfterEach
    fun cleanup() {
        DependencyRegistry.remove(TestService::class.java)
        DependencyRegistry.remove(OtherService::class.java)
    }

    @Test
    fun addSingletonAndGetService() {
        val service = TestService("test")

        DependencyRegistry.addSingleton(TestService::class.java, service)

        assertSame(service, DependencyRegistry.get(TestService::class.java))
    }

    @Test
    fun addSingletonAndGetServiceUsingReifiedType() {
        val service = TestService("test")

        DependencyRegistry.addSingleton(service)

        assertSame(service, DependencyRegistry.get<TestService>())
    }

    @Test
    fun addSingletonLazyServiceOnlyCreatesItOnce() {
        var calls = 0
        DependencyRegistry.addSingleton(TestService::class.java) {
            calls++
            TestService("test")
        }

        val first = DependencyRegistry.get(TestService::class.java)
        val second = DependencyRegistry.get(TestService::class.java)

        assertSame(first, second)
        assertEquals("test", first.value)
        assertEquals(1, calls)
    }

    @Test
    fun lazyServiceCanGetAnotherServiceDuringInstantiation() {
        DependencyRegistry.addSingleton(TestService::class.java) {
            TestService(DependencyRegistry.get<OtherService>().value)
        }
        DependencyRegistry.addSingleton(OtherService("other"))

        val service = DependencyRegistry.get<TestService>()

        assertEquals("other", service.value)
    }

    @Test
    fun addTransientAndGetService() {
        var calls = 0
        DependencyRegistry.addTransient(TestService::class.java) {
            calls++
            TestService("test $calls")
        }

        val first = DependencyRegistry.get(TestService::class.java)
        val second = DependencyRegistry.get(TestService::class.java)

        assertEquals("test 1", first.value)
        assertEquals("test 2", second.value)
        assertEquals(2, calls)
    }

    @Test
    fun addTransientAndGetServiceUsingReifiedType() {
        DependencyRegistry.addTransient {
            TestService("test")
        }

        assertEquals("test", DependencyRegistry.get<TestService>().value)
    }

    @Test
    fun getUsesSingletonBeforeTransient() {
        val singleton = TestService("singleton")
        DependencyRegistry.addTransient(TestService::class.java) {
            TestService("transient")
        }
        DependencyRegistry.addSingleton(TestService::class.java, singleton)

        val service = DependencyRegistry.get<TestService>()

        assertSame(singleton, service)
    }

    @Test
    fun addSingletonReplacesExistingService() {
        val first = TestService("first")
        val second = TestService("second")

        DependencyRegistry.addSingleton(TestService::class.java, first)
        DependencyRegistry.addSingleton(TestService::class.java, second)

        assertSame(second, DependencyRegistry.get(TestService::class.java))
    }

    @Test
    fun removeRemovesService() {
        DependencyRegistry.addSingleton(TestService::class.java, TestService("test"))

        DependencyRegistry.remove(TestService::class.java)

        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.get(TestService::class.java)
        }
    }

    @Test
    fun removeRemovesServiceUsingReifiedType() {
        DependencyRegistry.addSingleton(OtherService("test"))

        DependencyRegistry.remove<OtherService>()

        assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.get<OtherService>()
        }
    }

    @Test
    fun getThrowsWhenServiceIsNotRegistered() {
        val exception = assertThrows(IllegalStateException::class.java) {
            DependencyRegistry.get(TestService::class.java)
        }

        assertEquals(
            "No service registered for ${TestService::class.java.name}",
            exception.message
        )
    }

    private data class TestService(val value: String)

    private data class OtherService(val value: String)
}
