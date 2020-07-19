package com.danego.pictureprocess

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewpager.widget.ViewPager
import java.util.jar.Attributes

class CustomViewPager : ViewPager {

    private var currentview: View? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (currentview == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            return
        }
        var height = 0
        currentview!!.measure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val h = currentview!!.measuredHeight
        if (h > height)
            height = h
        val heightMeasureSpe = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, heightMeasureSpe)
    }

    fun measureCurrentView(view: View) {
        currentview = view
        requestLayout()
    }

    fun measureFragment(view: View):Int{
        view.measure(0,0)
        return view.measuredHeight
    }
}