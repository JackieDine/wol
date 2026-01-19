package com.jack.web

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupTransparentStatusBar()

        webView = findViewById(R.id.webview)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)

        // 配置 WebView 设置
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            mediaPlaybackRequiresUserGesture = false // 允许自动播放
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        // 处理视频全屏的关键：WebChromeClient
        webView.webChromeClient = object : WebChromeClient() {
            
            // 当网页请求进入全屏时调用
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    onHideCustomView()
                    return
                }
                customView = view
                customViewCallback = callback
                
                // 1. 强制设为横屏
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                
                // 2. 隐藏状态栏和导航栏，进入全屏模式
                window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN 
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION 
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
                
                // 3. 将视频 View 添加到当前布局最上层
                (window.decorView as FrameLayout).addView(customView, FrameLayout.LayoutParams(-1, -1))
                webView.visibility = View.GONE
            }

            // 当网页请求退出全屏时调用
            override fun onHideCustomView() {
                (window.decorView as FrameLayout).removeView(customView)
                customView = null
                customViewCallback?.onCustomViewHidden()
                
                // 1. 恢复竖屏
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                
                // 2. 恢复沉浸式状态栏设置
                setupTransparentStatusBar()
                
                webView.visibility = View.VISIBLE
            }
        }

        swipeRefreshLayout.setOnRefreshListener {
            webView.reload()
        }

        // 修改目标网址
        webView.loadUrl("https://quik-page.github.io/")
    }

    private fun setupTransparentStatusBar() {
        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            } else {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
        }
    }

    override fun onBackPressed() {
        // 如果正在全屏播放视频，按返回键先退出全屏
        if (customView != null) {
            webView.webChromeClient?.onHideCustomView()
        } else if (webView.canGoBack()) {
            webView.goBack()
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
}
