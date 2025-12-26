package com.ext.android_observable_scroll_view

import android.widget.ScrollView

/**
 * Listener interface for scroll events on ObservableScrollView
 */
interface ObservableScrollViewListener {

    /**
     * Called when the scroll position changes
     *
     * @param scrollView The scrollView that triggered the event
     * @param x Current horizontal scroll position
     * @param y Current vertical scroll position
     * @param oldx Previous horizontal scroll position
     * @param oldy Previous vertical scroll position
     */
    fun onScrollChanged(scrollView: ScrollView, x: Int, y: Int, oldx: Int, oldy: Int)

    /**
     * Called when scroll state changes (idle, touch scroll, fling)
     *
     * @param scrollView The scrollView that triggered the event
     * @param state The new scroll state
     */
    fun onScrollStateChanged(
        scrollView: ObservableScrollView,
        state: ObservableScrollView.ScrollState
    ) {
        // Default empty implementation
    }

    /**
     * Called when scroll direction changes
     *
     * @param scrollView The scrollView that triggered the event
     * @param direction The scroll direction (UP, DOWN, NONE)
     */
    fun onScrollDirectionChanged(
        scrollView: ObservableScrollView,
        direction: ObservableScrollView.ScrollDirection
    ) {
        // Default empty implementation
    }

    /**
     * Called when sticky view state changes
     *
     * @param scrollView The scrollView that triggered the event
     * @param isSticky True if view is now sticky, false otherwise
     */
    fun onStickyViewChanged(
        scrollView: ObservableScrollView,
        isSticky: Boolean
    ) {
        // Default empty implementation
    }
}