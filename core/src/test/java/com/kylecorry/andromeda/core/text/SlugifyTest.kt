package com.kylecorry.andromeda.core.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class SlugifyTest {

    @ParameterizedTest
    @CsvSource(
        "Hello, hello",
        "Hello World, hello-world",
        "Hello World!, hello-world",
        "test123, test123",
        "test 123, test-123",
        "test 123 test, test-123-test",
        "test'something, testsomething",
        "Hello World! é, hello-world-e"
    )
    fun testSlugify(input: String, expected: String) {
        val actual = input.slugify()
        assertEquals(expected, actual)
    }

}