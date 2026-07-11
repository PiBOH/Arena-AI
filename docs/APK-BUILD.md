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
> **Nota importante sulla firma (Signing):** Se non hai configurato un keystore (chiave privata di firma) all'interno del file `app/build.gradle.kts`, Gradle produrrà un file contrassegnato come **unsigned** (non firmato). Gli APK non firmati vengono bloccati dal sistema operativo Android all'installazione per motivi di sicurezza.
> 
> Leggi la sezione [Come Firmare l'APK](#come-firmare-it) qui sotto per le istruzioni dettagliate su come creare una chiave e firmare l'app.

---

<a name="come-firmare-it"></a>
## 🔑 Come Firmare l'APK (Release)

Per poter installare un APK di tipo **Release** su un dispositivo reale, questo deve essere firmato digitalmente con un certificato (Keystore). Di seguito trovi le 3 modalità principali per firmare l'applicazione.

### Opzione A: Firmare tramite Android Studio (Metodo Grafico - Consigliato)
Questo è il metodo più semplice se utilizzi Android Studio sul tuo computer:
1. Apri il progetto in **Android Studio**.
2. Nel menu in alto, seleziona **Build** > **Generate Signed Bundle / APK...**.
3. Scegli **APK** e fai clic su **Next**.
4. In **Keystore path**:
   * Se hai già una chiave, fai clic su **Choose existing...** e selezionala.
   * Se non hai una chiave, fai clic su **Create new...** per crearne una nuova (compila i campi richiesti come password, alias e dettagli del certificato).
5. Inserisci le credenziali della chiave (Keystore password, Key alias, Key password) e fai clic su **Next**.
6. Seleziona la variante di build **release**, metti la spunta su eventuali opzioni di firma proposte e fai clic su **Finish**.
7. Troverai l'APK firmato e pronto all'installazione nella cartella di destinazione scelta (solitamente sotto `app/release/app-release.apk`).

---

### Opzione B: Configurare la firma automatica in Gradle
Se vuoi che Gradle firmi l'applicazione automaticamente ogni volta che esegui `./gradlew assembleRelease`, puoi inserire le credenziali nel file Gradle.

1. Crea un file di Keystore (chiamato ad esempio `my-release-key.jks`) usando lo strumento `keytool` nel tuo terminale:
   ```bash
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
   ```
2. Sposta il file `my-release-key.jks` all'interno della cartella `app/` del tuo progetto.
3. Apri il file `app/build.gradle.kts` e inserisci il blocco `signingConfigs` prima del blocco `buildTypes`:
   ```kotlin
   android {
       ...
       signingConfigs {
           create("release") {
               storeFile = file("my-release-key.jks")
               storePassword = "LaTuaPasswordKeystore"
               keyAlias = "my-key-alias"
               keyPassword = "LaTuaPasswordDellaChiave"
           }
       }

       buildTypes {
           release {
               isMinifyEnabled = false
               signingConfig = signingConfigs.getByName("release") // Collega la firma alla release
               proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
           }
       }
   }
   ```
4. Ora esegui `./gradlew assembleRelease`: l'APK finale generato in `app/build/outputs/apk/release/app-release.apk` sarà già firmato e pronto all'uso!

---

### Opzione C: Firmare manualmente un APK esistente tramite Riga di Comando
Se hai già compilato l'APK non firmato (`app-release-unsigned.apk`), puoi firmarlo manualmente usando l'utility `apksigner` fornita dall'SDK Android (situata solitamente in `Android/Sdk/build-tools/<versione>/apksigner`):

1. Se non hai ancora una chiave, creala con il comando `keytool` (vedi sopra).
2. Allinea prima l'APK usando l'utility `zipalign` per ottimizzare l'uso della memoria (operazione consigliata prima della firma):
   ```bash
   zipalign -v -p 4 app-release-unsigned.apk app-release-aligned.apk
   ```
3. Firma l'APK con `apksigner`:
   ```bash
   apksigner sign --ks my-release-key.jks --out app-release-signed.apk app-release-aligned.apk
   ```
4. Puoi verificare che l'APK sia stato firmato correttamente eseguendo:
   ```bash
   apksigner verify app-release-signed.apk
   ```

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
> **Important Note on Signing:** If you have not configured a release signing key (keystore) in your `app/build.gradle.kts` file, Gradle will output an **unsigned** APK. Unsigned APKs are blocked from installation by Android's security systems.
> 
> Read the [How to Sign the APK](#how-to-sign-en) section below for detailed instructions on generating a key and signing the app.

---

<a name="how-to-sign-en"></a>
## 🔑 How to Sign the APK (Release)

To install a **Release** APK on a physical Android device, it must be digitally signed with a certificate (Keystore). Below are the 3 main ways to sign your application.

### Option A: Sign via Android Studio (Graphical Method - Recommended)
This is the easiest method if you are using Android Studio on your computer:
1. Open the project in **Android Studio**.
2. From the top menu, select **Build** > **Generate Signed Bundle / APK...**.
3. Select **APK** and click **Next**.
4. Under **Keystore path**:
   * If you already have a key, click **Choose existing...** and select it.
   * If you don't have a key, click **Create new...** to create a new one (fill out the required fields such as password, alias, and certificate details).
5. Enter your key credentials (Keystore password, Key alias, Key password) and click **Next**.
6. Select the **release** build variant, tick any recommended signing checkboxes, and click **Finish**.
7. You will find the signed APK ready for installation in your selected destination folder (usually under `app/release/app-release.apk`).

---

### Option B: Configure Automatic Signing in Gradle
If you want Gradle to automatically sign your app every time you run `./gradlew assembleRelease`, you can configure the signing credentials directly in your build script.

1. Generate a Keystore file (e.g., `my-release-key.jks`) using the `keytool` command in your terminal:
   ```bash
   keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
   ```
2. Move the `my-release-key.jks` file into the `app/` folder of your project.
3. Open `app/build.gradle.kts` and add the `signingConfigs` block before the `buildTypes` block:
   ```kotlin
   android {
       ...
       signingConfigs {
           create("release") {
               storeFile = file("my-release-key.jks")
               storePassword = "YourKeystorePassword"
               keyAlias = "my-key-alias"
               keyPassword = "YourKeyPassword"
           }
       }

       buildTypes {
           release {
               isMinifyEnabled = false
               signingConfig = signingConfigs.getByName("release") // Apply the release signature configuration
               proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
           }
       }
   }
   ```
4. Run `./gradlew assembleRelease`: the final APK generated at `app/build/outputs/apk/release/app-release.apk` will be fully signed and ready to use!

---

### Option C: Manually Sign an Existing APK via Command Line
If you have already built the unsigned release APK (`app-release-unsigned.apk`), you can manually sign it using the `apksigner` tool provided by the Android SDK (usually located at `Android/Sdk/build-tools/<version>/apksigner`):

1. If you don't have a key yet, generate one with `keytool` (see above).
2. Align the APK first using `zipalign` to optimize RAM usage (recommended before signing):
   ```bash
   zipalign -v -p 4 app-release-unsigned.apk app-release-aligned.apk
   ```
3. Sign the APK with `apksigner`:
   ```bash
   apksigner sign --ks my-release-key.jks --out app-release-signed.apk app-release-aligned.apk
   ```
4. Verify the signature by running:
   ```bash
   apksigner verify app-release-signed.apk
   ```

---

## How to Install the APK on Your Android Device
1. Transfer the compiled `.apk` file to your Android phone (via USB, Cloud storage like Google Drive, Email, or messaging apps like Telegram).
2. Open a File Manager app on your smartphone and tap the `.apk` file.
3. Allow installations from "Unknown Sources" if prompted by your system.
4. Follow the on-screen wizard to complete installation and launch **Arena AI**!
