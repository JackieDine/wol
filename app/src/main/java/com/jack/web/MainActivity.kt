package com.example.qqwebview

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        
        // 配置 WebView 设置
        webView.settings.apply {
            javaScriptEnabled = true // 启用 JS
            domStorageEnabled = true // 启用 DOM 存储
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        // 关键：设置 WebViewClient，防止点击链接跳出 APP
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false // 返回 false 表示由 WebView 处理 URL
            }
        }

        // 加载 QQ
        webView.loadUrl("https://www.qq.com")

        // 处理返回键逻辑（网页后退）
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }
}
