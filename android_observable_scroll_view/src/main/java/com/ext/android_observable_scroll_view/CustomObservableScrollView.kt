package com.ext.android_observable_scroll_view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import android.widget.ScrollView
import kotlin.math.abs

class ObservableScrollView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrollView(context, attrs, defStyleAttr) {

    private var scrollViewListener: ObservableScrollViewListener? = null
    private var touchListener: OnTouchListener? = null

    private var scrollState = ScrollState.IDLE
    private var lastY = 0

    // Toolbar binding
    private var boundToolbar: Toolbar? = null
    private var startToolbarColor = Color.TRANSPARENT
    private var endToolbarColor = Color.BLACK
    private var maxFadeScroll = 400

    // XML Attributes
    var parallaxMultiplier: Float = 0.5f
        private set
    var scrollTrackingEnabled: Boolean = true
        private set
    var scrollThreshold: Int = 0
        private set
    var enableSticky: Boolean = false
        private set

    // Sticky header
    private var stickyView: View? = null
    private var stickyViewTopOffset = 0
    private var stickyViewHeight = 0
    private var isViewSticky = false

    // Quick return feature
    private var quickReturnView: View? = null
    private var quickReturnViewHeight = 0
    private var lastScrollY = 0
    private var quickReturnMinScrollDistance = 50

