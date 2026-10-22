package com.vasudev.ekant.browser

import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView

/** A ScrollView that never grows past ~72% of the screen, so the menu stays a card. */
class MaxHeightScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : ScrollView(context, attrs, defStyle) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val cap = (resources.displayMetrics.heightPixels * 0.72f).toInt()
        super.onMeasure(widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(cap, MeasureSpec.AT_MOST))
    }
}
