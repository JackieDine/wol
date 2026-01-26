package com.jack.web

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var homeButton: FloatingActionButton
    
    // 默认备选地址
    private var homeUrl: String? = null
    private val DEFAULT_URL = "https://quik-page.github.io/"
    private val PREFS_NAME = "WebPrefs"
    private val KEY_HOME_URL = "home_url"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupTransparentStatusBar()

        webView = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        homeButton = findViewById(R.id.homeButton)

        // WebView 设置
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true // 允许访问本地 assets
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 1. 读取保存的主页
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        homeUrl = prefs.getString(KEY_HOME_URL, null)

        // 2. 首次启动逻辑判断
        if (homeUrl == null) {
            // 如果从未设置过，加载内置教程页
            webView.loadUrl("file:///android_asset/guide.html")
        } else {
            // 如果已设置，直接加载主页
            webView.loadUrl(homeUrl!!)
        }

        // 3. 悬浮按钮点击逻辑
        homeButton.setOnClickListener {
            val currentHome = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_HOME_URL, null)
            
            if (currentHome == null) {
                // 如果在教程页点击，提示长按
                Toast.makeText(this, "请长按此按钮设置您的首页", Toast.LENGTH_SHORT).show()
                webView.loadUrl("file:///android_asset/guide.html")
            } else {
                webView.loadUrl(currentHome)
            }
        }

        // 4. 悬浮按钮长按逻辑
        homeButton.setOnLongClickListener {
            showChangeHomeDialog()
            true
        }

        swipeRefreshLayout.setOnRefreshListener { webView.reload() }
    }

   private fun showChangeHomeDialog() {
    // 使用 MaterialAlertDialogBuilder 而非传统的 AlertDialog
    val editText = EditText(this)
    editText.setText(homeUrl ?: DEFAULT_URL)
    
    // M3 风格的输入框容器（增加边距）
    val container = android.widget.FrameLayout(this)
    val params = android.widget.FrameLayout.LayoutParams(
        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        android.view.ViewGroup.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(64, 20, 64, 0) // M3 需要更宽的边距
    editText.layoutParams = params
    container.addView(editText)

    com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
        .setTitle("更改主页")
        .setMessage("请输入新的起始网址")
        .setView(container)
        .setPositiveButton("保存") { _, _ ->
            val newUrl = editText.text.toString().trim()
            if (newUrl.isNotEmpty()) {
                val formattedUrl = if (newUrl.startsWith("http")) newUrl else "https://$newUrl"
                getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putString(KEY_HOME_URL, formattedUrl).apply()
                homeUrl = formattedUrl
                webView.loadUrl(formattedUrl)
            }
        }
        .setNegativeButton("取消", null)
        .show()
}

    private fun setupTransparentStatusBar() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
