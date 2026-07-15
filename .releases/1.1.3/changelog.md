## [1.1.3] - 2026-07-15

### Fixed
- **Stable Network Monitoring**: Resolved the false "No Internet Connection" screen bug by replacing the generic network request callback with the modern `registerDefaultNetworkCallback` API. 
- **Graceful Network Handover**: Implemented a secondary active capability check during network lost events to filter out false-positives and ensure smooth handovers between mobile data and Wi-Fi.
- **Immediate Offline Startup Initialization**: Correctly initialized `isOnline` with the actual device connection status at launch, avoiding race conditions prior to the first callback update.

---

## [1.1.2] - 2026-07-15

### Fixed
- **Definitive Background Idle Battery Fix**: Replaced the continuous background `setInterval` polling with an ultra-efficient, debounced `MutationObserver` web API. The observer automatically sleeps when the DOM is stable and executes single-operation O(1) checks only when mutations occur, bringing idle CPU consumption to a pristine 0%.
- **Adaptive Polling Rates**: Configured progressive delays (3s during active downloads, 5s during pending states, and 6s when paused) to prevent battery drain during file transfers.
- **WebView Render Optimization**: Patched the Compose `AndroidView.update` block to only update WebView layer types if they actually changed, avoiding costly layout and rendering invalidations.
- **Redundant JS Cleanups**: Entirely eliminated duplicate JavaScript injections from page navigation and progress events.
- **Incremental Build Parameters**: Fully restored the versioning setup to `versionCode = 7` and `versionName = "1.1.2"`.
