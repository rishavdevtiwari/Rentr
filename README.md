# Rentr — Rental Marketplace (Android)

Rentr is a rental marketplace Android application where users can discover items, request rentals, chat, and manage their listings. The project also includes an administrative side (within the same Android project) for platform governance features such as KYC verification, product approval, and review moderation.

## Key Features

### User Side
- Authentication (Firebase Auth)
- Browse products (search, categories)
- Create/manage listings
- Rental workflow (requests, history)
- Chat & conversations
- Notifications
- Policies: Privacy Policy, Terms & Conditions
- Change password
- Payments (Khalti)

### Admin Side
- Admin dashboard
- KYC verification / management
- Product approval / product management
- Review moderation (flagged reviews)

## Tech Stack
- **Kotlin**
- **Jetpack Compose**
- **MVVM architecture**
- **Firebase** (Auth, Firestore, Realtime Database, Messaging)
- **Retrofit + Gson**
- **Cloudinary** (media uploads)
- **OpenAI client / Groq API key usage** (via BuildConfig)
- **Instrumented testing** (Espresso / AndroidX Test)

## Project Structure (high level)
- `app/src/main/java/com/example/rentr/` — app code (views, viewmodels, repositories, etc.)
- `app/src/androidTest/` — instrumented tests
- `app/google-services.json` — Firebase config (replace with your own if required)

## Setup & Run (Android Studio)

### Prerequisites
- Android Studio (recent stable)
- JDK 11+
- Android SDK installed (project targets modern SDKs)

### 1) Open the project
Open the extracted folder in Android Studio:
- `Rentr-main/`

### 2) Firebase configuration
This project already contains a `google-services.json` under `app/`.
If your instructor requires your own Firebase project, replace it with your own:
- Firebase Console → Project Settings → Your apps → Download `google-services.json`
- Put it at: `app/google-services.json`

### 3) Add required keys in `local.properties`
This project reads secrets from **local.properties** and injects them at build time:

Create or edit **`local.properties`** (in the project root, same level as `settings.gradle.kts`) and add:

```properties
# Cloudinary
cloudinary_url=YOUR_CLOUDINARY_URL

# Groq/OpenAI key used by the app (injected into BuildConfig)
GROQ_API_KEY=YOUR_GROQ_API_KEY

# (Optional) Release signing config if building a release APK
storeFile=YOUR_KEYSTORE_PATH
storePassword=YOUR_STORE_PASSWORD
keyAlias=YOUR_KEY_ALIAS
keyPassword=YOUR_KEY_PASSWORD