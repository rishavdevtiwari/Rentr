package com.example.rentr.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Orange // Assuming Orange is defined in your theme
import com.example.rentr.R // Ensure R is imported if using painterResource

class TermsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TermsBodyTermsnConditions()
        }
    }
}

// --- Helper composable functions with 'TermsnConditions' appended ---

@Composable
private fun SectionHeaderTermsnConditions(text: String) {
    Text(
        text = text,
        color = Orange,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SubHeaderTermsnConditions(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun BodyTextTermsnConditions(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// 💡 The BulletPoint function now handles both String and AnnotatedString internally
@Composable
private fun BulletPointTermsnConditions(text: String, isBold: Boolean = false) {
    val styledText: AnnotatedString = if (isBold) {
        buildAnnotatedString {
            append("- ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                append(text)
            }
        }
    } else {
        AnnotatedString("- $text")
    }

    Text(
        text = styledText,
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
    )
}

// This function was likely unused in the Terms content, but kept for completeness
@Composable
private fun KeyedTextTermsnConditions(key: String, description: String) {
    Text(
        text = buildAnnotatedString {
            append("- ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                append(key)
            }
            append(description)
        },
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// --- Main Composable Functions ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsBodyTermsnConditions() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            // Re-confirming correct usage for R.drawable
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Terms and Conditions",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(color = Color.Black)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            TermsContentTermsnConditions()
        }
    }
}

@Composable
fun TermsContentTermsnConditions() {
    // Title
    SectionHeaderTermsnConditions(text = "TERMS AND CONDITIONS")
    Spacer(modifier = Modifier.height(8.dp))

    // --- 1. Acceptance of Terms ---
    SectionHeaderTermsnConditions(text = "1. Acceptance of Terms")
    BodyTextTermsnConditions(text = "By accessing and using this rental platform (\"Platform\"), you agree to be bound by these Terms and Conditions, our Privacy Policy, and all applicable laws and regulations. If you do not agree with any part of these terms, you must not use the Platform.")

    // --- 2. User Accounts and Registration ---
    SectionHeaderTermsnConditions(text = "2. User Accounts and Registration")
    BodyTextTermsnConditions(text = "Users must be at least 18 years old to register and use the Platform.")
    BodyTextTermsnConditions(text = "You agree to provide accurate, current, and complete information during registration and keep your account updated.")
    BodyTextTermsnConditions(text = "You are responsible for maintaining the confidentiality of your account credentials and for all activities under your account.")
    BodyTextTermsnConditions(text = "The Platform reserves the right to suspend or terminate accounts that provide false information or engage in fraudulent activities.")

    // --- 3. KYC Verification and Documentation ---
    SectionHeaderTermsnConditions(text = "3. KYC Verification and Documentation")
    BodyTextTermsnConditions(text = "All users must complete Know Your Customer (KYC) verification before engaging in rental transactions.")
    BodyTextTermsnConditions(text = "You agree to provide valid government-issued identification and other required documents.")
    SubHeaderTermsnConditions(text = "Investigatory Use:")
    BodyTextTermsnConditions(text = "By submitting KYC documents, you acknowledge and consent that these documents may be used for investigatory purposes if you are:")
    BulletPointTermsnConditions(text = "Suspected of fraudulent activity")
    BulletPointTermsnConditions(text = "Involved in a dispute requiring investigation")
    BulletPointTermsnConditions(text = "Subject to legal proceedings related to Platform use")
    BulletPointTermsnConditions(text = "Liable for penalties under these Terms")
    BodyTextTermsnConditions(text = "The Platform will handle all KYC data in accordance with our Privacy Policy and applicable data protection laws.")

    // --- 4. Rental Transactions and Responsibilities ---
    SectionHeaderTermsnConditions(text = "4. Rental Transactions and Responsibilities")
    SubHeaderTermsnConditions(text = "4.1 For Owners:")
    BulletPointTermsnConditions(text = "You warrant that you own or have legal authority to rent the items listed.")
    BulletPointTermsnConditions(text = "You must accurately describe items, including any defects or damage.")
    BulletPointTermsnConditions(text = "You are responsible for ensuring items are safe, functional, and legal to rent.")
    SubHeaderTermsnConditions(text = "4.2 For Renters:")
    BulletPointTermsnConditions(text = "You must inspect items upon receipt and report any pre-existing damage within 2 hours.")
    BulletPointTermsnConditions(text = "You agree to use items only for their intended purpose and in accordance with any instructions provided.")
    BulletPointTermsnConditions(text = "You are liable for damage, loss, or theft occurring during the rental period.")

    // --- 5. Late Return Penalties ---
    SectionHeaderTermsnConditions(text = "5. Late Return Penalties")
    SubHeaderTermsnConditions(text = "5.1 Penalty Structure:")
    BodyTextTermsnConditions(text = "The following penalties apply for late returns:")
    BulletPointTermsnConditions(text = "3-8 hours late: 25% penalty of the daily rental rate")
    BulletPointTermsnConditions(text = "More than 8 hours late: 100% penalty (equivalent to an additional full day's rental)")
    SubHeaderTermsnConditions(text = "5.2 Calculation and Application:")
    BulletPointTermsnConditions(text = "Penalties are calculated based on the agreed daily rental rate.")
    BulletPointTermsnConditions(text = "The 100% penalty includes forfeiture of the item's use for that additional day.")
    BulletPointTermsnConditions(text = "Penalties are automatically charged to the payment method on file.")
    BulletPointTermsnConditions(text = "Partial hours are rounded up to the next full hour for penalty calculation.")
    SubHeaderTermsnConditions(text = "5.3 Consideration for Extenuating Circumstances:")
    BodyTextTermsnConditions(text = "Users may request penalty waiver consideration by:")
    BulletPointTermsnConditions(text = "Providing verifiable evidence of emergency circumstances")
    BulletPointTermsnConditions(text = "Notifying both the other party and Platform support immediately")
    BulletPointTermsnConditions(text = "Submitting documentation within 24 hours of the incident")
    BodyTextTermsnConditions(text = "The Platform reserves sole discretion to modify penalties in genuine emergency situations.")

    // --- 6. Cancellation and Refund Policy ---
    SectionHeaderTermsnConditions(text = "6. Cancellation and Refund Policy")
    SubHeaderTermsnConditions(text = "6.1 Renter Cancellations:")
    BulletPointTermsnConditions(text = "More than 48 hours before rental start: Full refund")
    BulletPointTermsnConditions(text = "24-48 hours before rental start: 50% refund")
    BulletPointTermsnConditions(text = "Less than 24 hours before rental start: No refund")
    SubHeaderTermsnConditions(text = "6.2 Owner Cancellations:")
    BodyTextTermsnConditions(text = "Owners who cancel confirmed bookings may be subject to:")
    BulletPointTermsnConditions(text = "Platform usage penalties")
    BulletPointTermsnConditions(text = "Temporary suspension of listing privileges")
    BulletPointTermsnConditions(text = "Compensation to affected renters")

    // --- 7. Security Deposits and Damage ---
    SectionHeaderTermsnConditions(text = "7. Security Deposits and Damage")
    BodyTextTermsnConditions(text = "Owners may require security deposits at their discretion.")
    BodyTextTermsnConditions(text = "Deposits will be held securely and returned within 7 business days after item return, minus any:")
    BulletPointTermsnConditions(text = "Repair costs for damage beyond normal wear and tear")
    BulletPointTermsnConditions(text = "Cleaning fees for excessive dirtiness")
    BulletPointTermsnConditions(text = "Late return penalties")
    BodyTextTermsnConditions(text = "Documentation (photos/videos) must support all damage claims.")

    // --- 8. Dispute Resolution ---
    SectionHeaderTermsnConditions(text = "8. Dispute Resolution")
    SubHeaderTermsnConditions(text = "8.1 Initial Resolution:")
    BulletPointTermsnConditions(text = "Parties must attempt to resolve disputes directly for 48 hours before escalating.")
    BulletPointTermsnConditions(text = "All communication should occur through the Platform's messaging system.")
    SubHeaderTermsnConditions(text = "8.2 Platform Mediation:")
    BulletPointTermsnConditions(text = "If direct resolution fails, either party may request Platform mediation.")
    BulletPointTermsnConditions(text = "You agree to provide all requested information and documentation promptly.")
    BulletPointTermsnConditions(text = "The Platform's decision in disputes is final and binding for Platform-related remedies.")
    SubHeaderTermsnConditions(text = "8.3 Legal Action:")
    BulletPointTermsnConditions(text = "These Terms do not prevent parties from pursuing legal action through appropriate courts.")
    BulletPointTermsnConditions(text = "You agree that the Platform is not liable for user-to-user disputes.")

    // --- 9. Prohibited Activities ---
    SectionHeaderTermsnConditions(text = "9. Prohibited Activities")
    BodyTextTermsnConditions(text = "Users may not:")
    BulletPointTermsnConditions(text = "List prohibited or illegal items")
    BulletPointTermsnConditions(text = "Use the Platform for fraudulent purposes")
    BulletPointTermsnConditions(text = "Harass or threaten other users")
    BulletPointTermsnConditions(text = "Circumvent the Platform's payment system")
    BulletPointTermsnConditions(text = "Provide false information")
    BulletPointTermsnConditions(text = "Violate intellectual property rights")

    // --- 10. Platform Fees ---
    SectionHeaderTermsnConditions(text = "10. Platform Fees")
    BodyTextTermsnConditions(text = "The Platform charges a service fee on completed transactions.")
    BodyTextTermsnConditions(text = "Fees are clearly displayed before transaction confirmation.")
    BodyTextTermsnConditions(text = "You authorize the Platform to deduct fees from transaction amounts.")

    // --- 11. Limitation of Liability ---
    SectionHeaderTermsnConditions(text = "11. Limitation of Liability")
    BodyTextTermsnConditions(text = "The Platform is a venue connecting owners and renters and is not party to rental agreements.")
    BodyTextTermsnConditions(text = "We do not guarantee item quality, safety, or legality.")
    BodyTextTermsnConditions(text = "Our maximum liability is limited to the fees we collected from the specific transaction in dispute.")
    BodyTextTermsnConditions(text = "We are not liable for:")
    BulletPointTermsnConditions(text = "Personal injury or property damage")
    BulletPointTermsnConditions(text = "User interactions and agreements")
    BulletPointTermsnConditions(text = "Items lost, stolen, or damaged")
    BulletPointTermsnConditions(text = "Third-party actions")

    // --- 12. Termination and Suspension ---
    SectionHeaderTermsnConditions(text = "12. Termination and Suspension")
    BodyTextTermsnConditions(text = "We may suspend or terminate accounts for:")
    BulletPointTermsnConditions(text = "Violation of these Terms")
    BulletPointTermsnConditions(text = "Fraudulent activity")
    BulletPointTermsnConditions(text = "Multiple user complaints")
    BulletPointTermsnConditions(text = "Legal or safety concerns")
    BulletPointTermsnConditions(text = "Non-payment of fees or penalties")

    // --- 13. Modifications to Terms ---
    SectionHeaderTermsnConditions(text = "13. Modifications to Terms")
    BodyTextTermsnConditions(text = "We may update these Terms at any time.")
    BodyTextTermsnConditions(text = "Continued use after changes constitutes acceptance of modified Terms.")
    BodyTextTermsnConditions(text = "Material changes will be communicated via email or Platform notification.")

    // --- 14. Governing Law and Jurisdiction ---
    SectionHeaderTermsnConditions(text = "14. Governing Law and Jurisdiction")
    BodyTextTermsnConditions(text = "These Terms are governed by the laws of [Your Jurisdiction]. Any disputes shall be resolved in the courts of [Your Jurisdiction].")

    // --- 15. Contact Information ---
    SectionHeaderTermsnConditions(text = "15. Contact Information")
    BodyTextTermsnConditions(text = "For questions about these Terms, contact customer support")

    Spacer(modifier = Modifier.height(32.dp))
}

@Preview(showBackground = true)
@Composable
fun TermsPreviewTermsnConditions() {
    TermsBodyTermsnConditions()
}