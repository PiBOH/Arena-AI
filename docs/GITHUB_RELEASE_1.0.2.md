# 🚀 Arena AI — Release v1.0.2

Welcome to **Arena AI v1.0.2**! This release focuses on stability, fixing key build issues, and laying down the groundwork for seamless local builds and GitHub issue reporting.

---

## ✨ What's New

### 📱 Core Application & UI
* **LMSYS Chatbot Arena Client**: Seamlessly chat with various LLMs, compare side-by-side responses, and vote on the best answers.
* **Modern Material 3 Styling**: Fully adaptive design utilizing a sleek dark/light color scheme, consistent grid alignment, and fluid touch interactions (touch targets ≥ 48dp).
* **Robust WebView & Integration Handling**: Improved the core activity container to safely manage system activities and file uploads.
* **Authentication & Login Fix**: Resolved an issue where logging in (via Google Accounts, Hugging Face, GitHub, etc.) would freeze or hang. All OAuth sign-in and redirect flows are now kept fully contained within the secure native WebView, preserving login sessions and cookies.

### 🔧 Build System & Developer Tooling
* **Resolved Lint Obstacles**: Fixed the `InvalidFragmentVersionForActivityResult` compilation error on Release builds by upgrading `androidx.fragment:fragment-ktx` to `1.8.2` and adding graceful lint configurations.
* **Automatic Keystore Alignment**: Patched the `debugConfig` task so that Gradle automatically resolves local user keystores when a repository-level key isn't present, preventing build failures.
* **Detailed Documentation**: Created comprehensive APK building guidelines in both **Italian** 🇮🇹 and **English** 🇬🇧 inside `docs/APK-BUILD.md` featuring full release-signing instructions (`assembleRelease`).
* **Bilingual Issue Templates**: Added customizable `.github/ISSUE_TEMPLATE` bug reports and feature requests styled precisely with descriptive emoji sets.

---

## 📦 Assets & Installation

You can download the compiled APK file directly from this release:
* **`app-release.apk`**: The production-ready release variant (needs to be signed or installed with developer permission overrides).
* **`app-debug.apk`**: Pre-signed debug variant, ready to install immediately for quick testing on any Android device.

### 📥 Manual Installation Steps:
1. Download the `.apk` file to your Android smartphone.
2. Tap the file in your preferred File Manager.
3. Allow installations from "Unknown Sources" if prompted by your system.
4. Launch **Arena AI** and start comparing chatbot models!

---

## 👥 Contributors & Feedback
* Developed with ❤️ by [PiBOH](https://github.com/PiBOH).
* Encountered a bug or have a feature suggestion? Open a structured ticket using our brand-new issue templates in the **Issues** tab!
