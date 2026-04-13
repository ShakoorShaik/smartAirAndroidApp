# SmartAir Health — Android App

SmartAir is a comprehensive asthma management platform for children, parents, and healthcare providers. Built as a native Android application, it enables children to monitor their respiratory health, parents to stay informed about their child's condition, and providers to access clinical insights — all in one connected system.

## Demo

https://github.com/user-attachments/assets/5dccf876-4035-4406-8a99-d6700c6d609d

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Firebase Setup](#firebase-setup)
- [Building & Running](#building--running)
- [Running Tests](#running-tests)

---

## Overview

SmartAir centers on the **asthma zone system** — a clinically recognized framework that classifies a child's respiratory control into three zones based on Peak Expiratory Flow (PEF) readings relative to a personal best baseline:

| Zone   | Meaning                        |
|--------|--------------------------------|
| Green  | Good control — 80–100% of PB   |
| Yellow | Caution — 50–80% of PB         |
| Red    | Emergency — below 50% of PB    |

The app supports three distinct user roles — **Child**, **Parent**, and **Provider** — each with their own tailored dashboard and feature set. Accounts are linked using generated codes so a parent can monitor their child and grant visibility to a healthcare provider.

---

## Features

### Child
- **Dashboard** with live zone status card, emergency button, and quick-log actions
- **Daily Check-In** for tracking how the child feels each day
- **Log Inhaler Usage** — rescue vs. controller medication, dose count, pre/post breathing ratings, and canister level tracking with low-canister alerts
- **Log Triggers & Symptoms** — record asthma-related events and symptoms
- **Inhaler Technique Practice** — step-by-step guided technique with video support
- **Zone Guidance** — contextual action steps for Green, Yellow, and Red zones
- **Emergency Flow** — one-tap emergency alert with guided response steps
- **Task & Badge System** — gamified streaks and badges to encourage adherence
- **History** — view past check-ins, medication logs, and PEF readings

### Parent
- **Multi-child Dashboard** — monitor all linked children from one view
- **Medicine Management** — track medications, expiry dates, and canister levels with a calendar view
- **Adherence Scheduling** — configure and monitor medication adherence plans
- **PEF & Zone History** — review historical peak flow readings and zone trends
- **Provider Linking** — generate and share codes to connect a healthcare provider
- **Provider Visibility Controls** — configure exactly what data a provider can see
- **Data Export** — export a child's health history as a PDF report
- **Low-Canister Alerts** — real-time notifications when inhaler levels are low

### Provider
- **Patient Overview** — view a list of all linked children
- **PEF Analytics** — peak flow data visualized over time
- **Adherence Reports** — track how consistently a child follows their plan
- **Rescue Medication Trends** — monitor rescue inhaler usage frequency
- **Triage & Emergency History** — review past emergency events
- **Trigger & Symptom Analysis** — identify patterns in asthma triggers and symptoms

---

## Architecture

<img width="4346" height="3899" alt="Image" src="https://github.com/user-attachments/assets/2e3b1e2b-e726-468e-8b03-6f97a21bca60" />

The app uses a hybrid architecture:

- **MVP (Model-View-Presenter)** for the authentication layer, keeping Firebase logic separated from the UI.
- **Manager/Singleton Pattern** for all domain-specific data operations (e.g., `ZoneManager`, `PEFManager`, `MedicineManager`). Each manager encapsulates Firestore reads and writes for its domain and uses interface callbacks for async results.
- **Activity + Fragment Navigation** with bottom navigation bars for each role's dashboard.
- **Jetpack Compose** is included and used alongside traditional Views (XML layouts).

---

## Tech Stack

### Language & Platform

| Component | Details |
|-----------|---------|
| Language | Java (primary), Kotlin (Compose & build scripts) |
| Min SDK | API 24 (Android 7.0) |
| Target SDK | API 36 (Android 15) |
| Build System | Gradle 8.13.1 with Kotlin DSL |
| Kotlin Version | 2.0.21 |

### AndroidX & UI

| Library | Version | Purpose |
|---------|---------|---------|
| `androidx.appcompat` | 1.6.1 | Backward-compatible Activity/Fragment support |
| `androidx.constraintlayout` | 2.1.4 | Flexible XML-based layouts |
| `androidx.recyclerview` | 1.4.0 | Efficient list/grid rendering |
| `androidx.viewpager2` | 1.0.0 | Swipeable paged views |
| `androidx.cardview` | 1.0.0 | Material card containers |
| `com.google.android.material` | 1.10.0 | Material Design 3 components |

### Jetpack Compose

| Library | Version |
|---------|---------|
| Compose BOM | 2024.09.00 |
| `compose.ui` | BOM-managed |
| `compose.material3` | BOM-managed |
| `activity-compose` | 1.8.0 |

### Backend — Firebase

| Service | Library | Purpose |
|---------|---------|---------|
| Authentication | `firebase-auth:24.0.1` | Email/password login for all roles |
| Database | `firebase-firestore` | NoSQL document store for all app data |
| Analytics | `firebase-analytics` | Usage analytics |
| Google Services Plugin | 4.4.4 | Integrates `google-services.json` at build time |

### Data & Visualization

| Library | Version | Purpose |
|---------|---------|---------|
| MPAndroidChart | v3.1.0 | Line, bar, and other charts for health data |
| iText7 Core | 7.2.5 | PDF generation for health history export |
| LeonidsLib | 1.3.2 | Particle animations for gamification |

### Testing

| Library | Version | Purpose |
|---------|---------|---------|
| JUnit | 4.13.2 | Unit testing |
| Mockito Core | 5.11.0 | Mocking dependencies |
| Mockito Inline | 5.2.0 | Mocking final classes |
| AndroidX JUnit | 1.1.5 | AndroidX test extensions |
| Espresso | 3.5.1 | UI instrumentation testing |

### Other

- **Core Library Desugaring** (`desugar_jdk_libs:2.1.5`) — enables Java 8+ APIs on API 24+ devices

---

## Prerequisites

- **Android Studio** Koala (2024.1.1) or newer
- **JDK** 8 or higher
- **Android SDK** with API 36 platform tools installed
- **A Firebase project** (see setup below)
- A physical device or emulator running Android 7.0 (API 24) or higher

---

## Firebase Setup

The app uses Firebase as its backend. You must connect it to a Firebase project before building.

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Add an **Android app** with package name `com.example.smartair`.
3. Enable the following services:
   - **Authentication** → Email/Password sign-in method
   - **Firestore Database** → Start in test mode for development
   - **Analytics** (optional but included in the build)
4. Download `google-services.json` from the Firebase console.
5. Place `google-services.json` in the `app/` directory:
   ```
   smartAirAndroidApp/
   └── app/
       └── google-services.json   ← here
   ```

> **Note:** `google-services.json` is gitignored and is not included in the repository. Obtain it from the Firebase console or from a team member with project access.

---

## Building & Running

### Using Android Studio (Recommended)

1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd smartAirAndroidApp
   ```
2. Open the project in Android Studio (`File → Open`).
3. Let Gradle sync complete (it will download all dependencies automatically).
4. Select a device or emulator (API 24+ required).
5. Click **Run** or press `Shift + F10`.

### Using the Gradle Wrapper (CLI)

```bash
# Build a debug APK
./gradlew assembleDebug

# Build a release APK
./gradlew assembleRelease

# Install directly on a connected device
./gradlew installDebug
```

The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

### First Launch Flow

On first launch, `MainActivity` checks Firebase Auth state:

- **No authenticated user** → routes to Login
- **Authenticated** → reads `accountType` from Firestore and routes to the correct dashboard:
  - `Parent` → `ParentDashboardWithChildrenActivity`
  - `Child` → `ChildDashboardHome`
  - `Provider` → `ProviderHomePage`

### Creating Test Accounts

1. Register a **Parent** account via the Registration screen.
2. From the Parent dashboard, create a **Child** account linked to your profile.
3. Register a separate **Provider** account and use the parent-generated linking code to connect it.

---

## Running Tests

```bash
# Unit tests (no device needed)
./gradlew test

# Instrumentation tests (requires a connected device or running emulator)
./gradlew connectedAndroidTest

# Run a specific test class
./gradlew test --tests "com.example.smartair.YourTestClass"
```

Test source files are located in:
- `app/src/test/` — unit tests (JUnit + Mockito)
- `app/src/androidTest/` — instrumentation tests (Espresso)
