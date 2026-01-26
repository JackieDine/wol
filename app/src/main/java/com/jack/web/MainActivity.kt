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
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var homeButton: FloatingActionButton
    
    private val PREFS_NAME = "WebPrefs"
    private val KEY_HOME_URL = "home_url"
    private var homeUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 确保在 setContentView 之前没有任何导致崩溃的逻辑
        setContentView(R.layout.activity_main)

        setupTransparentStatusBar()

        webView = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        homeButton = findViewById(R.id.homeButton)

        // 核心配置
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true 
            databaseEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        // 开启嵌套滚动以支持 M3 按钮滑动隐藏
        webView.isNestedScrollingEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        homeUrl = prefs.getString(KEY_HOME_URL, null)

        if (homeUrl == null) {
            webView.loadUrl("file:///android_asset/guide.html")
        } else {
            webView.loadUrl(homeUrl!!)
        }

        homeButton.setOnClickListener {
            val currentHome = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_HOME_URL, null)
            if (currentHome == null) {
                Toast.makeText(this, "长按按钮设置主页", Toast.LENGTH_SHORT).show()
            } else {
                webView.loadUrl(currentHome)
            }
        }

        homeButton.setOnLongClickListener {
            showM3Dialog()
            true
        }

        swipeRefreshLayout.setOnRefreshListener { webView.reload() }
    }

    private fun showM3Dialog() {
        val editText = EditText(this)
        editText.setText(homeUrl ?: "")
        editText.hint = "https://www.google.com"
        
        // M3 对话框边距处理
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        params.setMargins(48, 24, 48, 0)
        editText.layoutParams = params
        container.addView(editText)

        MaterialAlertDialogBuilder(this)
            .setTitle("设置主页")
            .setView(container)
            .setPositiveButton("保存") { _, _ ->
                var url = editText.text.toString().trim()
                if (url.isNotEmpty()) {
                    if (!url.startsWith("http")) url = "https://$url"
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().putString(KEY_HOME_URL, url).apply()
                    homeUrl = url
                    webView.loadUrl(url)
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
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
