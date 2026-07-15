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

        // Pre-create the WebView wasm cache directory to prevent chromium logs warnings/errors
        try {
            val wasmCacheDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!wasmCacheDir.exists()) {
                wasmCacheDir.mkdirs()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

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
        val builder = NetworkRequest.Builder()
        connectivityManager.registerNetworkCallback(builder.build(), object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread { isOnline.value = true }
            }

            override fun onLost(network: Network) {
                runOnUiThread { isOnline.value = false }
            }
        })
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

private fun injectCustomMenuJavascript(webView: WebView?) {
    webView?.evaluateJavascript(
        """
        (function() {
            function injectCustomTabs() {
                try {
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

            injectCustomTabs();
            if (!window.androidTabsInterval) {
                window.androidTabsInterval = setInterval(injectCustomTabs, 1000);
            }
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
            }

            val webView = WebView(ctx).apply {
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
                        val isAuthFlow = lowerUrl.contains("accounts.google") ||
                                         lowerUrl.contains("google.com/accounts") ||
                                         lowerUrl.contains("huggingface.co") ||
                                         lowerUrl.contains("github.com/login") ||
                                         lowerUrl.contains("github.com/session") ||
                                         lowerUrl.contains("appleid.apple.com") ||
                                         lowerUrl.contains("oauth") ||
                                         lowerUrl.contains("login") ||
                                         lowerUrl.contains("signin") ||
                                         lowerUrl.contains("signup") ||
                                         lowerUrl.contains("auth") ||
                                         lowerUrl.contains("checkpoint")

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
                    try {
                        val request = DownloadManager.Request(Uri.parse(downloadUrl)).apply {
                            setMimeType(mimetype)
                            val cookies = CookieManager.getInstance().getCookie(downloadUrl)
                            addRequestHeader("cookie", cookies)
                            addRequestHeader("User-Agent", userAgentString)
                            setDescription("Downloading file...")
                            setTitle(URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype))
                            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                URLUtil.guessFileName(downloadUrl, contentDisposition, mimetype)
                            )
                        }
                        val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        dm.enqueue(request)
                        Toast.makeText(ctx, "Download started...", Toast.LENGTH_SHORT).show()
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
                        if (newProgress > 50) {
                            injectCustomMenuJavascript(view)
                        }
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
                w.setLayerType(
                    if (isHardwareAccelerated) View.LAYER_TYPE_HARDWARE else View.LAYER_TYPE_SOFTWARE,
                    null
                )
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
