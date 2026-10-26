package com.example.hawkins

import android.content.res.Configuration
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReceiverLocalizationTest {
    private fun resources(tag: String) = InstrumentationRegistry.getInstrumentation().targetContext.let { context ->
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(tag))
        }
        context.createConfigurationContext(configuration).resources
    }

    @Test
    fun englishAndChineseResolveIndependently() {
        val english = resources("en")
        val chinese = resources("zh-CN")
        assertEquals("Listen", english.getString(R.string.action_listen))
        assertEquals("收听", chinese.getString(R.string.action_listen))
        assertEquals("CONTACT ESTABLISHED", english.getString(R.string.phase_solved_0))
        assertEquals("连接已建立", chinese.getString(R.string.phase_solved_0))
        assertEquals("Reset room and receiver", english.getString(R.string.a11y_reset_view))
        assertEquals("重置房间和接收机", chinese.getString(R.string.a11y_reset_view))
        assertEquals("房间未能加载，请点击重置后重试。", chinese.getString(R.string.error_status))
    }

    @Test
    fun formattedReadoutsPreserveNumericValues() {
        val english = resources("en")
        val chinese = resources("zh-CN")
        assertEquals("83.0 MHz", english.getString(R.string.frequency_format, 83f))
        assertEquals("83.0 兆赫", chinese.getString(R.string.frequency_format, 83f))
        assertEquals("100% LINK", english.getString(R.string.strength_format, 100))
        assertEquals("连接强度 100%", chinese.getString(R.string.strength_format, 100))
        assertEquals("R", chinese.getString(R.string.letter_r))
    }

    @Test
    fun unsupportedLocaleFallsBackToEnglish() {
        assertEquals("Transmit", resources("fr-FR").getString(R.string.submit_reply))
    }
}
