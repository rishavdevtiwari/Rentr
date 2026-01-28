package com.example.rentr

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rentr.view.DashboardActivity
import com.example.rentr.view.LoginActivity
import com.example.rentr.view.ProfileActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogoutInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ProfileActivity>()

    @Test
    fun logout_confirmDialog_logoutSuccess() {

        // click logout
        composeRule.onNodeWithTag("logoutButton")
            .performClick()

        // dialog visible
        composeRule.onNodeWithText("Are you sure you want to logout?")
            .assertExists()

        // confirm
        composeRule.onNodeWithText("Yes")
            .performClick()
    }
}


