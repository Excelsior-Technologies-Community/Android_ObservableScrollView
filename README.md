# **ObservableScrollView Android Library**

---

ObservableScrollView is a custom ScrollView for Android that enhances the standard ScrollView with features like:

- **Parallax header scrolling**

- **Toolbar color fading on scroll**

- **Hide/show toolbar on scroll (quick return)**

- **Sticky header support**

- **Scroll listeners with state detection (IDLE, TOUCH_SCROLL, FLING)**

It’s easy to integrate, fully configurable via XML attributes or programmatically, and works seamlessly with Toolbar and other UI components.

---

## ✨ **Features**

- ObservableScrollView is a custom ScrollView for Android that enhances the standard ScrollView with features like:

- Parallax header scrolling

- Toolbar color fading on scroll

- Hide/show toolbar on scroll (quick return)

- Sticky header support

  ---

# **Preview**
---
<img src="https://github.com/S13reya/Android_ObservableScrollView/blob/stages/app/src/main/assets/demovideo.gif" height="320"/>


## ⚡ **Installation**

**Step 1:** Add JitPack repository to your root build.gradle:

```
gradle
maven { url = uri("https://jitpack.io") }
```

**Step 2:** Add the dependency in your app `build.gradle` (example if hosted on JitPack):  

```gradle
dependencies {
	        	        implementation 'com.github.Excelsior-Technologies-Community:Android_Popup_Dialog:1.0.0'

}
```


## ⚡ **Usage**

1. Add in XML

```
<com.ext.android_observable_scroll_view.ObservableScrollView
    android:id="@+id/observableScrollView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fillViewport="true"
    app:parallaxMultiplier="0.5"
    app:scrollTrackingEnabled="true"
    app:scrollThreshold="10"
    app:toolbarStartColor="@android:color/transparent"
    app:toolbarEndColor="#2196F3"
    app:enableSticky="true">

    <!-- Scrollable content here, e.g., LinearLayout with ImageView and Cards -->

</com.ext.android_observable_scroll_view.ObservableScrollView>

```

2.Toolbar Overlay Example

```
<androidx.appcompat.widget.Toolbar
    android:id="@+id/toolbar"
    android:layout_width="match_parent"
    android:layout_height="?attr/actionBarSize"
    android:background="@android:color/transparent"
    android:elevation="4dp"
    android:theme="@style/ThemeOverlay.AppCompat.Dark.ActionBar"/>

```



## **2. Setup in Activity**

**Bind Toolbar with Fade**

```
val scrollView = findViewById<ObservableScrollView>(R.id.observableScrollView)
val toolbar = findViewById<Toolbar>(R.id.toolbar)

scrollView.bindToolbar(
    toolbar = toolbar,
    startColor = Color.BLUE,
    endColor = Color.BLACK,
    fadeHeight = 400
)

```
**Setup Parallax Header**

```
scrollView.setScrollViewListener(object : ObservableScrollViewListener {
    override fun onScrollChanged(scrollView: ScrollView, x: Int, y: Int, oldx: Int, oldy: Int) {
        val multiplier = scrollView.parallaxMultiplier
        headerImage.translationY = y * multiplier
        val scale = 1f + (y.toFloat() / headerHeight * 0.15f).coerceIn(0f, 0.15f)
        headerImage.scaleX = scale
        headerImage.scaleY = scale
        headerImage.alpha = (1f - y.toFloat() / headerHeight).coerceIn(0.3f, 1f)
    }

    override fun onScrollStateChanged(scrollView: ObservableScrollView, state: ObservableScrollView.ScrollState) {}
})

```






## **📄 License**

**MIT License**  
```
Copyright (c) 2025 Excelsior Technologies

Permission is hereby granted, free of charge, to any person obtaining a copy  
of this software and associated documentation files (the "Software"), to deal  
in the Software without restriction, including without limitation the rights  
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell  
copies of the Software, and to permit persons to whom the Software is  
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all  
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED **"AS IS"**, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR  
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,  
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```



  
