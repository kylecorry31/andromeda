package com.kylecorry.andromeda.preferences

import com.kylecorry.andromeda.core.topics.generic.Topic
import org.junit.Assert
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CachedPreferencesTest {

    @Test
    fun retrievesFromCache(){
        val key = "test_int"
        val mockPreferences = mock<IPreferences>()
        val topic = Topic<String>()
        whenever(mockPreferences.onChange).thenReturn(topic)
        whenever(mockPreferences.getInt(key)).thenReturn(1)

        val preferences = CachedPreferences(mockPreferences)
        Assert.assertEquals(preferences.getInt(key), 1)
        Assert.assertEquals(preferences.getInt(key), 1)
        verify(mockPreferences, times(1)).getInt(key)

        // Invalidate
        topic.publish(key)
        Assert.assertEquals(preferences.getInt(key), 1)
        Assert.assertEquals(preferences.getInt(key), 1)
        verify(mockPreferences, times(2)).getInt(key)

        // Invalidate a different key
        topic.publish(key + "1")
        Assert.assertEquals(preferences.getInt(key), 1)
        verify(mockPreferences, times(2)).getInt(key)
    }

}