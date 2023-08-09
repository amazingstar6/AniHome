package com.example.anilist.utils

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.example.anilist.utils.Utils.Companion.toHexString
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

class UtilsTest {
    @Test
    fun convertEpochToStringTest() {
        assertEquals("2021-12-04", Utils.convertEpochToString(1638626505))
    }

    @Test
    fun getRelativeTimeTest() {
        TODO()
    }

    @Test
    fun colorToHexStringTest() {
        val color = Color.Cyan
        assertEquals("#00FFFF", color.toHexString())
    }
}