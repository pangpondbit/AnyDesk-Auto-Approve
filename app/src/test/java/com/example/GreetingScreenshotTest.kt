package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.ClickStep
import com.example.ui.components.CustomStepsConfigCard
import com.example.ui.theme.AnyDeskAutoApproveTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun custom_steps_screenshot() {
    composeTestRule.setContent {
      AnyDeskAutoApproveTheme {
        CustomStepsConfigCard(
          clickSteps = listOf(
            ClickStep(keywords = listOf("แชร์ทั้งหน้าจอ", "Entire screen"))
          ),
          clickDelayMs = 300L,
          onAddStep = {},
          onRemoveStep = {},
          onMoveStepUp = {},
          onMoveStepDown = {},
          onAddKeywordToStep = { _, _ -> },
          onRemoveKeywordFromStep = { _, _ -> },
          onSetClickDelayMs = {},
          onResetDefaults = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/custom_steps.png")
  }
}

