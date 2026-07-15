package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import android.app.DownloadManager
import android.webkit.URLUtil
import android.os.Environment
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import android.content.ContextWrapper
import android.app.Activity
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private var webView: WebView? = null
    private val isOnline = mutableStateOf(true)
    private var backPressedTime = 0L

    // Default target website
    private val targetUrl = "https://arena.ai/"

    // Callback for WebView file picker
    var filePathCallback: android.webkit.ValueCallback<Array<Uri>>? = null

    val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNullOrEmpty()) {
            filePathCallback?.onReceiveValue(null)
        } else {
            filePathCallback?.onReceiveValue(uris.toTypedArray())
        }
        filePathCallback = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Pre-create and clear WebView Code Cache directories to resolve Chromium simple_file_enumerator errors
        try {
            val codeCacheDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            if (codeCacheDir.exists()) {
                codeCacheDir.deleteRecursively()
            }
            val wasmCacheDir = java.io.File(codeCacheDir, "wasm")
            wasmCacheDir.mkdirs()
            val jsCacheDir = java.io.File(codeCacheDir, "js")
            jsCacheDir.mkdirs()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Initialize connection state correctly on startup
        isOnline.value = isHasNetworkConnection(this)

        // Monitor Network Connectivity
        registerNetworkCallback()

        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                var canGoBack by remember { mutableStateOf(false) }
                var canGoForward by remember { mutableStateOf(false) }
                var isLoading by remember { mutableStateOf(true) }
                var loadingProgress by remember { mutableFloatStateOf(0f) }
                var showSettings by remember { mutableStateOf(false) }
                var isHardwareAccelerated by remember { mutableStateOf(true) }
                
                // Track current webview page url
                var currentUrl by remember { mutableStateOf(targetUrl) }

                // Check and handle deep linking URLs from notifications on app launch
                val initialUrl = intent?.getStringExtra(MyFirebaseMessagingService.EXTRA_URL) ?: targetUrl

                // Warn user if notification permission is not granted on app startup
                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val isGranted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!isGranted) {
                            Toast.makeText(
                                this@MainActivity,
                                "Warning: Notification permission is not granted! You won't be able to monitor download progress.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                // Back Button Interceptor
                BackHandler {
                    val web = webView
                    if (web != null && web.canGoBack()) {
                        web.goBack()
                    } else {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - backPressedTime < 2000) {
                            finish()
                        } else {
                            backPressedTime = currentTime
                            Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding()
                            .background(Color(0xFF0F172A)) // Slate 900
                    ) {
                        // Check Online/Offline state
                        if (isOnline.value) {
                            // WebView Container with Nested SwipeRefreshLayout
                            WebViewContainer(
                                url = initialUrl,
                                isHardwareAccelerated = isHardwareAccelerated,
                                onWebViewCreated = { web ->
                                    webView = web
                                },
                                onProgressChanged = { progress ->
                                    loadingProgress = progress / 100f
                                    isLoading = progress < 100
                                },
                                onPageStateChanged = { backState, forwardState, currentActiveUrl ->
                                    canGoBack = backState
                                    canGoForward = forwardState
                                    currentUrl = currentActiveUrl
                                },
                                onPageLoadStarted = {
                                    isLoading = true
                                },
                                onReceivedError = {
                                    // Handle any deep connection errors (like domain unreachable)
                                    isOnline.value = isHasNetworkConnection(this@MainActivity)
                                },
                                onSettingsClick = { showSettings = true }
                            )
                        } else {
                            // Offline fallback screen
                            OfflineScreen(
                                onRetryClick = {
                                    isOnline.value = isHasNetworkConnection(this@MainActivity)
                                    webView?.reload()
                                }
                            )
                        }

                        // Native Page Load Progress Indicator at top
                        AnimatedVisibility(
                            visible = isLoading && isOnline.value,
                            enter = fadeIn(),
                            exit = fadeOut(),
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            LinearProgressIndicator(
                                progress = { loadingProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp),
                                color = Color(0xFF38BDF8), // Celestial Sky Blue
                                trackColor = Color(0xFF1E293B) // Slate 800
                            )
                        }
                    }

                    // Settings Bottom Sheet
                    if (showSettings) {
                        SettingsBottomSheet(
                            isHardwareAccelerated = isHardwareAccelerated,
                            onHardwareAcceleratedChange = { enabled ->
                                isHardwareAccelerated = enabled
                                webView?.let { web ->
                                    web.setLayerType(
                                        if (enabled) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_SOFTWARE,
                                        null
                                    )
                                }
                            },
                            onClearCacheClick = {
                                webView?.let { web ->
                                    web.clearCache(true)
                                    web.clearHistory()
                                    val cookieManager = CookieManager.getInstance()
                                    cookieManager.removeAllCookies(null)
                                    cookieManager.flush()
                                    Toast.makeText(this@MainActivity, "Cache and cookies cleared!", Toast.LENGTH_SHORT).show()
                                    web.loadUrl(targetUrl)
                                }
                            },
                            currentUrl = currentUrl,
                            onDismiss = { showSettings = false }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = intent.getStringExtra(MyFirebaseMessagingService.EXTRA_URL)
        if (url != null) {
            webView?.loadUrl(url)
        }
    }

    private fun registerNetworkCallback() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        try {
            connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread { isOnline.value = true }
                }

                override fun onLost(network: Network) {
                    // Re-verify network status to prevent false-positives during network handover
                    val hasConn = isHasNetworkConnection(this@MainActivity)
                    runOnUiThread { isOnline.value = hasConn }
                }
            })
        } catch (e: Exception) {
            val builder = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            connectivityManager.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    runOnUiThread { isOnline.value = true }
                }

                override fun onLost(network: Network) {
                    val hasConn = isHasNetworkConnection(this@MainActivity)
                    runOnUiThread { isOnline.value = hasConn }
                }
            })
        }
    }

    private fun isHasNetworkConnection(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}

