# APK Build Guide / Guida alla Compilazione dell'APK

This document is available in / Questo documento è disponibile in:
* 🇮🇹 [Italiano (Italian)](#italiano)
* 🇬🇧 [English (English)](#english)

---

<a name="italiano"></a>
# 🇮🇹 Guida in Italiano: Generazione e Compilazione dell'APK

Questa guida descrive come compilare e scaricare l'APK (Android Package) per l'applicazione **Arena AI** sia in modalità **Debug** che **Release**.

## Metodo 1: Generazione tramite Google AI Studio (Consigliato per Debug)

Google AI Studio consente di compilare l'applicazione nel cloud in modo semplice e veloce.

### Passaggi:
1. Apri il progetto in **Google AI Studio**.
2. Fai clic sul menu delle **Impostazioni** (icona dell'ingranaggio nell'angolo in alto a destra o nella barra laterale).
3. Seleziona **Generate APK/AAB**.
4. Attendi il completamento della compilazione nel cloud.
5. Scarica il file `.apk` generato direttamente sul tuo computer o telefono.

---

## Metodo 2: Compilazione Locale sul tuo Computer (Debug & Release)

Se desideri creare una build di tipo **Release** (`assembleRelease`) o personalizzare la compilazione, puoi farlo localmente sul tuo PC.

### Prerequisiti:
* **Java Development Kit (JDK):** Versione 17 o superiore installata sul computer.
* **Android Studio:** Consigliato per scaricare l'SDK Android e gestire il progetto (scaricabile gratuitamente da [developer.android.com](https://developer.android.com/studio)).

### Passaggi dettagliati:

1. **Esporta il progetto da AI Studio:**
   * Apri il menu Impostazioni in AI Studio.
   * Clicca su **Export project as ZIP** e scarica l'archivio sul computer.
   * Estrai il file ZIP in una cartella locale.

2. **Apri il progetto in Android Studio:**
   * Apri Android Studio, seleziona **Open** e scegli la cartella estratta.
   * Attendi che Android Studio completi la sincronizzazione automatica dei file Gradle (potrebbe richiedere qualche minuto alla prima apertura).

3. **Compilazione tramite Terminale:**
   Apri la scheda **Terminal** integrata in Android Studio (in basso a sinistra) o la riga di comando (Prompt dei comandi / PowerShell su Windows, Terminale su macOS/Linux) posizionandoti nella cartella principale del progetto ed esegui i comandi descritti di seguito.

---

### 🔧 Generazione APK di Debug (Debug Build)
L'APK di debug viene firmato automaticamente con una chiave di test locale generata da Gradle ed è subito installabile sul telefono per effettuare test.

* **Su macOS / Linux:**
  ```bash
  ./gradlew assembleDebug
  ```
* **Su Windows (Prompt dei comandi / CMD):**
  ```cmd
  gradlew assembleDebug
  ```
* **Su Windows (PowerShell):**
  ```powershell
  ./gradlew assembleDebug
  ```

* **Percorso del file generato:**
  `app/build/outputs/apk/debug/app-debug.apk`

---

### 🚀 Generazione APK di Release (Release Build)
L'APK di release è altamente ottimizzato, più veloce, privato dei log di debug e pronto per la distribuzione sul Play Store o ad utenti esterni.

* **Su macOS / Linux:**
  ```bash
  ./gradlew assembleRelease
  ```
* **Su Windows (Prompt dei comandi / CMD):**
  ```cmd
  gradlew assembleRelease
  ```
* **Su Windows (PowerShell):**
  ```powershell
  ./gradlew assembleRelease
  ```

* **Percorso del file generato:**
  `app/build/outputs/apk/release/app-release-unsigned.apk` (oppure `app-release.apk` se è già stata configurata una chiave di firma nel file Gradle).

> [!NOTE]
> **Nota importante sulla firma (Signing):** Se non hai configurato un keystore (chiave privata di firma) all'interno del file `app/build.gradle.kts`, Gradle produrrà un file contrassegnato come **unsigned** (non firmato). Gli APK non firmati vengono bloccati dal sistema operativo Android all'installazione per motivi di sicurezza. Per poterlo installare su un telefono o pubblicarlo, dovrai firmarlo usando uno strumento come `apksigner` o configurare il blocco `signingConfigs` nel build Gradle.

---

## Come Installare l'APK sul tuo Dispositivo Android
1. Trasferisci il file `.apk` compilato sul telefono (tramite cavo USB, servizi Cloud come Google Drive, Email o messaggistica come Telegram).
2. Apri un'applicazione di gestione file (File Manager) sul tuo smartphone e tocca il file `.apk`.
3. Consenti l'installazione da "Sorgenti Sconosciute" (o "Installa app sconosciute") se richiesto dal browser o dal gestore file.
4. Segui la procedura guidata e avvia **Arena AI**!

---

<a name="english"></a>
# 🇬🇧 English Guide: Generating and Compiling the APK

This guide explains how to build and download the APK (Android Package) for the **Arena AI** app in both **Debug** and **Release** modes.

## Method 1: Generating via Google AI Studio (Recommended for Debug)

Google AI Studio allows you to easily compile the application in the cloud with one click.

### Steps:
1. Open the project in **Google AI Studio**.
2. Click on the **Settings** menu (gear icon in the top-right corner or sidebar).
3. Select **Generate APK/AAB**.
4. Wait for the cloud build process to finish.
5. Download the generated `.apk` file directly to your computer or mobile device.

---

## Method 2: Local Compilation on Your Computer (Debug & Release)

If you want to compile a **Release** build (`assembleRelease`) or customize compilation options, you can do so locally on your PC.

### Prerequisites:
* **Java Development Kit (JDK):** Version 17 or higher installed on your system.
* **Android Studio:** Recommended to download the Android SDK and manage the project (downloadable for free from [developer.android.com](https://developer.android.com/studio)).

### Detailed Steps:

1. **Export the Project from AI Studio:**
   * Open the Settings menu in AI Studio.
   * Click **Export project as ZIP** and download the archive.
   * Extract the ZIP file onto your local drive.

2. **Open the Project in Android Studio:**
   * Launch Android Studio, select **Open**, and navigate to the extracted folder.
   * Wait for Gradle to finish synchronizing project files (it may take a few minutes on the first run).

3. **Compilation via Terminal:**
   Open Android Studio's integrated **Terminal** (bottom-left tab) or your system's command line (Command Prompt / PowerShell on Windows, Terminal on macOS/Linux), navigate to the project root directory, and execute the desired commands.

---

### 🔧 Generating a Debug APK
A debug APK is signed automatically with a default local test key generated by Gradle, and is ready to be installed immediately on your device for testing.

* **On macOS / Linux:**
  ```bash
  ./gradlew assembleDebug
  ```
* **On Windows (Command Prompt / CMD):**
  ```cmd
  gradlew assembleDebug
  ```
* **On Windows (PowerShell):**
  ```powershell
  ./gradlew assembleDebug
  ```

* **Generated file path:**
  `app/build/outputs/apk/debug/app-debug.apk`

---

### 🚀 Generating a Release APK
A release APK is optimized for performance, obfuscated, and ready for deployment on Google Play or distribution to external users.

* **On macOS / Linux:**
  ```bash
  ./gradlew assembleRelease
  ```
* **On Windows (Command Prompt / CMD):**
  ```cmd
  gradlew assembleRelease
  ```
* **On Windows (PowerShell):**
  ```powershell
  ./gradlew assembleRelease
  ```

* **Generated file path:**
  `app/build/outputs/apk/release/app-release-unsigned.apk` (or `app-release.apk` if a signing key has been configured).

> [!NOTE]
> **Important Note on Signing:** If you have not configured a release signing key (keystore) in your `app/build.gradle.kts` file, Gradle will output an **unsigned** APK. Unsigned APKs are blocked from installation by Android's security systems. To install or distribute it, you must sign it using `apksigner` or configure a custom `signingConfigs` block in your build Gradle script.

---

## How to Install the APK on Your Android Device
1. Transfer the compiled `.apk` file to your Android phone (via USB, Cloud storage like Google Drive, Email, or messaging apps like Telegram).
2. Open a File Manager app on your smartphone and tap the `.apk` file.
3. Allow installations from "Unknown Sources" if prompted by your system.
4. Follow the on-screen wizard to complete installation and launch **Arena AI**!
