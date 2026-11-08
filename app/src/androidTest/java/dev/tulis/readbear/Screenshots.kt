package dev.tulis.readbear

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.rules.ActivityScenarioRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.locale.LocaleTestRule

@RunWith(JUnit4::class)
class Screenshots {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val composeTestRule = createComposeRule()

    @Rule @JvmField
    val localeTestRule = LocaleTestRule()

    @Test
    fun testTakeScreenshot() {
        composeTestRule.waitUntil(10_000) {
            composeTestRule
                .onAllNodesWithTag("settings_button", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.waitForIdle()

        Screengrab.screenshot("1")

        composeTestRule.onNodeWithTag("settings_button", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        Screengrab.screenshot("2")

        composeTestRule.onNodeWithTag("settings_tab_3", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()
        Screengrab.screenshot("3")

        composeTestRule
            .onAllNodes(isRoot())[1]
            .performKeyInput {
                pressKey(Key.Back)
            }

        composeTestRule.onNodeWithTag("menu_burger", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("menu_snippets", useUnmergedTree = true).performClick()
        composeTestRule.waitForIdle()

        Screengrab.screenshot("4")
    }
}