class WebAppInterface(
    private val webView: WebView,
    private val onSettingsClick: () -> Unit
) {
    @android.webkit.JavascriptInterface
    fun goBack() {
        webView.post {
            if (webView.canGoBack()) {
                webView.goBack()
            }
        }
    }

    @android.webkit.JavascriptInterface
    fun goForward() {
        webView.post {
            if (webView.canGoForward()) {
                webView.goForward()
            }
        }
    }

    @android.webkit.JavascriptInterface
    fun reload() {
        webView.post {
            webView.reload()
        }
    }

    @android.webkit.JavascriptInterface
    fun goHome() {
        webView.post {
            webView.loadUrl("https://arena.ai/")
        }
    }

    @android.webkit.JavascriptInterface
    fun openSettings() {
        webView.post {
            onSettingsClick()
        }
    }
}

private fun trackDownloadProgress(context: Context, dm: DownloadManager, downloadId: Long, fileName: String) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        var lastDownloadedBytes = 0L
        var lastQueryTime = System.currentTimeMillis()
        
        // Battery optimization: Keep track of previous state to avoid redundant notification redraws
        var lastProgress = -1
        var lastContentText = ""

        CoroutineScope(Dispatchers.IO).launch {
            var downloading = true
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create a low priority channel for active updates so it doesn't beep every second
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "downloads_channel",
                    "Downloads",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Download progress and completion notifications"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val notificationId = (downloadId % Int.MAX_VALUE).toInt()
            var currentDelay = 3000L // Start with 3 seconds to reduce polling frequency

            while (downloading) {
                delay(currentDelay)
                val cursor = dm.query(query)
                if (cursor != null && cursor.moveToFirst()) {
                    val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val bytesDownloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                    val bytesTotalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                    
                    if (statusIndex != -1 && bytesDownloadedIndex != -1 && bytesTotalIndex != -1) {
                        val status = cursor.getInt(statusIndex)
                        val bytesDownloaded = cursor.getLong(bytesDownloadedIndex)
                        val bytesTotal = cursor.getLong(bytesTotalIndex)
                        
                        val currentTime = System.currentTimeMillis()
                        val timeDeltaMs = currentTime - lastQueryTime
                        
                        var speedText = ""
                        var timeLeftText = ""
                        
                        if (timeDeltaMs > 0) {
                            val bytesDelta = bytesDownloaded - lastDownloadedBytes
                            // speed in bytes/sec
                            val speed = (bytesDelta * 1000.0) / timeDeltaMs
                            speedText = formatSpeed(speed)
                            
                            if (speed > 0 && bytesTotal > 0) {
                                val remainingBytes = bytesTotal - bytesDownloaded
                                val remainingSeconds = (remainingBytes / speed).toLong()
                                timeLeftText = formatTimeRemaining(remainingSeconds)
                            }
                        }
                        
                        lastDownloadedBytes = bytesDownloaded
                        lastQueryTime = currentTime

                        when (status) {
                            DownloadManager.STATUS_RUNNING -> {
                                currentDelay = 3000L // Active download: poll every 3 seconds to preserve battery
                                val progress = if (bytesTotal > 0) {
                                    (bytesDownloaded * 100 / bytesTotal).toInt()
                                } else {
                                    0
                                }
                                
                                val contentText = buildString {
                                    if (bytesTotal > 0) {
                                        append("$progress% (${formatSize(bytesDownloaded)} / ${formatSize(bytesTotal)})")
                                    } else {
                                        append("Downloaded: ${formatSize(bytesDownloaded)}")
                                    }
                                    if (speedText.isNotEmpty()) append(" • $speedText")
                                    if (timeLeftText.isNotEmpty()) append(" • $timeLeftText remaining")
                                }

                                // Battery optimization: only send update to the system if something actually changed
                                if (progress != lastProgress || contentText != lastContentText) {
                                    lastProgress = progress
                                    lastContentText = contentText

                                    val builder = NotificationCompat.Builder(context, "downloads_channel")
                                        .setSmallIcon(android.R.drawable.stat_sys_download)
                                        .setContentTitle(fileName)
                                        .setContentText(contentText)
                                        .setPriority(NotificationCompat.PRIORITY_LOW)
                                        .setOnlyAlertOnce(true)
                                        .setProgress(100, progress, bytesTotal <= 0)
                                        .setOngoing(true)
                                    
                                    notificationManager.notify(notificationId, builder.build())
                                }
                            }
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                downloading = false
                                val builder = NotificationCompat.Builder(context, "downloads_channel")
                                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                                    .setContentTitle("Download completed")
                                    .setContentText(fileName)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setProgress(0, 0, false)
                                    .setOngoing(false)
                                    .setAutoCancel(true)
                                notificationManager.notify(notificationId, builder.build())
                                
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Download completed: $fileName", Toast.LENGTH_LONG).show()
                                }
                            }
                            DownloadManager.STATUS_FAILED -> {
                                downloading = false
                                val builder = NotificationCompat.Builder(context, "downloads_channel")
                                    .setSmallIcon(android.R.drawable.stat_notify_error)
                                    .setContentTitle("Download failed")
                                    .setContentText(fileName)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setProgress(0, 0, false)
                                    .setOngoing(false)
                                    .setAutoCancel(true)
                                notificationManager.notify(notificationId, builder.build())
                                
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Download failed: $fileName", Toast.LENGTH_LONG).show()
                                }
                            }
                            DownloadManager.STATUS_PAUSED -> {
                                currentDelay = 6000L // Paused: poll every 6 seconds to avoid useless wakeups
                                val contentText = "Paused"
                                if (lastContentText != contentText) {
                                    lastContentText = contentText
                                    val builder = NotificationCompat.Builder(context, "downloads_channel")
                                        .setSmallIcon(android.R.drawable.stat_sys_download)
                                        .setContentTitle("Download paused")
                                        .setContentText(fileName)
                                        .setPriority(NotificationCompat.PRIORITY_LOW)
                                        .setOngoing(true)
                                    notificationManager.notify(notificationId, builder.build())
                                }
                            }
                            else -> {
                                currentDelay = 5000L // Other states (pending, etc.): poll every 5 seconds
                            }
                        }
                    }
                } else {
                    downloading = false
                }
                cursor?.close()
            }
        }
    }

    private fun formatSpeed(speedBytesPerSec: Double): String {
        return when {
            speedBytesPerSec >= 1024 * 1024 -> String.format("%.1f MB/s", speedBytesPerSec / (1024 * 1024))
            speedBytesPerSec >= 1024 -> String.format("%.1f KB/s", speedBytesPerSec / 1024)
            else -> String.format("%.0f B/s", speedBytesPerSec)
        }
    }

    private fun formatTimeRemaining(seconds: Long): String {
        if (seconds <= 0) return "0s"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return when {
            h > 0 -> String.format("%dh %dm", h, m)
            m > 0 -> String.format("%dm %ds", m, s)
            else -> String.format("%ds", s)
        }
    }

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.2f MB", mb)
        kb >= 1.0 -> String.format("%.2f KB", kb)
        else -> "$bytes B"
    }
}

