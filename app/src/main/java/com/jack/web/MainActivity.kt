package com.jack.web

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.*
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var homeButton: FloatingActionButton
    
    // 默认主页
    private var homeUrl: String = "https://quik-page.github.io/"
    private val PREFS_NAME = "WebPrefs"
    private val KEY_HOME_URL = "home_url"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 读取保存的主页地址
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        homeUrl = prefs.getString(KEY_HOME_URL, homeUrl) ?: homeUrl

        setupTransparentStatusBar()

        webView = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        homeButton = findViewById(R.id.homeButton)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 短按：回到主页
        homeButton.setOnClickListener {
            webView.loadUrl(homeUrl)
        }

        // 长按逻辑（系统默认长按较快，我们手动实现长按逻辑或使用默认长按）
        homeButton.setOnLongClickListener {
            showChangeHomeDialog()
            true
        }

        swipeRefreshLayout.setOnRefreshListener { webView.reload() }
        
        webView.loadUrl(homeUrl)
    }

    // 弹出更改主页的对话框
    private fun showChangeHomeDialog() {
        val editText = EditText(this)
        editText.setText(homeUrl)
        
        AlertDialog.Builder(this)
            .setTitle("更改主页地址")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                var newUrl = editText.text.toString().trim()
                if (newUrl.isNotEmpty()) {
                    if (!newUrl.startsWith("http")) newUrl = "https://$newUrl"
                    homeUrl = newUrl
                    // 持久化保存
                    getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString(KEY_HOME_URL, homeUrl).apply()
                    webView.loadUrl(homeUrl)
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
