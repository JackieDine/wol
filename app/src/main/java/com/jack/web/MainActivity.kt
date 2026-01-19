package com.jack.web

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. 设置透明状态栏环境
        setupTransparentStatusBar()

        webView = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // 2. 配置下拉刷新
        swipeRefreshLayout.setOnRefreshListener {
            webView.reload() // 刷新网页
        }

        // 3. 配置 WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                // 网页加载完成后停止刷新动画
                swipeRefreshLayout.isRefreshing = false
            }
        }

        webView.loadUrl("https://www.qq.com")
    }

    private fun setupTransparentStatusBar() {
        window.apply {
            // 设置状态栏为透明，背景将由布局文件中的 fitsSystemWindows 颜色（白色）决定
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT

            // Android 6.0+ 自动适配深色图标（解决白色背景看不清时间的问题）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
