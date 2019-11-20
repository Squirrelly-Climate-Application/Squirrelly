package com.example.timil.climateapplication

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView

/**
 * A class that I found on the internet. Together with an oval-shaped shape resource,
 * it can be used to present a circular TextView.
 * */
class EqualWidthHeightTextView : TextView {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val r = Math.max(measuredWidth, measuredHeight)
        setMeasuredDimension(r, r)
    }
}