    private var lastNotifiedScroll = 0

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it,
                R.styleable.ObservableScrollView,
                0,
                0
            )

            try {
                parallaxMultiplier = typedArray.getFloat(
                    R.styleable.ObservableScrollView_parallaxMultiplier,
                    0.5f
                )

                scrollTrackingEnabled = typedArray.getBoolean(
                    R.styleable.ObservableScrollView_scrollTrackingEnabled,
                    true
                )

                scrollThreshold = typedArray.getInt(
                    R.styleable.ObservableScrollView_scrollThreshold,
                    0
                )

                startToolbarColor = typedArray.getColor(
                    R.styleable.ObservableScrollView_toolbarStartColor,
                    Color.TRANSPARENT
                )

                endToolbarColor = typedArray.getColor(
                    R.styleable.ObservableScrollView_toolbarEndColor,
                    Color.BLACK
                )

                enableSticky = typedArray.getBoolean(
                    R.styleable.ObservableScrollView_enableSticky,
                    false
                )
            } finally {
                typedArray.recycle()
            }
        }
    }

    private val scrollRunnable: Runnable = object : Runnable {
        override fun run() {
            val newY = scrollY
            if (lastY == newY) {
                scrollState = ScrollState.IDLE
                if (scrollTrackingEnabled) {
                    scrollViewListener?.onScrollStateChanged(
                        this@ObservableScrollView,
                        ScrollState.IDLE
                    )
                }
            } else {
                postDelayed(this, 100)
            }
            lastY = newY
        }
    }

    enum class ScrollState {
        IDLE, TOUCH_SCROLL, FLING
    }

    enum class ScrollDirection {
        UP, DOWN, NONE
    }

    fun setScrollViewListener(listener: ObservableScrollViewListener?) {
        scrollViewListener = listener
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        // Determine scroll direction
        val direction = when {
            t > oldt -> ScrollDirection.DOWN
            t < oldt -> ScrollDirection.UP
            else -> ScrollDirection.NONE
        }

        // Notify listener with threshold
        if (scrollTrackingEnabled) {
            val scrollDelta = abs(t - lastNotifiedScroll)
            if (scrollDelta >= scrollThreshold) {
                scrollViewListener?.onScrollChanged(this, l, t, oldl, oldt)
                scrollViewListener?.onScrollDirectionChanged(this, direction)
                lastNotifiedScroll = t
            }
        }

        // Apply toolbar color transition
        applyToolbarColor(t)

        // Handle sticky view
        handleStickyView(t)

        // Handle quick return view
        handleQuickReturn(t, direction)

        lastScrollY = t
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                scrollState = ScrollState.TOUCH_SCROLL
                if (scrollTrackingEnabled) {
                    scrollViewListener?.onScrollStateChanged(this, scrollState)
                }
                removeCallbacks(scrollRunnable)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                scrollState = ScrollState.FLING
                if (scrollTrackingEnabled) {
                    scrollViewListener?.onScrollStateChanged(this, scrollState)
                }
                lastY = scrollY
                postDelayed(scrollRunnable, 100)
            }
        }
        touchListener?.onTouch(this, ev)
        return super.onTouchEvent(ev)
    }

    override fun setOnTouchListener(l: OnTouchListener?) {
        touchListener = l
    }

    override fun fling(velocityY: Int) {
        super.fling(velocityY)
        scrollState = ScrollState.FLING
        if (scrollTrackingEnabled) {
            scrollViewListener?.onScrollStateChanged(this, scrollState)
        }
    }

    // ==================== TOOLBAR BINDING ====================

    fun bindToolbar(
        toolbar: Toolbar,
        startColor: Int = this.startToolbarColor,
        endColor: Int = this.endToolbarColor,
        fadeHeight: Int = 400
    ) {
        boundToolbar = toolbar
        startToolbarColor = startColor
        endToolbarColor = endColor
        maxFadeScroll = fadeHeight
        toolbar.setBackgroundColor(startColor)
    }

    private fun applyToolbarColor(scrollY: Int) {
        boundToolbar?.let { toolbar ->
            val fraction = (scrollY.toFloat() / maxFadeScroll).coerceIn(0f, 1f)

            toolbar.setBackgroundColor(
                interpolateColor(startToolbarColor, endToolbarColor, fraction)
            )

            // Optional: fade in toolbar
            if (startToolbarColor == Color.TRANSPARENT) {
                toolbar.alpha = fraction
            }
        }
    }

    private fun interpolateColor(start: Int, end: Int, fraction: Float): Int {
        val startA = Color.alpha(start)
        val startR = Color.red(start)
        val startG = Color.green(start)
        val startB = Color.blue(start)

        val endA = Color.alpha(end)
        val endR = Color.red(end)
        val endG = Color.green(end)
        val endB = Color.blue(end)

        return Color.argb(
            (startA + (endA - startA) * fraction).toInt(),
            (startR + (endR - startR) * fraction).toInt(),
            (startG + (endG - startG) * fraction).toInt(),
            (startB + (endB - startB) * fraction).toInt()
        )
    }

    // ==================== STICKY HEADER ====================

    fun setStickyView(view: View, topOffset: Int = 0) {
        stickyView = view
        stickyViewTopOffset = topOffset
        stickyViewHeight = view.measuredHeight

        if (stickyViewHeight == 0) {
            view.post {
                stickyViewHeight = view.measuredHeight
            }
        }
    }

    private fun handleStickyView(scrollY: Int) {
        if (!enableSticky || stickyView == null) return

        val stickyView = stickyView ?: return

        // Calculate if view should stick
        val shouldStick = scrollY >= stickyViewTopOffset

        if (shouldStick && !isViewSticky) {
            // Make view sticky
            isViewSticky = true
            stickyView.translationY = scrollY.toFloat()
            scrollViewListener?.onStickyViewChanged(this, true)
        } else if (!shouldStick && isViewSticky) {
            // Release sticky view
            isViewSticky = false
            stickyView.translationY = 0f
            scrollViewListener?.onStickyViewChanged(this, false)
        } else if (isViewSticky) {
            // Update sticky position
            stickyView.translationY = scrollY.toFloat()
        }
    }

    // ==================== QUICK RETURN ====================

    fun setQuickReturnView(view: View, minScrollDistance: Int = 50) {
        quickReturnView = view
        quickReturnViewHeight = view.measuredHeight
        quickReturnMinScrollDistance = minScrollDistance

        if (quickReturnViewHeight == 0) {
            view.post {
                quickReturnViewHeight = view.measuredHeight
            }
        }
    }

    private fun handleQuickReturn(scrollY: Int, direction: ScrollDirection) {
        quickReturnView?.let { view ->
            val scrollDelta = abs(scrollY - lastScrollY)

            if (scrollDelta < quickReturnMinScrollDistance) return

            when (direction) {
                ScrollDirection.DOWN -> {
                    // Hide view when scrolling down
                    view.animate()
                        .translationY(-quickReturnViewHeight.toFloat())
                        .setDuration(200)
                        .start()
                }
                ScrollDirection.UP -> {
                    // Show view when scrolling up
                    view.animate()
                        .translationY(0f)
                        .setDuration(200)
                        .start()
                }
                ScrollDirection.NONE -> {}
            }
        }
    }

    // ==================== SMOOTH SCROLL ====================

    fun smoothScrollToTop(duration: Long = 500) {
        smoothScrollTo(0, 0)
    }

    fun smoothScrollToBottom(duration: Long = 500) {
        val child = getChildAt(0)
        if (child != null) {
            val bottom = child.height - height
            smoothScrollTo(0, bottom)
        }
    }

    // ==================== PUBLIC SETTERS ====================

    fun setParallaxMultiplier(multiplier: Float) {
        parallaxMultiplier = multiplier.coerceIn(0f, 2f)
    }

    fun setScrollTrackingEnabled(enabled: Boolean) {
        scrollTrackingEnabled = enabled
    }

    fun setScrollThreshold(threshold: Int) {
        scrollThreshold = threshold.coerceAtLeast(0)
    }

    fun setEnableSticky(enabled: Boolean) {
        enableSticky = enabled
    }

    // ==================== UTILITY METHODS ====================

    fun isScrolledToTop(): Boolean = scrollY == 0

    fun isScrolledToBottom(): Boolean {
        val child = getChildAt(0) ?: return true
        return scrollY >= (child.height - height)
    }

    fun getScrollPercentage(): Float {
        val child = getChildAt(0) ?: return 0f
        val maxScroll = (child.height - height).coerceAtLeast(1)
        return (scrollY.toFloat() / maxScroll * 100f).coerceIn(0f, 100f)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(scrollRunnable)
        scrollViewListener = null
        touchListener = null
    }
}



