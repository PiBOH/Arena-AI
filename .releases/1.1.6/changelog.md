## [1.1.6] - 2026-07-15

### Fixed
- **Mobile Chat Menu Options (Three-Dots) Fix**: Patched mobile browser limitations by injecting custom touch-optimization stylesheets and event handlers.
  - *No-Hover Workaround*: Removed desktop-only `:hover` visibility dependencies inside chat list sidebars, forcing options/three-dots buttons to be permanently visible and accessible on touchscreen devices.
  - *Tap Propagation Shielding*: Added capturing-phase event listeners to stop `click`, `touchstart`, `touchend`, and `mousedown` event propagation on the three-dots buttons. This ensures that tapping chat options triggers the corresponding action dropdown menu instead of bubbling up and activating the parent row container (which selected/switched chats and dismissed menus).
  - *Generous Touch Target Scaling*: Scaled three-dots options tap regions to a highly precise `40dp` minimum boundary to guarantee seamless, error-free mobile selection.
## [1.1.5] - 2026-07-15

### Fixed
- **Chromium Simple File Enumerator Fix**: Implemented startup sanitation of the WebView `Code Cache` folder, clearing potentially corrupted or stale V8 JavaScript compile cache bytecode files. This prevents Chromium's async background file enumerator thread from throwing metadata lookup error logs, ensuring pristine runtime execution.

---

## [1.1.4] - 2026-07-15

### Fixed
- **Soft Keyboard Covering Input Fields Fix**: Configured the standard Android `adjustResize` window soft input mode within `AndroidManifest.xml` and integrated Compose `.imePadding()` dynamically on the main UI content container. This resizes the WebView when the soft keyboard is shown, ensuring focused chat inputs are automatically scrolled above the keyboard, preventing overlay/obscuration.
- **Incremental Build Parameters**: Incremented the build parameters to `versionCode = 9` and `versionName = "1.1.4"`.
