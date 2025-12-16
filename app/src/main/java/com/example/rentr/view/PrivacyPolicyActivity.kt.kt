package com.example.rentr

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rentr.ui.theme.Orange

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrivacyPolicyBody()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyBody() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    containerColor = Color.Black // Sticky TopBar color
                ),
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? ComponentActivity)?.finish()
                    }) {
                        Icon(
                            // Replace R.drawable.baseline_arrow_back_24 with your actual back icon resource
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Privacy Policy",
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
            // Render the Privacy Policy Content
            PrivacyPolicyContent()
        }
    }
}

// Helper composable for Section Header (H2)
@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        color = Orange,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

// Helper composable for Sub-Section Header (H3)
@Composable
fun SubHeader(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

// Helper composable for regular body text
@Composable
fun BodyText(text: String) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 14.sp,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}

// Helper composable for body text with bolded keyword prefix
@Composable
fun KeyedText(key: String, description: String) {
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

@Composable
fun PrivacyPolicyContent() {
    // Styles for specific elements not handled by helpers
    val grayStyle = SpanStyle(fontSize = 14.sp, color = Color.Gray)
    val emailStyle = SpanStyle(textDecoration = TextDecoration.Underline, color = Orange)

    // Title and Dates
    Text(
        text = "Privacy Policy for Rentr",
        color = Orange,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 8.dp)
    )
    Text(
        text = buildAnnotatedString {
            withStyle(grayStyle) { append("Last Updated: April 12, 2025\n") }
            withStyle(grayStyle) { append("Effective Date: April 12, 2025") }
        },
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    // --- 1. Introduction ---
    SectionHeader(text = "1. Introduction")
    BodyText(text = "Welcome to Rentr. Your privacy is critically important to us. This Privacy Policy explains how Rentr collects, uses, discloses, and safeguards your information when you use our rental marketplace platform, mobile application, and related services.")
    BodyText(text = "Please read this Privacy Policy carefully. By accessing or using Rentr, you agree to be bound by this policy. If you do not agree with our practices, you must not use our services.")
    Spacer(modifier = Modifier.height(16.dp))

    // --- 2. Information We Collect ---
    SectionHeader(text = "2. Information We Collect")

    SubHeader(text = "2.1 Information You Provide to Us")
    KeyedText(key = "Account Information:", description = " Full name, email address, phone number, password, profile picture")
    KeyedText(key = "Contact Details:", description = " Physical address, preferred pickup locations, additional phone numbers")
    KeyedText(key = "Financial Information:", description = " Payment card details (processed via secure third-party processors), billing address, transaction history, earnings, and spending records")
    KeyedText(key = "Identity Verification (KYC):", description = " Government-issued IDs (passport, driver's license, national ID), proof of address documents, selfie verification images, and any additional documents required for verification")
    KeyedText(key = "Rental Listings:", description = " Item descriptions, photos, pricing information, availability calendars, pickup/drop-off locations")
    KeyedText(key = "Communications:", description = " Messages between users, support tickets, feedback, reviews, and ratings")
    KeyedText(key = "User Content:", description = " Photos, descriptions, reviews, and any other content you upload to the Platform")

    SubHeader(text = "2.2 Information We Collect Automatically")
    KeyedText(key = "Device Information:", description = " IP address, browser type, operating system, device identifiers, mobile network information")
    KeyedText(key = "Usage Data:", description = " Pages visited, features used, search queries, clickstream data, time stamps, duration of visits")
    KeyedText(key = "Location Information:", description = " When you enable location services, we collect precise or approximate location data for: Showing items available near you, Setting and verifying pickup/drop-off locations, Tracking rental handoffs and returns, Calculating distances between users")
    KeyedText(key = "Cookies and Tracking Technologies:", description = " We use cookies, web beacons, pixel tags, and similar technologies to enhance your experience and analyze Platform usage")

    SubHeader(text = "2.3 Information from Third Parties")
    BodyText(text = "Payment processors (transaction status, payment method details), Identity verification services, Social media platforms (if you choose to connect accounts), Background check providers (where permitted by law), Analytics and marketing partners.")
    Spacer(modifier = Modifier.height(16.dp))

    // --- 3. How We Use Your Information ---
    SectionHeader(text = "3. How We Use Your Information")
    BodyText(text = "We use the information we collect for the following purposes:")
    KeyedText(key = "Service Delivery:", description = " Create and manage your account, process transactions, facilitate rentals, provide customer support")
    KeyedText(key = "Verification and Security:", description = " Verify your identity through KYC verification, Prevent fraud, abuse, and illegal activities, Investigate suspicious behavior and violations of our Terms")
    KeyedText(key = "Communication:", description = " Send transaction confirmations, rental updates, security alerts, support messages, and promotional communications (you may opt-out)")
    KeyedText(key = "Platform Improvement:", description = " Develop new features, improve user experience, conduct research and analysis")
    KeyedText(key = "Legal Compliance:", description = " Comply with applicable laws, regulations, legal processes, and enforceable governmental requests")
    KeyedText(key = "Personalization:", description = " Customize content, show relevant listings, and personalize your experience")
    KeyedText(key = "Payment Processing:", description = " Process payments, calculate fees, handle refunds, and manage payouts")
    Spacer(modifier = Modifier.height(16.dp))

    // --- 4. Special Processing Circumstances ---
    SectionHeader(text = "4. Special Processing Circumstances")

    SubHeader(text = "4.1 KYC Document Processing and Investigation")
    BodyText(text = "Your KYC documents are processed with strict security measures. We use them for: Initial identity verification during registration, Periodic re-verification as required by law or our policies, Fraud prevention and detection.")
    BodyText(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                append("Investigation Authority:")
            }
            append(" By submitting KYC documents, you acknowledge and consent that Rentr may investigate these documents and your account activity when: Fraudulent activity is suspected, Multiple user complaints are received, Unusual transaction patterns are detected, Legal authorities require investigation, Insurance claims are filed, Platform terms violations are identified")
        }.toString()
    )

    SubHeader(text = "4.2 Rental Order Tracking and Monitoring")
    BodyText(text = "To ensure safe, reliable transactions and prevent fraud, Rentr implements comprehensive rental tracking:")
    KeyedText(key = "Comprehensive Transaction Monitoring:", description = " We track all rental activities from booking initiation to completion")
    KeyedText(key = "Two-Way Activity Tracking:", description = " Both owners and renters can monitor: Booking status and confirmation, Payment processing stages, Pickup verification and timestamps, Return initiation and completion, Communication history related to each transaction")
    KeyedText(key = "Location-Based Tracking:", description = " When location services are enabled for active rentals, we collect location data to: Verify pickup and return at agreed locations, Monitor rental handoff processes, Track late returns for penalty calculation, Provide evidence in case of disputes")

    SubHeader(text = "4.3 Investigation of Historical Activities")
    BodyText(
        text = buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                append("Important Notice:")
            }
            append(" Rentr reserves the right to investigate past rental activities and user history when: Current fraudulent activity suggests past violations, Legal authorities request historical data, Insurance investigations require historical context, Patterns of abuse are detected across multiple transactions.")
        }.toString()
    )
    BodyText(text = "During such investigations, we may review: Complete rental history and transaction records, All communications between involved parties, Uploaded documents and verification materials, Payment and refund histories, User behavior patterns and Platform interactions.")
    Spacer(modifier = Modifier.height(16.dp))

    // --- 5. Information Sharing and Disclosure ---
    SectionHeader(text = "5. Information Sharing and Disclosure")

    SubHeader(text = "5.1 Sharing Between Users")
    BodyText(text = "During transactions, we share limited information between owners and renters: First name and last initial, Profile picture (if provided), Overall rating and number of completed transactions, Contact information necessary for coordination (phone number, in-app messaging), Verification status indicators.")

    SubHeader(text = "5.2 Service Providers")
    BodyText(text = "We share information with trusted third-party providers who assist with: Payment processing, Cloud storage and hosting, Customer support services, Analytics and performance monitoring, Marketing and advertising (with your consent), Identity verification.")

    SubHeader(text = "5.3 Legal Requirements")
    BodyText(text = "We may disclose your information: To comply with legal obligations or court orders, To protect the rights, property, or safety of Rentr, our users, or others, In connection with business transfers (merger, acquisition, or asset sale), To law enforcement when required by law.")

    SubHeader(text = "5.4 With Your Consent")
    BodyText(text = "We share information with third parties when you give us explicit consent to do so.")
    Spacer(modifier = Modifier.height(16.dp))

    // --- 6. Data Security ---
    SectionHeader(text = "6. Data Security")
    BodyText(text = buildAnnotatedString {
        append("We implement comprehensive security measures: ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("Encryption:") }
        append(" Industry-standard encryption for data in transit and at rest, ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("Access Controls:") }
        append(" Strict role-based access to personal information, ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("Regular Security Audits:") }
        append(" Periodic assessments and penetration testing, ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("Secure Document Storage:") }
        append(" KYC documents stored in encrypted, access-controlled systems, ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("Employee Training:") }
        append(" Regular privacy and security training for all staff, ")
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("Incident Response:") }
        append(" Established procedures for data breach response.")
    }.toString())
    Spacer(modifier = Modifier.height(16.dp))

    // --- 7. Data Retention ---
    SectionHeader(text = "7. Data Retention")
    KeyedText(key = "Account Information:", description = " While active + 5 years after deactivation")
    KeyedText(key = "Transaction Records:", description = " 7 years (for legal, tax, and accounting purposes)")
    KeyedText(key = "KYC Documents:", description = " 5 years after account closure or as required by law")
    KeyedText(key = "Communications:", description = " 3 years from date of communication")
    KeyedText(key = "Investigation Records:", description = " Indefinitely for users involved in fraud or serious violations")
    KeyedText(key = "Location Data:", description = " 30 days for operational purposes, longer if involved in disputes")
    Spacer(modifier = Modifier.height(16.dp))

    // --- 8. Your Privacy Rights ---
    SectionHeader(text = "8. Your Privacy Rights")
    BodyText(text = "Depending on your location, you may have rights to: Access your personal information, Correct inaccurate or incomplete data, Request deletion of your data, Restrict or object to certain processing, Data portability, Withdraw consent (where processing is based on consent).")
    Text(
        text = buildAnnotatedString {
            append("To exercise these rights, contact us at ")
            withStyle(emailStyle) { append("privacy@rentr.com") }
            append(".")
        },
        color = Color.White,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(16.dp))

    // --- 9. International Data Transfers ---
    SectionHeader(text = "9. International Data Transfers")
    BodyText(text = "Your information may be transferred to and processed in countries outside your residence. We ensure appropriate safeguards are in place, including standard contractual clauses and other approved mechanisms.")

    // --- 10. Children's Privacy ---
    SectionHeader(text = "10. Children's Privacy")
    BodyText(text = "Rentr is not intended for individuals under 18 years of age. We do not knowingly collect personal information from children. If you believe we have collected information from a child, please contact us immediately.")

    // --- 11. Updates to This Policy ---
    SectionHeader(text = "11. Updates to This Policy")
    BodyText(text = "We may update this Privacy Policy periodically. We will notify you of significant changes through: Email notifications, In-app announcements, Updated \"Last Updated\" date at the top of this policy. Your continued use of Rentr after changes constitutes acceptance of the updated policy.")
    Spacer(modifier = Modifier.height(16.dp))

    // --- 12. Contact Information ---
    SectionHeader(text = "12. Contact Information")
    BodyText(text = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) { append("Data Protection Officer\n") }
        append("Rentr Platform\n")
        append("Email: ")
        withStyle(emailStyle) { append("privacy@rentr.com") }
        append("\n")
        append("Address: [Your Business Address]\n")
        append("Phone: [Your Contact Number]")
    }.toString())
    Spacer(modifier = Modifier.height(16.dp))

    // --- 13. Specific Consent Acknowledgments ---
    SectionHeader(text = "13. Specific Consent Acknowledgments")
    BodyText(text = "By using Rentr, you specifically acknowledge and consent to:")
    BodyText(text = buildAnnotatedString { append("1. "); withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("KYC Investigation:") }; append(" Your verification documents may be investigated in cases of suspected fraud, violations, or legal requirements") }.toString())
    BodyText(text = buildAnnotatedString { append("2. "); withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Rental Tracking:") }; append(" Comprehensive tracking of rental activities from owner to renter and renter to owner") }.toString())
    BodyText(text = buildAnnotatedString { append("3. "); withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Historical Review:") }; append(" Investigation of past rental activities when current circumstances warrant") }.toString())
    BodyText(text = buildAnnotatedString { append("4. "); withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Location Monitoring:") }; append(" Collection of location data during active rentals for verification and safety purposes") }.toString())
    BodyText(text = buildAnnotatedString { append("5. "); withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Fraud Prevention:") }; append(" Active monitoring and analysis of Platform activities to prevent abuse") }.toString())
    Spacer(modifier = Modifier.height(16.dp))

    // --- 14. Dispute Resolution ---
    SectionHeader(text = "14. Dispute Resolution")
    Text(
        text = buildAnnotatedString {
            append("If you have concerns about our privacy practices, please contact us first at ")
            withStyle(emailStyle) { append("privacy@rentr.com") }
            append(". If we cannot resolve your concern, you may have the right to lodge a complaint with your local data protection authority.")
        },
        color = Color.White,
        fontSize = 14.sp
    )
    Spacer(modifier = Modifier.height(16.dp))

    // --- APPENDIX: TRACKING AND INVESTIGATION DETAILS ---
    SectionHeader(text = "APPENDIX: TRACKING AND INVESTIGATION DETAILS")

    SubHeader(text = "A. What We Track During Transactions")
    BodyText(text = "- Booking request timestamps and responses, Payment authorization, completion, and refund processing, Pickup confirmation (time, location verification), Return process (initiation, completion, condition verification), All in-app communications related to specific rentals, Location data for pickup/return verification (when enabled), Late return calculations and penalty assessments")

    SubHeader(text = "B. Investigation Triggers")
    BodyText(text = "- Multiple users report issues with the same individual, Payment disputes or chargebacks exceed normal thresholds, KYC document anomalies or inconsistencies are detected, Automated systems flag suspicious patterns or behaviors, Law enforcement or regulatory authorities request information, Insurance companies submit claims requiring investigation, Significant or repeated late returns, damages, or violations occur")

    SubHeader(text = "C. User Responsibilities for Privacy")
    BodyText(text = "- You agree to: Maintain accurate and current account information, Secure your login credentials and account access, Report suspicious activities or privacy concerns immediately, Cooperate with legitimate investigations by providing requested information, Respect the privacy of other users during transactions, Update your privacy settings according to your preferences")

    // Final Statement
    Spacer(modifier = Modifier.height(24.dp))
    BodyText(text = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("By using the Rentr platform, you confirm that you have read, understood, and agree to this Privacy Policy, including the specific provisions regarding KYC investigation, rental tracking, and fraud prevention measures.\n\n") }
        append("*This policy is effective as of December 04, 2025, and supersedes all previous versions.*")
    }.toString())
    Spacer(modifier = Modifier.height(32.dp))
}

@Preview(showBackground = true)
@Composable
fun PrivacyPolicyPreview() {
    PrivacyPolicyBody()
}