private fun injectCustomMenuJavascript(webView: WebView?) {
    webView?.evaluateJavascript(
        """
        (function() {
            // 1. Inject custom styles to make sure three-dots options/menu buttons are visible and tappable on mobile
            try {
                const styleId = 'android-three-dots-patch';
                if (!document.getElementById(styleId)) {
                    const style = document.createElement('style');
                    style.id = styleId;
                    style.innerHTML = `
                        /* Remove hover restrictions on chat list menus/three dots inside Gradio or sidebars */
                        div[class*="history"] button,
                        div[class*="sidebar"] button,
                        [class*="history-item"] button,
                        [class*="sidebar-item"] button,
                        .chat-menu-button,
                        .options-button,
                        .menu-button,
                        .dots-button,
                        [class*="menu-btn"],
                        [class*="options-btn"],
                        [class*="dots-btn"],
                        button[aria-label*="menu" i],
                        button[aria-label*="option" i],
                        button[aria-label*="more" i],
                        button[id*="menu" i],
                        /* Support SVG-only options buttons */
                        button:has(svg):hover,
                        button:has(svg) {
                            /* Ensure button is visible without hover on touch screens */
                            opacity: 1 !important;
                            visibility: visible !important;
                            display: inline-flex !important;
                            align-items: center !important;
                            justify-content: center !important;
                            /* Make easily clickable */
                            min-width: 40px !important;
                            min-height: 40px !important;
                            z-index: 1000 !important;
                            pointer-events: auto !important;
                        }

                        /* Ensure containers do not clip popup menus or the three-dots buttons */
                        div[class*="history-item"],
                        div[class*="sidebar-item"],
                        [class*="chat-row"],
                        [class*="chat-item"] {
                            overflow: visible !important;
                            position: relative !important;
                        }
                    `;
                    document.head.appendChild(style);
                }
            } catch (styleErr) {
                console.error("Android Stylesheet Patch Error: ", styleErr);
            }

            // 2. Stop event propagation on three-dots buttons so clicking them doesn't select/switch the chat row
            function optimizeThreeDots() {
                try {
                    const selectors = 'button, [role="button"], .menu-btn, .options-btn, [class*="menu-button"], [class*="options-button"], [class*="dots-button"]';
                    const buttons = document.querySelectorAll(selectors);
                    buttons.forEach(btn => {
                        const html = btn.innerHTML || '';
                        const ariaLabel = (btn.getAttribute('aria-label') || '').toLowerCase();
                        const className = (btn.className || '').toLowerCase();
                        
                        const isThreeDots = html.includes('svg') || 
                                            html.includes('⋮') || 
                                            html.includes('...') || 
                                            className.includes('menu') || 
                                            className.includes('option') || 
                                            className.includes('dots') ||
                                            ariaLabel.includes('menu') || 
                                            ariaLabel.includes('option') || 
                                            ariaLabel.includes('more');
                                            
                        if (isThreeDots) {
                            // Boost tap reliability
                            btn.style.opacity = '1';
                            btn.style.visibility = 'visible';
                            btn.style.pointerEvents = 'auto';
                            
                            if (!btn.dataset.threeDotsOptimized) {
                                const stopEvent = (e) => {
                                    // Prevent event bubbling to parent chat row (which selects/resets the active conversation)
                                    e.stopPropagation();
                                };
                                ['click', 'touchstart', 'touchend', 'mousedown', 'mouseup'].forEach(evt => {
                                    btn.addEventListener(evt, stopEvent, { capture: true });
                                });
                                btn.dataset.threeDotsOptimized = 'true';
                            }
                        }
                    });
                } catch (e) {
                    console.error("Three Dots Optimization Error: ", e);
                }
            }

            function injectCustomTabs() {
                try {
                    if (document.getElementById('android-app-options-wrapper')) {
                        return;
                    }
                    // Define targets to locate the menu/sidebar area
                    const targetTexts = ['new chat', 'leaderboard', 'search', 'arena (battle)'];
                    let refBtn = null;
                    
                    // Search for any button or link containing these target texts
                    const allButtons = Array.from(document.querySelectorAll('button, a, .menu-item, [role="button"]'));
                    for (const btn of allButtons) {
                        const txt = (btn.textContent || '').trim().toLowerCase();
                        if (targetTexts.some(t => txt === t || (txt.length < 30 && txt.includes(t)))) {
                            refBtn = btn;
                            break; // Stop at the first match to prevent duplicates
                        }
                    }
                    
                    // Fallback to find any visible button in sidebar if no exact match is found
                    if (!refBtn) {
                        const sidebar = document.querySelector('.sidebar, [role="tablist"], .tab-nav');
                        if (sidebar) {
                            refBtn = sidebar.querySelector('button, a');
                        }
                    }
                    
                    if (refBtn && refBtn.parentElement) {
                        const parent = refBtn.parentElement;
                        
                        // Check if the wrapper is already injected inside this active parent
                        if (parent.querySelector('#android-app-options-wrapper')) {
                            return;
                        }
                        
                        // Clean up any stale/duplicated wrappers in the entire document before re-injecting
                        const allWrappers = document.querySelectorAll('#android-app-options-wrapper');
                        allWrappers.forEach(w => w.remove());
                        
                        // Create a single container wrapper
                        const wrapper = document.createElement('div');
                        wrapper.id = 'android-app-options-wrapper';
                        wrapper.style.width = '100%';
                        wrapper.style.display = 'flex';
                        wrapper.style.flexDirection = 'column';
                        
                        // Create Toggle Button
                        const toggleBtn = document.createElement('button');
                        toggleBtn.id = 'android-app-options-btn';
                        toggleBtn.type = 'button';
                        
                        if (refBtn.className) {
                            toggleBtn.className = refBtn.className;
                            toggleBtn.classList.remove('selected', 'active');
                        } else {
                            toggleBtn.style.padding = '8px 12px';
                            toggleBtn.style.margin = '4px 0';
                            toggleBtn.style.border = '1px solid rgba(56, 189, 248, 0.3)';
                            toggleBtn.style.borderRadius = '8px';
                            toggleBtn.style.background = 'transparent';
                            toggleBtn.style.color = '#38BDF8';
                            toggleBtn.style.fontSize = '14px';
                            toggleBtn.style.cursor = 'pointer';
                        }
                        
                        // Uniform styling
                        toggleBtn.style.display = 'flex';
                        toggleBtn.style.alignItems = 'center';
                        toggleBtn.style.justifyContent = 'space-between';
                        toggleBtn.style.width = '100%';
                        toggleBtn.style.boxSizing = 'border-box';
                        toggleBtn.style.color = '#38BDF8';
                        toggleBtn.style.fontWeight = 'bold';
                        toggleBtn.style.marginTop = '8px';
                        toggleBtn.style.marginBottom = '8px';
                        
                        toggleBtn.innerHTML = '<span>📱 App Options</span><span class="app-arrow" style="transition: transform 0.2s; margin-left: 8px;">▼</span>';
                        
                        // Create Collapsible Submenu
                        const submenu = document.createElement('div');
                        submenu.id = 'android-app-submenu';
                        submenu.style.display = 'none';
                        submenu.style.flexDirection = 'column';
                        submenu.style.gap = '8px';
                        submenu.style.padding = '10px';
                        submenu.style.marginTop = '4px';
                        submenu.style.marginBottom = '8px';
                        submenu.style.width = '100%';
                        submenu.style.boxSizing = 'border-box';
                        submenu.style.backgroundColor = 'rgba(15, 23, 42, 0.95)';
                        submenu.style.border = '1px solid rgba(56, 189, 248, 0.3)';
                        submenu.style.borderRadius = '8px';
                        
                        // Row 1: Back, Forward, Reload
                        const row1 = document.createElement('div');
                        row1.style.display = 'flex';
                        row1.style.flexDirection = 'row';
                        row1.style.gap = '6px';
                        row1.style.width = '100%';
                        
                        const createSubBtn = (label, actionName) => {
                            const btn = document.createElement('button');
                            btn.type = 'button';
                            btn.style.flex = '1';
                            btn.style.padding = '8px 4px';
                            btn.style.border = '1px solid rgba(56, 189, 248, 0.2)';
                            btn.style.borderRadius = '6px';
                            btn.style.background = '#1E293B';
                            btn.style.color = '#38BDF8';
                            btn.style.fontSize = '12px';
                            btn.style.fontWeight = 'bold';
                            btn.style.cursor = 'pointer';
                            btn.style.transition = 'all 0.2s';
                            
                            btn.onmouseover = () => {
                                btn.style.background = '#38BDF8';
                                btn.style.color = '#0F172A';
                            };
                            btn.onmouseout = () => {
                                btn.style.background = '#1E293B';
                                btn.style.color = '#38BDF8';
                            };
                            btn.onclick = function(e) {
                                e.preventDefault();
                                e.stopPropagation();
                                if (window.AndroidApp && window.AndroidApp[actionName]) {
                                    window.AndroidApp[actionName]();
                                }
                            };
                            btn.innerHTML = label;
                            return btn;
                        };
                        
                        row1.appendChild(createSubBtn('⬅️ Back', 'goBack'));
                        row1.appendChild(createSubBtn('➡️ Forward', 'goForward'));
                        row1.appendChild(createSubBtn('🔄 Reload', 'reload'));
                        
                        // Row 2: Settings
                        const row2 = document.createElement('div');
                        row2.style.display = 'flex';
                        row2.style.width = '100%';
                        
                        const settingsBtn = createSubBtn('⚙️ Settings', 'openSettings');
                        settingsBtn.style.width = '100%';
                        row2.appendChild(settingsBtn);
                        
                        submenu.appendChild(row1);
                        submenu.appendChild(row2);
                        
                        wrapper.appendChild(toggleBtn);
                        wrapper.appendChild(submenu);
                        
                        // Toggle Logic
                        toggleBtn.onclick = function(e) {
                            e.preventDefault();
                            e.stopPropagation();
                            const isHidden = submenu.style.display === 'none';
                            submenu.style.display = isHidden ? 'flex' : 'none';
                            
                            const arrow = toggleBtn.querySelector('.app-arrow');
                            if (arrow) {
                                arrow.style.transform = isHidden ? 'rotate(180deg)' : 'rotate(0deg)';
                            }
                        };
                        
                        // Insert into DOM right after the found reference element
                        if (refBtn.nextSibling) {
                            parent.insertBefore(wrapper, refBtn.nextSibling);
                        } else {
                            parent.appendChild(wrapper);
                        }
                    }
                } catch (err) {
                    console.error("Android Custom Tabs Injection Error: ", err);
                }
            }

            // Initial execution
            injectCustomTabs();
            optimizeThreeDots();

            // MutationObserver to track active changes & preserve optimization states on loaded content
            let throttleTimeout = null;
            const observer = new MutationObserver(function(mutations) {
                // Always optimize three-dots even if custom tab menu is already present
                optimizeThreeDots();
                
                if (throttleTimeout) return;
                
                throttleTimeout = setTimeout(function() {
                    throttleTimeout = null;
                    if (!document.getElementById('android-app-options-wrapper')) {
                        injectCustomTabs();
                    }
                }, 2000); // 2s response latency for heavy dynamic layouts
            });

            observer.observe(document.body, {
                childList: true,
                subtree: true
            });
            
            window.androidMenuObserver = observer;
        })();
        """.trimIndent(),
        null
    )
}

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
fun WebViewContainer(
    url: String,
    isHardwareAccelerated: Boolean,
    onWebViewCreated: (WebView) -> Unit,
    onProgressChanged: (Int) -> Unit,
    onPageStateChanged: (canGoBack: Boolean, canGoForward: Boolean, url: String) -> Unit,
    onPageLoadStarted: () -> Unit,
    onReceivedError: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            val swipeRefreshLayout = SwipeRefreshLayout(ctx).apply {
                setColorSchemeColors(android.graphics.Color.parseColor("#38BDF8"))
                setProgressBackgroundColorSchemeColor(android.graphics.Color.parseColor("#1E293B"))
                isEnabled = false // Disable drag-to-refresh gesture to prevent losing active chat sessions on accidental swipes
            }

            val webView = WebView(ctx).apply {
                overScrollMode = View.OVER_SCROLL_NEVER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                isClickable = true
                isFocusable = true
                isFocusableInTouchMode = true

                addJavascriptInterface(WebAppInterface(this, onSettingsClick), "AndroidApp")

                // Optimize settings for extreme speed and older device compatibility
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    allowFileAccess = true
                    allowContentAccess = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    cacheMode = WebSettings.LOAD_DEFAULT
                    javaScriptCanOpenWindowsAutomatically = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }
                    
                    // Mask modern mobile Chrome user-agent for optimal grid/CSS layout parsing on Arena.ai
                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                }

                // Sync Cookie states
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                cookieManager.setAcceptThirdPartyCookies(this, true)

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onPageLoadStarted()
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        swipeRefreshLayout.isRefreshing = false
                        onPageStateChanged(canGoBack(), canGoForward(), url ?: "")
                        injectCustomMenuJavascript(view)
                        
                        // Explicitly flush cookies to disk to preserve authentication state immediately after password entry
                        try {
                            CookieManager.getInstance().flush()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        request: WebResourceRequest?,
                        error: WebResourceError?
                    ) {
                        super.onReceivedError(view, request, error)
                        swipeRefreshLayout.isRefreshing = false
                        if (request?.isForMainFrame == true) {
                            onReceivedError()
                        }
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val targetUrl = request?.url?.toString() ?: return false
                        val lowerUrl = targetUrl.lowercase()
                        
                        // Keep chatbot arena and lmsys ecosystem fully contained inside the native app
                        val isArenaOrLmsys = lowerUrl.contains("arena.ai") || 
                                              lowerUrl.contains("lmarena.ai") ||
                                              lowerUrl.contains("lmsys.org")
                        
                        // Support all common authentication domains and OAuth callbacks
                        val isAuthFlow = lowerUrl.contains("google.com") ||
                                         lowerUrl.contains("google.it") ||
                                         lowerUrl.contains("huggingface.co") ||
                                         lowerUrl.contains("github.com") ||
                                         lowerUrl.contains("apple.com") ||
                                         lowerUrl.contains("oauth") ||
                                         lowerUrl.contains("login") ||
                                         lowerUrl.contains("signin") ||
                                         lowerUrl.contains("signup") ||
                                         lowerUrl.contains("auth") ||
                                         lowerUrl.contains("checkpoint") ||
                                         lowerUrl.contains("recaptcha") ||
                                         lowerUrl.contains("gstatic")

                        if (isArenaOrLmsys || isAuthFlow) {
                            return false
                        }
                        
                        // Handle non-http protocols (like mailto, tel, whatsapp, intent) safely without crashes
                        if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                            try {
                                val intent = Intent.parseUri(targetUrl, Intent.URI_INTENT_SCHEME).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                val currentContext = view?.context ?: context
                                currentContext.startActivity(intent)
                                return true
                            } catch (e: Exception) {
                                // Try parsing browser fallback URL if available
                                try {
                                    val intent = Intent.parseUri(targetUrl, Intent.URI_INTENT_SCHEME)
                                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                                    if (fallbackUrl != null) {
                                        view?.loadUrl(fallbackUrl)
                                        return true
                                    }
                                } catch (ex: Exception) {
                                    // Ignore
                                }
                            }
                            return true // Prevent WebView from trying to load custom protocols and crashing
                        }
                        
                        // Redirect other external domains out to system browser (safeguard native feel)
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            val currentContext = view?.context ?: context
                            currentContext.startActivity(intent)
                            return true
                        } catch (e: Exception) {
                            return false
                        }
                    }
                }

                setDownloadListener { downloadUrl, userAgentString, contentDisposition, mimetype, contentLength ->
                    val fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
                    
                    // Warn user if notification permission is not granted so they know they won't see progress notifications
                    val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            ctx,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else {
                        true
                    }
                    if (!hasNotif) {
                        Toast.makeText(
                            ctx,
                            "Warning: Notification permission is not granted! You won't be able to monitor this download's progress.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    try {
                        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                            setMimeType(mimetype)
                            val cookies = CookieManager.getInstance().getCookie(downloadUrl)
                            addRequestHeader("cookie", cookies)
                            addRequestHeader("User-Agent", userAgentString)
                            setDescription("Downloading: $fileName")
                            setTitle(fileName)
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                            setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                fileName
                            )
                        }
                        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val downloadId = dm.enqueue(request)
                        Toast.makeText(ctx, "Download started: $fileName", Toast.LENGTH_SHORT).show()
                        trackDownloadProgress(ctx, dm, downloadId, fileName)
                    } catch (e: Exception) {
                        // Fallback: Open in external browser
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            ctx.startActivity(intent)
                        } catch (ex: Exception) {
                            Toast.makeText(ctx, "Unable to start download", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        onProgressChanged(newProgress)
                    }

                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallback: android.webkit.ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        val activity = context as? MainActivity ?: return false
                        activity.filePathCallback?.onReceiveValue(null)
                        activity.filePathCallback = filePathCallback
                        try {
                            activity.fileChooserLauncher.launch("*/*")
                        } catch (e: Exception) {
                            activity.filePathCallback?.onReceiveValue(null)
                            activity.filePathCallback = null
                            return false
                        }
                        return true
                    }
                }

                loadUrl(url)
            }

            swipeRefreshLayout.addView(webView)
            
            swipeRefreshLayout.setOnRefreshListener {
                webView.reload()
            }

            // CRITICAL: Prevent SwipeRefreshLayout from intercepting touches and click gestures when web view is scrolled down
            swipeRefreshLayout.setOnChildScrollUpCallback { _, _ ->
                webView.canScrollVertically(-1)
            }

            onWebViewCreated(webView)

            swipeRefreshLayout
        },
        modifier = Modifier.fillMaxSize(),
        update = { swipeRefreshLayout ->
            val web = swipeRefreshLayout.getChildAt(0) as? WebView
            web?.let { w ->
                val targetLayerType = if (isHardwareAccelerated) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_SOFTWARE
                if (w.layerType != targetLayerType) {
                    w.setLayerType(targetLayerType, null)
                }
            }
        }
    )
}


