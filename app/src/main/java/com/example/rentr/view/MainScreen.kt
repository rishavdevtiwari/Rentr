package com.example.rentr.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.rentr.repository.UserRepoImpl
import com.example.rentr.ui.theme.Orange
import com.example.rentr.viewmodel.UserViewModel

data class NavItem(
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    NavItem("Home", Icons.Default.Home),
    NavItem("Listings", Icons.Default.ListAlt),
    NavItem("Rentals", Icons.Default.History),
    NavItem("Chats", Icons.Default.ChatBubble),
    NavItem("Profile", Icons.Default.Person)
)

@Composable
fun MainScreen() {
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = Orange
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Orange,
                            unselectedIconColor = Color.Gray,
                            selectedTextColor = Orange,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.Black
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedIndex) {
                0 -> DashboardScreen()
                1 -> ListedScreen()
                2 -> RentalScreen()
                3 -> ConversationsScreen(onBackClicked = { selectedIndex = 0 })
                4 -> ProfileScreen(userViewModel)
            }
        }
    }
}
