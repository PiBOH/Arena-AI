# APK Build Guide

How to build and sign the **Arena AI** APK in **debug** and **release** mode.

> This guide predates the current setup. The authoritative source is
> `app/build.gradle.kts` and `AGENTS.md`. In particular, release signing reads
> `KEYSTORE_PATH`, `STORE_PASSWORD` and `KEY_PASSWORD` from the environment.

## Method 1: Google AI Studio (debug only)

Google AI Studio can build the app in the cloud.

1. Open the project in **Google AI Studio**.
2. Open the **Settings** menu (gear icon in the top-right corner or sidebar).
3. Select **Generate APK/AAB**.
4. Wait for the cloud build to finish.
5. Download the generated `.apk` to your computer or phone.

---

## Method 2: Local build (debug and release)

### Requirements

- **Java Development Kit (JDK) 17 or newer** (JDK 21 recommended; Gradle 9
 refuses Java 8).
- **Android Studio** or the command-line SDK tools, with **Android SDK 36**.

### Steps

1. **Get the sources**: clone the repository or export the project from AI
 Studio as a ZIP and extract it.
2. **Open the project**: in Android Studio choose **Open** and select the
 project folder, then wait for the Gradle sync to finish.
3. **Build from the terminal**: open Android Studio's integrated **Terminal**
 (bottom-left) or a system shell in the project root.

### Debug APK

The debug APK is signed automatically with a local test key and can be installed
immediately for testing.

- **macOS / Linux:**
  ```bash
 ./gradlew assembleDebug
  ```
- **Windows (Command Prompt / CMD):**
  ```cmd
 gradlew assembleDebug
  ```
- **Windows (PowerShell):**
  ```powershell
 ./gradlew assembleDebug
  ```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK

The release APK is optimized and stripped of debug logging, ready for
distribution or Google Play.

- **macOS / Linux:**
  ```bash
 ./gradlew assembleRelease
  ```
- **Windows (Command Prompt / CMD):**
  ```cmd
 gradlew assembleRelease
  ```
- **Windows (PowerShell):**
  ```powershell
 ./gradlew assembleRelease
  ```

Output: `app/build/outputs/apk/release/app-release.apk` when a signing config is
present, otherwise `app-release-unsigned.apk`.

> [!NOTE]
> **Signing:** without a configured keystore, Gradle produces an **unsigned**
> APK, which Android refuses to install. See
> [How to sign the APK](#how-to-sign-the-apk) below.

---

## How to sign the APK (release)

A release APK must be digitally signed with a certificate (keystore) before it
can be installed on a real device. There are three ways to do it.

### Option A: Android Studio (graphical, recommended)

1. Open the project in **Android Studio**.
2. From the top menu select **Build** > **Generate Signed Bundle / APK...**.
3. Choose **APK** and click **Next**.
4. Under **Keystore path**:
 - if you already have a key, click **Choose existing...** and select it;
 - otherwise click **Create new...** and fill in password, alias and
 certificate details.
5. Enter the keystore password, key alias and key password, then click **Next**.
6. Select the **release** build variant and click **Finish**.
7. The signed APK is written to the destination folder you picked (usually
  `app/release/app-release.apk`).

### Option B: Automatic signing with Gradle

This is how the project signs releases: the keystore path and passwords come
from the environment.

1. Create a keystore with `keytool`:
  ```bash
 keytool -genkeypair -v -keystore my-release-key.jks \
 -keyalg RSA -keysize 4096 -validity 10000 -alias upload
  ```
2. Export the credentials before building:
  ```bash
 export KEYSTORE_PATH="$PWD/my-release-key.jks"
 export STORE_PASSWORD="your_store_password"
 export KEY_PASSWORD="your_key_password"
 ./gradlew assembleRelease
  ```
  `app/build.gradle.kts` maps `KEYSTORE_PATH` to the `release` signing config
 with alias `upload`, so nothing secret is committed.

### Option C: Sign an existing APK manually

If you already built an unsigned APK, sign it with the SDK's `apksigner`
(usually `Android/Sdk/build-tools/<version>/apksigner`):

1. Create a keystore with `keytool` if you do not have one (see above).
2. Align the APK first with `zipalign`:
  ```bash
 zipalign -v -p 4 app-release-unsigned.apk app-release-aligned.apk
  ```
3. Sign it:
  ```bash
 apksigner sign --ks my-release-key.jks --out app-release-signed.apk app-release-aligned.apk
  ```
4. Verify the signature:
  ```bash
 apksigner verify app-release-signed.apk
  ```

---

## Installing the APK on a device

1. Transfer the `.apk` to the phone (USB, cloud storage, email or a messaging
 app).
2. Open a file manager on the phone and tap the file.
3. Allow installation from "Unknown sources" if the system asks.
4. Follow the wizard and launch **Arena AI**.