@Composable
fun OfflineScreen(onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.WifiOff,
            contentDescription = "No Connection",
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Internet Connection",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "An active internet connection is required to use Arena AI. Please check your network and try again.",
            fontSize = 14.sp,
            color = Color(0xFF94A3B8),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetryClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Retry", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    isHardwareAccelerated: Boolean,
    onHardwareAcceleratedChange: (Boolean) -> Unit,
    onClearCacheClick: () -> Unit,
    currentUrl: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    // Notification fields for testing
    var pushTitle by remember { mutableStateOf("Super Leaderboard!") }
    var pushMessage by remember { mutableStateOf("Discover the new LLM leading the leaderboard.") }
    var pushUrl by remember { mutableStateOf("https://arena.ai/") }

    // Notification permission launcher
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notifications successfully authorized!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F172A), // Slate 900
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            // Header
            Text(
                text = "Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF38BDF8)
            )
            Text(
                text = "Customize the experience and test push notifications",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Section 1: Push Notifications
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Push Notification Center",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Text(
                            text = "Enable notifications to receive updates about new models and chatbot leaderboards.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Authorize Notifications", color = Color(0xFF0F172A))
                        }
                    } else {
                        Text(
                            text = "Notifications enabled! You can send a mock test push notification below:",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Push Fields
                        OutlinedTextField(
                            value = pushTitle,
                            onValueChange = { pushTitle = it },
                            label = { Text("Notification Title") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8),
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = pushMessage,
                            onValueChange = { pushMessage = it },
                            label = { Text("Notification Message") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedLabelColor = Color(0xFF38BDF8),
                                unfocusedLabelColor = Color(0xFF94A3B8),
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF475569)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                MyFirebaseMessagingService.sendNotification(
                                    context,
                                    pushTitle,
                                    pushMessage,
                                    pushUrl
                                )
                                Toast.makeText(context, "Test push notification sent!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Send Test Notification", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 2: Performance
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Performance & Fluidity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Optimize web rendering performance on older devices.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Hardware Acceleration", fontWeight = FontWeight.Medium)
                            Text(
                                text = "Use GPU for smooth rendering",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        Switch(
                            checked = isHardwareAccelerated,
                            onCheckedChange = onHardwareAcceleratedChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF38BDF8),
                                checkedTrackColor = Color(0xFF0F172A)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onClearCacheClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Clear Cache & Cookies", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 3: About App
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "About LMSYS Chatbot Arena",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "An open LLM evaluation platform developed by researchers from UC Berkeley, testing models via blind human preference.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section 4: GitHub Community & Feedback
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
            ) {
                val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "GitHub",
                            tint = Color(0xFF38BDF8)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "GitHub Community & Feedback",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Report bugs, propose new features, or contribute code directly on our GitHub repository.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { uriHandler.openUri("https://github.com/PiBOH/Arena-AI/issues/new/choose") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "New Issue", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(
                            onClick = { uriHandler.openUri("https://github.com/PiBOH/Arena-AI/pulls") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Pull Requests", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Footer URL indicator
            Text(
                text = "Active Page: $currentUrl",
                fontSize = 10.sp,
                color = Color(0xFF475569),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
