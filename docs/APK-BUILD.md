# Guida alla Generazione e al Download dell'APK

Questa guida descrive come ottenere e compilare l'APK (Android Package) per l'applicazione **Arena AI (LMSYS Chatbot Arena)** in due modi diversi:
1. **Tramite l'interfaccia di Google AI Studio** (il metodo più semplice e rapido, senza installare nulla).
2. **Localmente sul tuo computer** (per sviluppatori, utilizzando Android Studio e Gradle).

---

## Metodo 1: Generazione dell'APK direttamente in Google AI Studio (Consigliato)

Google AI Studio include una funzionalità integrata per compilare l'applicazione nel cloud e scaricare direttamente il file APK pronto per l'installazione sul tuo dispositivo Android.

### Passaggi:
1. Apri il progetto in **Google AI Studio**.
2. Fai clic sul menu delle **Impostazioni** (icona a forma di ingranaggio o menu di opzioni, solitamente nell'angolo in alto a destra o nella barra laterale).
3. Trova e seleziona l'opzione **Generate APK/AAB** (oppure "Genera APK/AAB").
4. Attendi che il sistema completi la compilazione nel cloud.
5. Al termine, apparirà un link per scaricare direttamente il file `.apk` compilato sul tuo computer o dispositivo.

---

## Metodo 2: Compilazione locale sul tuo computer

Se preferisci compilare il codice sorgente in autonomia sul tuo PC, puoi esportare il progetto ed eseguire la compilazione locale tramite Gradle.

### Prerequisiti:
* **Java Development Kit (JDK):** Assicurati di avere installato Java 17 o versione successiva.
* **Android Studio:** Consigliato per installare automaticamente l'SDK Android e per gestire l'ambiente di sviluppo (puoi scaricarlo gratuitamente da [developer.android.com](https://developer.android.com/studio)).

### Passaggi:

1. **Esporta il progetto da AI Studio:**
   * Fai clic sul menu delle Impostazioni in AI Studio.
   * Seleziona l'opzione per **esportare il progetto come file ZIP** (o "Export project as ZIP").
   * Scarica ed estrai l'archivio ZIP in una cartella sul tuo computer.

2. **Apri il progetto:**
   * Avvia Android Studio.
   * Seleziona **Open** (Apri) e naviga fino alla cartella in cui hai estratto il progetto ZIP.
   * Attendi che Android Studio sincronizzi il progetto con i file Gradle (l'operazione potrebbe richiedere qualche minuto alla prima esecuzione).

3. **Compila l'APK tramite il Terminale:**
   * Apri la scheda **Terminal** integrata in Android Studio (in basso a sinistra) o usa il terminale del tuo sistema operativo posizionandoti nella cartella principale del progetto.
   * Esegui uno dei seguenti comandi a seconda del tuo sistema operativo:

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

4. **Trova il file APK generato:**
   * Una volta che la compilazione termina con successo (`BUILD SUCCESSFUL`), troverai l'APK pronto al seguente percorso a partire dalla cartella radice del progetto:
     ```
     app/build/outputs/apk/debug/app-debug.apk
     ```

---

## Come Installare l'APK sul tuo Smartphone Android

1. Copia o trasferisci il file `.apk` sul tuo telefono Android (tramite cavo USB, Google Drive, email o Telegram).
2. Sul telefono, apri un file manager e tocca il file APK.
3. Se richiesto, autorizza l'installazione da "Sorgenti Sconosciute" (Unknown Sources) per il browser o per il file manager utilizzato.
4. Segui le istruzioni a schermo per completare l'installazione e avviare **Arena AI**!
