package com.example.rentr.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Field = Color(0xFF333232)
val Orange = Color(0xFFFF5D18)
val Outline = Color(0xFF818181)

class AdminReviewManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReviewScreenContent()
        }
    }
}
enum class FlagStatus(val label: String, val color: Color) {
    PENDING("PENDING", Color.Yellow),
    RESOLVED("RESOLVED", Color.Green),
    MARKED("MARKED", Color.Cyan),
    SUSPENDED("SUSPENDED", Color.Red)
}

data class FlaggedUser(
    val id: Int,
    val username: String,
    val reason: String,
    val status: FlagStatus
)


@Composable
fun FlaggedUserCard(user: FlaggedUser) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Field),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Outline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = user.username,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                // Status Badge
                Surface(
                    color = user.status.color.copy(alpha = 0.1f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, user.status.color)
                ) {
                    Text(
                        text = user.status.label,
                        color = user.status.color,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reason: ${user.reason}",
                color = Color.LightGray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button using your Orange
            Button(
                onClick = { /* Handle Review */ },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Orange),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Text("Review", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreenContent() {
    val flaggedList = listOf(
        FlaggedUser(1, "User_Alpha", "Violation of terms of service.", FlagStatus.PENDING),
        FlaggedUser(2, "Beta_Tester", "Suspicious login from unknown IP.", FlagStatus.SUSPENDED),
        FlaggedUser(3, "Gamma_Ray", "Identity verification pending.", FlagStatus.MARKED),
        FlaggedUser(4, "Delta_Shield", "Report reviewed and closed.", FlagStatus.RESOLVED)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("FLAGGED REVIEWS", color = Orange, fontWeight = FontWeight.Black)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            item {
                Text(
                    text = "Recent Flags",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(flaggedList) { user ->
                FlaggedUserCard(user = user)
            }
        }
    }
}

@Preview(showBackground = true, name = "Flagged Reviews Screen")
@Composable
fun ReviewScreenPreview() {
    MaterialTheme {
        ReviewScreenContent()
    }
}
