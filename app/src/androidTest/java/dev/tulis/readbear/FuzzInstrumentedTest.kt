package dev.tulis.readbear

import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class FuzzInstrumentedTest {


    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun randomClicksDontCrash() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        val width = device.displayWidth
        val height = device.displayHeight

        repeat(5000) {
            var x = Random.nextInt(
                (width * 0.01).toInt(),
                (width * 0.15).toInt()
            )

            var y = Random.nextInt(
                (height * 0.01).toInt(),
                (height * 0.10).toInt()
            )

//            device.executeShellCommand("input tap ${x.toString()} ${y.toString()}")

            device.click(x, y)


            Thread.sleep(1)

//            x = Random.nextInt(
//                (width * 0.8).toInt(),
//                (width * 0.85).toInt()
//            )
//
//            y = Random.nextInt(
//                (height * 0.5).toInt(),
//                (height * 0.6).toInt()
//            )
//
//            device.click(x, y)


            Thread.sleep(1)


//            device.click(x, y)
//            Thread.sleep(50)
//
//            device.pressBack()
//            Thread.sleep(50)

            composeTestRule
                .onNodeWithText("You 100% should")
                .assertIsNotDisplayed()
        }
    }
}