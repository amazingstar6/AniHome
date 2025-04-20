package com.kevin.anihome.utils

import androidx.compose.ui.graphics.Color
import com.kevin.anihome.utils.Utils.Companion.toHexString
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals

class UtilsTest {
    @Test
    fun convertEpochToStringTest() {
        assertEquals("2021-12-04", Utils.convertEpochToDateString(1638626505))
    }

    @Test
    fun colorToHexStringTest() {
        val color = Color.Cyan
        assertEquals("#00FFFF", color.toHexString())
    }
}
