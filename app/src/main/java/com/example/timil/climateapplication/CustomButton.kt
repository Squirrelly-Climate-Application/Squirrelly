package com.example.timil.climateapplication

import android.animation.*
import android.content.Context
import android.graphics.Path
import android.util.AttributeSet
import android.view.*
import android.widget.RelativeLayout
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.custom_button_layout.view.*
import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.view.animation.AccelerateInterpolator
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_start.view.*


class CustomButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = -1,
    defStyleRes: Int = -1
) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    private var onStartGameListener: OnStartGameListener? = null

    fun setOnStartGameListener(listener: OnStartGameListener) {
        onStartGameListener = listener
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_button_layout, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it, R.styleable.custom_component_attributes, 0, 0
            )
            typedArray.recycle()
        }
        setButtonClickListener()
    }


    private fun setButtonClickListener() {
        custom_button_button.setOnClickListener {
            moveButton()
        }
    }

    private fun moveButton() {
        val linearDailyTips = this.rootView.findViewById<LinearLayout>(R.id.linearDailyTips)
        linearDailyTips.visibility = LinearLayout.GONE

        custom_button_button.foreground = context!!.applicationContext.getDrawable(R.drawable.qr_code)
        custom_button_button.setTextColor(context!!.applicationContext.getColor(android.R.color.transparent))
        custom_button_button.setBackgroundColor(context!!.applicationContext.getColor(android.R.color.transparent))

        val x = custom_button_frame.x
        val y = custom_button_frame.y
        val path = Path().apply {
            arcTo(x-40, y, x+40, y+80, 270f, -180f, true)
            arcTo(x-40, y, x+40, y+80, 90f, 180f, true)
        }
        val circleAnimator = ObjectAnimator.ofFloat(this, View.X, View.Y, path).apply {
            duration = 2000
        }

        val centerX = custom_button_frame.width / 2.0f
        val centerY = custom_button_frame.height / 2.0f
        val rotation3dAnimator = Rotate3dAnimation(0f, 360f, centerX, centerY, -500.0f, true)
        rotation3dAnimator.fillAfter = true
        rotation3dAnimator.interpolator = AccelerateInterpolator()
        rotation3dAnimator.duration = 2000

        this.startAnimation(rotation3dAnimator)

        val colourAnimator = ObjectAnimator.ofObject(
            custom_button_button,
            "backgroundColor",
            ArgbEvaluator(),
            context!!.applicationContext.getColor(android.R.color.transparent),
            context!!.applicationContext.getColor(android.R.color.transparent)
        )
        colourAnimator.duration = 2000

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(circleAnimator)
        setAnimatorListener(animatorSet)
        animatorSet.start()

    }

    private fun setAnimatorListener(animatorSet: AnimatorSet) {
        animatorSet.addListener(object : AnimatorListener {

            override fun onAnimationStart(animation: Animator) {
                custom_button_button.isEnabled = false
            }

            override fun onAnimationRepeat(animation: Animator) {
                // ...
            }

            override fun onAnimationEnd(animation: Animator) {
                custom_button_button.isEnabled = true
                onStartGameListener!!.onStartGame()
            }

            override fun onAnimationCancel(animation: Animator) {
                // ...
            }
        })
    }
}