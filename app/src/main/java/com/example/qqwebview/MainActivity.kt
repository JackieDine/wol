package com.example.networkwakeup

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val url = "https://nas.gaoge.asia/wola72qede6n5a92yah" // configured URL

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)

        val ws: WebSettings = webView.settings
        ws.javaScriptEnabled = true
        ws.domStorageEnabled = true
        ws.loadWithOverviewMode = true
        ws.useWideViewPort = true
        ws.allowFileAccess = false
        ws.allowContentAccess = false

        // Enable media playback without user gesture (useful for inline video)
        ws.mediaPlaybackRequiresUserGesture = false

        // Cookie handling — keep user logged in across sessions
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(webView, true)
        }

        // Keep navigation inside the WebView
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                // Always load in this WebView (prevents external browser)
                view.loadUrl(request.url.toString())
                return true
            }
        }

        // For video full-screen support and other chrome features
        webView.webChromeClient = object : WebChromeClient() {
            // You can add custom full-screen handlers here if needed
        }

        // Load URL or local offline page if no network
        if (isNetworkAvailable()) {
            webView.loadUrl(url)
        } else {
            webView.loadUrl("file:///android_asset/offline.html")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(n) ?: return false
            return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val ni = cm.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return ni.isConnected
        }
    }
}
