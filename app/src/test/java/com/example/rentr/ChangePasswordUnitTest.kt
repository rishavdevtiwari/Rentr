package com.example.rentr

import com.example.rentr.repository.UserRepo
import com.example.rentr.viewmodel.UserViewModel
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ChangePasswordUnitTest {

    @Test
    fun change_password_success_test() {
        // 1. Mock Repo & ViewModel
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        val oldPass = "oldPass123"
        val newPass = "newPass123"
        val successMessage = "Password updated successfully"

        // 2. Define Behavior
        // We use index '2' because the callback is the 3rd argument: (old, new, callback)
        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(true, successMessage)
            null
        }.`when`(repo).changePassword(any(), any(), any())

        // 3. Act
        var successResult = false
        var messageResult = ""

        viewModel.changePassword(oldPass, newPass) { success, msg ->
            successResult = success
            messageResult = msg
        }

        // 4. Assert
        assertTrue(successResult)
        assertEquals(successMessage, messageResult)

        // 5. Verify
        verify(repo).changePassword(eq(oldPass), eq(newPass), any())
    }

    @Test
    fun change_password_failure_test() {
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)
        val errorMessage = "Update failed"

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(2)
            callback(false, errorMessage)
            null
        }.`when`(repo).changePassword(any(), any(), any())

        var successResult = true
        var messageResult = ""

        viewModel.changePassword("wrong", "new") { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertFalse(successResult)
        assertEquals(errorMessage, messageResult)
    }
}