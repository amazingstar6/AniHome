package com.example.anilist

import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.properties.ReadOnlyProperty

// fixme tests only work when executed separately
@HiltAndroidTest
class ComposeTests {
    /**
     * Manages the components' state and is used to perform injection on your test
     */
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    /**
     * Create a temporary folder used to create a Data Store file. This guarantees that
     * the file is removed in between each test, preventing a crash.
     * FIXME: not working idk why
     */
    @BindValue
    @get:Rule(order = 1)
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    /**
     * Use the primary activity to initialize the app normally.
     */
    @get:Rule(order = 2)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @After
    fun tearDown() {
//        composeTestRule.activity.datas
        File(
            ApplicationProvider.getApplicationContext<Context>().filesDir,
            "USER_PREFERENCES",
        ).deleteRecursively()
    }

    private fun AndroidComposeTestRule<*, *>.stringResource(
        @StringRes resId: Int,
    ) = ReadOnlyProperty<Any?, String> { _, _ -> activity.getString(resId) }

    private val settings by composeTestRule.stringResource(R.string.settings)
    private val back by composeTestRule.stringResource(R.string.back)
    private val myAnime by composeTestRule.stringResource(R.string.my_anime)
    private val myManga by composeTestRule.stringResource(R.string.my_manga)
    private val pleaseLogin by composeTestRule.stringResource(R.string.please_login_to_use_this_feature)

    @Test
    fun navigateToSettingsTest() {
        composeTestRule.apply {
            onNodeWithContentDescription(settings).performClick()
            onNodeWithText("Display").assertIsDisplayed()
            onNodeWithContentDescription(back).performClick()
            onNodeWithText("Home").assertIsDisplayed()
        }
    }

    @Test
    fun navigateToMyAnimeTest() {
        composeTestRule.apply {
            onNodeWithText(myAnime).performClick()
            onNodeWithText(pleaseLogin).assertIsDisplayed()
            onNodeWithText(myManga).performClick()
            onNodeWithText(pleaseLogin).assertIsDisplayed()
        }
    }
}

// A custom runner to set up the instrumented application class for tests.
class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?,
    ): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
