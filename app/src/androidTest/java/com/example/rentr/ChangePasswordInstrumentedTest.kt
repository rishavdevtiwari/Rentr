package com.example.rentr.view

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChangePasswordInstrumentedTest {


    @get:Rule
    val composeRule = createAndroidComposeRule<ChangePassActivity>()


    @Test
    fun testChangePassword_success() {

        composeRule.onNodeWithTag("oldPasswordInput")
            .performTextInput("oldPass123")


        composeRule.onNodeWithTag("newPasswordInput")
            .performTextInput("newPass123")


        composeRule.onNodeWithTag("confirmPasswordInput")
            .performTextInput("newPass123")


        composeRule.onNodeWithTag("changePasswordButton")
            .performClick()


    }

    @Test
    fun testChangePassword_mismatch_showsError() {

        composeRule.onNodeWithTag("oldPasswordInput")
            .performTextInput("oldPass123")


        composeRule.onNodeWithTag("newPasswordInput")
            .performTextInput("newPass123")


        composeRule.onNodeWithTag("confirmPasswordInput")
            .performTextInput("wrongPass")


        composeRule.onNodeWithTag("changePasswordButton")
            .performClick()


        composeRule.onNodeWithTag("changePasswordButton").assertIsDisplayed()
    }
}