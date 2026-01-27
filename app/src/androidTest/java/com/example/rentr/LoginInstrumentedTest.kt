package com.example.rentr

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.example.rentr.view.DashboardActivity
import com.example.rentr.view.LoginActivity
import com.example.rentr.view.RegistrationActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginInstrumentedTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<LoginActivity>()

    @Before
    fun setup() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun testNavigationToRegistration() {
        // Click on the "Sign up" text
        composeRule.onNodeWithTag("register")
            .performClick()

        // Assert that RegistrationActivity was started
        Intents.intended(hasComponent(RegistrationActivity::class.java.name))
    }

    @Test
    fun testSuccessfulLogin_navigatesToDashboard() {
        // Enter email
        composeRule.onNodeWithTag("email")
            .performTextInput("ram@gmail.com")

        // Enter password
        composeRule.onNodeWithTag("password")
            .performTextInput("password")

        // Click Login button
        composeRule.onNodeWithTag("login")
            .performClick()


        Intents.intended(hasComponent(DashboardActivity::class.java.name))
    }
}