//package com.ext.android_observable_scroll_view
//
//import android.content.Context
//import android.graphics.Color
//import android.util.AttributeSet
//import android.view.MotionEvent
//import androidx.appcompat.widget.Toolbar
//import android.widget.ScrollView
//
//class ObservableScrollView @JvmOverloads constructor(
//    context: Context,
//    attrs: AttributeSet? = null,
//    defStyleAttr: Int = 0
//) : ScrollView(context, attrs, defStyleAttr) {
//
//    private var scrollViewListener: ObservableScrollViewListener? = null
//    private var touchListener: OnTouchListener? = null
//
//    private var scrollState = ScrollState.IDLE
//    private var lastY = 0
//
//    // 🔵 Toolbar binding
//    private var boundToolbar: Toolbar? = null
//    private var startToolbarColor = Color.BLUE
//    private var endToolbarColor = Color.BLACK
//    private var maxFadeScroll = 400
//
//    // 🔵 XML Attributes
//    var parallaxMultiplier: Float = 0.5f
//        private set
//    var scrollTrackingEnabled: Boolean = true
//        private set
//    var scrollThreshold: Int = 0
//        private set
//    var enableSticky: Boolean = false
//        private set
//
//    private var lastNotifiedScroll = 0
//
//    init {
//        // 🔥 READ XML ATTRIBUTES
//        attrs?.let {
//            val typedArray = context.obtainStyledAttributes(
//                it,
//                R.styleable.ObservableScrollView,
//                0,
//                0
//            )
//
//            try {
//                parallaxMultiplier = typedArray.getFloat(
//                    R.styleable.ObservableScrollView_parallaxMultiplier,
//                    0.5f
//                )
//
//                scrollTrackingEnabled = typedArray.getBoolean(
//                    R.styleable.ObservableScrollView_scrollTrackingEnabled,
//                    true
//                )
//
//                scrollThreshold = typedArray.getInt(
//                    R.styleable.ObservableScrollView_scrollThreshold,
//                    0
//                )
//
//                startToolbarColor = typedArray.getColor(
//                    R.styleable.ObservableScrollView_toolbarStartColor,
//                    Color.BLUE
//                )
//
//                endToolbarColor = typedArray.getColor(
//                    R.styleable.ObservableScrollView_toolbarEndColor,
//                    Color.BLACK
//                )
//
//                enableSticky = typedArray.getBoolean(
//                    R.styleable.ObservableScrollView_enableSticky,
//                    false
//                )
//            } finally {
//                typedArray.recycle()
//            }
//        }
//    }
//
//    private val scrollRunnable: Runnable = object : Runnable {
//        override fun run() {
//            val newY = scrollY
//            if (lastY == newY) {
//                scrollState = ScrollState.IDLE
//                if (scrollTrackingEnabled) {
//                    scrollViewListener?.onScrollStateChanged(
//                        this@ObservableScrollView,
//                        ScrollState.IDLE
//                    )
//                }
//            } else {
//                postDelayed(this, 100)
//            }
//            lastY = newY
//        }
//    }
//
//    enum class ScrollState {
//        IDLE, TOUCH_SCROLL, FLING
//    }
//
//    fun setScrollViewListener(listener: ObservableScrollViewListener?) {
//        scrollViewListener = listener
//    }
//
//    override fun onScrollChanged(
//        l: Int, t: Int, oldl: Int, oldt: Int
//    ) {
//        super.onScrollChanged(l, t, oldl, oldt)
//
//        // 🔥 Only notify if tracking is enabled and threshold is met
//        if (scrollTrackingEnabled) {
//            val scrollDelta = Math.abs(t - lastNotifiedScroll)
//            if (scrollDelta >= scrollThreshold) {
//                scrollViewListener?.onScrollChanged(this, l, t, oldl, oldt)
//                lastNotifiedScroll = t
//            }
//        }
//
//        // 🔥 Apply toolbar color transition
//        applyToolbarColor(t)
//    }
//
//    override fun onTouchEvent(ev: MotionEvent): Boolean {
//        when (ev.actionMasked) {
//            MotionEvent.ACTION_DOWN -> {
//                scrollState = ScrollState.TOUCH_SCROLL
//                if (scrollTrackingEnabled) {
//                    scrollViewListener?.onScrollStateChanged(this, scrollState)
//                }
//                removeCallbacks(scrollRunnable)
//            }
//            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
//                scrollState = ScrollState.FLING
//                if (scrollTrackingEnabled) {
//                    scrollViewListener?.onScrollStateChanged(this, scrollState)
//                }
//                lastY = scrollY
//                postDelayed(scrollRunnable, 100)
//            }
//        }
//        touchListener?.onTouch(this, ev)
//        return super.onTouchEvent(ev)
//    }
//
//    override fun setOnTouchListener(l: OnTouchListener?) {
//        touchListener = l
//    }
//
//    override fun fling(velocityY: Int) {
//        super.fling(velocityY)
//        scrollState = ScrollState.FLING
//        if (scrollTrackingEnabled) {
//            scrollViewListener?.onScrollStateChanged(this, scrollState)
//        }
//    }
//
//    // 🔵 BIND TOOLBAR (Blue → Other color)
//    fun bindToolbar(
//        toolbar: Toolbar,
//        startColor: Int = this.startToolbarColor,
//        endColor: Int = this.endToolbarColor,
//        fadeHeight: Int = 400
//    ) {
//        boundToolbar = toolbar
//        startToolbarColor = startColor
//        endToolbarColor = endColor
//        maxFadeScroll = fadeHeight
//        toolbar.setBackgroundColor(startColor)
//    }
//
//    // 🔵 COLOR INTERPOLATION
//    private fun applyToolbarColor(scrollY: Int) {
//        boundToolbar?.let { toolbar ->
//            val fraction = (scrollY.toFloat() / maxFadeScroll).coerceIn(0f, 1f)
//
//            toolbar.setBackgroundColor(
//                interpolateColor(startToolbarColor, endToolbarColor, fraction)
//            )
//
//            toolbar.alpha = fraction
//        }
//    }
//
//    private fun interpolateColor(start: Int, end: Int, fraction: Float): Int {
//        val r = (Color.red(start) + (Color.red(end) - Color.red(start)) * fraction).toInt()
//        val g = (Color.green(start) + (Color.green(end) - Color.green(start)) * fraction).toInt()
//        val b = (Color.blue(start) + (Color.blue(end) - Color.blue(start)) * fraction).toInt()
//
//        return Color.rgb(r, g, b)
//    }
//
//    // 🔵 Public setters for programmatic changes
//    fun setParallaxMultiplier(multiplier: Float) {
//        parallaxMultiplier = multiplier.coerceIn(0f, 2f)
//    }
//
//    fun setScrollTrackingEnabled(enabled: Boolean) {
//        scrollTrackingEnabled = enabled
//    }
//
//    fun setScrollThreshold(threshold: Int) {
//        scrollThreshold = threshold.coerceAtLeast(0)
//    }
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        removeCallbacks(scrollRunnable)
//    }
//}