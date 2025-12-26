package com.ext.android_observablescrollview

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowInsetsController
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.ext.android_observable_scroll_view.ObservableScrollView
import com.ext.android_observable_scroll_view.ObservableScrollViewListener
import android.widget.ScrollView

class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var scrollView: ObservableScrollView
    private lateinit var headerImage: ImageView

    private val headerHeight = 250

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = Color.BLACK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0, // no light icons
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
        toolbar = findViewById(R.id.toolbar)
        scrollView = findViewById(R.id.observableScrollView)
        headerImage = findViewById(R.id.header_image)

        setSupportActionBar(toolbar)
        supportActionBar?.title = "My App"

        // 🔥 BIND TOOLBAR - uses colors from XML attributes
        scrollView.bindToolbar(
            toolbar = toolbar,
            fadeHeight = 400
        )

        setupParallax()
    }

    private fun setupParallax() {
        scrollView.setScrollViewListener(object : ObservableScrollViewListener {
            override fun onScrollChanged(
                scrollView: ScrollView,
                x: Int,
                y: Int,
                oldx: Int,
                oldy: Int
            ) {
                // 🔥 Use parallaxMultiplier from XML
                val multiplier = this@MainActivity.scrollView.parallaxMultiplier
                headerImage.translationY = y * multiplier

                // Optional scale
                val scale = 1f + (y.toFloat() / headerHeight * 0.15f).coerceIn(0f, 0.15f)
                headerImage.scaleX = scale
                headerImage.scaleY = scale

                // Optional fade
                headerImage.alpha = (1f - y.toFloat() / headerHeight).coerceIn(0.3f, 1f)
            }

            override fun onScrollStateChanged(
                scrollView: ObservableScrollView,
                state: ObservableScrollView.ScrollState
            ) {
                // Handle scroll state changes if needed
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        scrollView.setScrollViewListener(null)
    }
}



