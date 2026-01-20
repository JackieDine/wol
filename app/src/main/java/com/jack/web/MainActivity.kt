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
        val currentPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentUrl = currentPrefs.getString(KEY_HOME_URL, DEFAULT_URL)

        val editText = EditText(this)
        editText.setText(currentUrl)
        editText.setHint("例如: baidu.com")
        editText.setPadding(60, 40, 60, 40)

        AlertDialog.Builder(this)
            .setTitle("设置您的主页")
            .setMessage("输入网址后点击保存，此后轻触按钮即可返回该页面。")
            .setView(editText)
            .setPositiveButton("保存并跳转") { _, _ ->
                var newUrl = editText.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    if (!newUrl.startsWith("http")) {
                        newUrl = "https://$newUrl"
                    }
                    // 持久化保存
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString(KEY_HOME_URL, newUrl).apply()
                    
                    homeUrl = newUrl
                    webView.loadUrl(newUrl)
                    Toast.makeText(this, "主页设置成功！", Toast.LENGTH_SHORT).show()
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
