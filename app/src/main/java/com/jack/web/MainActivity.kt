package com.jack.web

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
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
    private val DEFAULT_URL = "https://quik-page.github.io/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 初始化沉浸式配置（必须在 setContentView 之前）
        setupTransparentStatusBar()
        
        setContentView(R.layout.activity_main)

        // 2. 绑定 UI 组件
        webView = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        homeButton = findViewById(R.id.homeButton)

        // 3. 配置 WebView
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true 
            databaseEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // 响应式布局支持
            useWideViewPort = true
            loadWithOverviewMode = true
        }

        // 开启嵌套滚动支持 FAB 的滑动隐藏行为
        webView.isNestedScrollingEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 4. 读取存储的 URL 逻辑
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        homeUrl = prefs.getString(KEY_HOME_URL, null)

        if (homeUrl == null) {
            // 首次启动，加载内置教程页
            webView.loadUrl("file:///android_asset/guide.html")
        } else {
            // 已有配置，直接加载
            webView.loadUrl(homeUrl!!)
        }

        // 5. 悬浮按钮点击与长按
        homeButton.setOnClickListener {
            val currentHome = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(KEY_HOME_URL, null)
            if (currentHome == null) {
                Toast.makeText(this, "请长按此按钮设置主页", Toast.LENGTH_SHORT).show()
                webView.loadUrl("file:///android_asset/guide.html")
            } else {
                webView.loadUrl(currentHome)
            }
        }

        homeButton.setOnLongClickListener {
            showM3Dialog()
            true
        }

        // 6. 下拉刷新
        swipeRefreshLayout.setOnRefreshListener { webView.reload() }
    }

    /**
     * 使用 Material Design 3 风格的对话框
     */
    private fun showM3Dialog() {
        val editText = EditText(this)
        editText.setText(homeUrl ?: "")
        editText.hint = "例如: baidu.com"
        
        // 为输入框添加外边距，符合 M3 规范
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, 
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val margin = (24 * resources.displayMetrics.density).toInt()
        params.setMargins(margin, (8 * resources.displayMetrics.density).toInt(), margin, 0)
        editText.layoutParams = params
        container.addView(editText)

        MaterialAlertDialogBuilder(this)
            .setTitle("设置主页地址")
            .setView(container)
            .setPositiveButton("保存并前往") { _, _ ->
                var url = editText.text.toString().trim()
                if (url.isNotEmpty()) {
                    if (!url.startsWith("http")) url = "https://$url"
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString(KEY_HOME_URL, url).apply()
                    homeUrl = url
                    webView.loadUrl(url)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 沉浸式状态栏适配：解决遮挡与图标颜色
     */
    private fun setupTransparentStatusBar() {
        // 告诉系统内容可以延伸到系统栏（状态栏/导航栏）
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 设置状态栏背景透明
        window.statusBarColor = Color.TRANSPARENT

        // 适配状态栏图标颜色（深色/浅色模式切换）
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        val isNightMode = (resources.configuration.uiMode and 
                          android.content.res.Configuration.UI_MODE_NIGHT_MASK) == 
                          android.content.res.Configuration.UI_MODE_NIGHT_YES
        
        // 非深色模式时，将状态栏图标设为深色
        controller.isAppearanceLightStatusBars = !isNightMode
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}