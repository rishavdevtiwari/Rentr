package com.example.rentr

import com.example.rentr.repository.UserRepo
import com.example.rentr.viewmodel.UserViewModel
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import kotlin.test.assertEquals

class LogoutUnitTest {
    @Test
    fun logout_success_test(){
        val repo = mock<UserRepo>()
        val viewModel = UserViewModel(repo)

        doAnswer { invocation ->
            val callback = invocation.getArgument<(Boolean, String) -> Unit>(0)
            callback(true, "Logout success")
            null
        }.`when`(repo).logout(any())

        var successResult = false
        var messageResult = ""

        viewModel.logout { success, msg ->
            successResult = success
            messageResult = msg
        }

        assertTrue(successResult)
        assertEquals("Logout success", messageResult)

        verify(repo).logout(any())
    }
}