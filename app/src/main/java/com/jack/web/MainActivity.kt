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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var homeButton: FloatingActionButton
    private lateinit var statusBarPlaceholder: View

    private val PREFS_NAME = "WebPrefs"
    private val KEY_HOME_URL = "home_url"
    private var homeUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 开启彻底沉浸式模式
        setupTransparentStatusBar()

        setContentView(R.layout.activity_main)

        // 2. 绑定占位符并动态设置高度
        statusBarPlaceholder = findViewById(R.id.statusBarPlaceholder)
        ViewCompat.setOnApplyWindowInsetsListener(statusBarPlaceholder) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val params = view.layoutParams
            params.height = bars.top // 动态设为系统状态栏高度
            view.layoutParams = params
            insets
        }

        webView = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        homeButton = findViewById(R.id.homeButton)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.isNestedScrollingEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
                // 页面加载完成后，再次刷新状态栏图标颜色适配
                updateStatusBarIcons()
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
        val container = FrameLayout(this)
        val params = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val margin = (24 * resources.displayMetrics.density).toInt()
        params.setMargins(margin, (8 * resources.displayMetrics.density).toInt(), margin, 0)
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
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        updateStatusBarIcons()
    }

    private fun updateStatusBarIcons() {
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        val isNightMode = (resources.configuration.uiMode and
                          android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                          android.content.res.Configuration.UI_MODE_NIGHT_YES
        // 浅色模式下，状态栏图标设为深色
        controller.isAppearanceLightStatusBars = !isNightMode
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) webView.goBack() else super.onBackPressed()
    }
}
