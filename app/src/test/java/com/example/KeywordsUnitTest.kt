package com.example

import com.example.data.ClickStep
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class KeywordsUnitTest {

    @Test
    fun clickStep_jsonSerializationAndEmptyDefaults() {
        val initialDefault = ClickStep.listFromJsonString(null)
        assertEquals(2, initialDefault.size)
        assertTrue(initialDefault[0].keywords.contains("ทั้งหน้าจอ"))
        assertTrue(initialDefault[1].keywords.contains("เริ่มเลย"))

        val steps = listOf(
            ClickStep(
                keywords = listOf("แชร์ทั้งหน้าจอ", "Entire screen")
            ),
            ClickStep(
                keywords = listOf("แชร์หน้าจอ", "Start now")
            )
        )

        val json = ClickStep.listToJsonString(steps)
        val deserialized = ClickStep.listFromJsonString(json)

        assertEquals(2, deserialized.size)
        assertEquals(listOf("แชร์ทั้งหน้าจอ", "Entire screen"), deserialized[0].keywords)
        assertEquals(listOf("แชร์หน้าจอ", "Start now"), deserialized[1].keywords)
    }

    @Test
    fun mainActivity_foregroundStateTracksCorrectly() {
        assertEquals(false, MainActivity.isAppInForeground)
    }
}

