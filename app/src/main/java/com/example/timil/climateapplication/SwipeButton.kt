package com.example.timil.climateapplication

import android.animation.*
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.custom_swipe_button_layout.view.*
import android.view.LayoutInflater
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_view_discount.view.*

class SwipeButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = -1,
    defStyleRes: Int = -1
) : RelativeLayout(context, attrs, defStyle, defStyleRes) {

    private var active: Boolean = false
    private var initialButtonWidth: Int = 0
    private var initialX = 0f

    private var disabledDrawable: Drawable? = null
    private var enabledDrawable: Drawable? = null
    private var viewGroup: ViewGroup? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.custom_swipe_button_layout, this, true)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                it, R.styleable.custom_component_attributes, 0, 0
            )
            typedArray.recycle()
        }
        setButtonTouchListener()
    }

    private fun setButtonTouchListener() {
        sliding_button.setOnTouchListener { _, event ->
            val maxSwipe = swipe_btn.width - sliding_button.width.toFloat()
            val x = event.rawX
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = x
                }
                MotionEvent.ACTION_MOVE -> {
                    if (x > initialX && x < initialX + maxSwipe) {
                        sliding_button.x = x - initialX
                        center_text.alpha = 1 - 1.3f * (sliding_button.x + sliding_button.width) / width
                        sliding_button.foreground.alpha = 255 - ((x - initialX)*0.45).toInt()
                    } else if (x >= initialX + maxSwipe) {
                        sliding_button.x = maxSwipe
                    } else {
                        sliding_button.x = 0f
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (!active) {
                        initialButtonWidth = sliding_button.width
                        if (sliding_button.x + sliding_button.width > width * 0.85) {
                            sliding_button.foreground.alpha = 0
                            expandButton()
                        } else {
                            moveButtonBack()
                            sliding_button.foreground.alpha = 255
                        }
                    }
                }
            }
            true
        }
    }

    private fun expandButton() {
        val positionAnimator = ValueAnimator.ofFloat(sliding_button.x, 0f)
        positionAnimator.addUpdateListener {
            val x = positionAnimator.animatedValue as Float
            sliding_button.x = x
        }

        val widthAnimator = ValueAnimator.ofInt(
            sliding_button.width,
            width
        )

        widthAnimator.addUpdateListener {
            val params = sliding_button.layoutParams
            params.width = widthAnimator.animatedValue as Int
            sliding_button.layoutParams = params
        }

        val animatorSet = AnimatorSet()
        animatorSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)

                active = true
                sliding_button.setImageDrawable(enabledDrawable)
            }
        })

        animatorSet.playTogether(positionAnimator, widthAnimator)
        animatorSet.start()
        showDialog()
    }

    private fun collapseButton() {
        sliding_button.foreground.alpha = 255
        val widthAnimator = ValueAnimator.ofInt(
            sliding_button.width,
            initialButtonWidth
        )

        widthAnimator.addUpdateListener {
            val params = sliding_button.layoutParams
            params.width = widthAnimator.animatedValue as Int
            sliding_button.layoutParams = params
        }

        widthAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                active = false
                sliding_button.setImageDrawable(disabledDrawable)
            }
        })

        val objectAnimator = ObjectAnimator.ofFloat(
            center_text, "alpha", 1f
        )

        val animatorSet = AnimatorSet()

        animatorSet.playTogether(objectAnimator, widthAnimator)
        animatorSet.start()
    }

    private fun moveButtonBack() {
        val positionAnimator = ValueAnimator.ofFloat(sliding_button.x, 0f)
        positionAnimator.interpolator = AccelerateDecelerateInterpolator()
        positionAnimator.addUpdateListener {
            val x = positionAnimator.animatedValue as Float
            sliding_button.x = x
        }

        val objectAnimator = ObjectAnimator.ofFloat(
            center_text, "alpha", 1f
        )

        positionAnimator.duration = 200

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(objectAnimator, positionAnimator)
        animatorSet.start()
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(context)
        val layoutInflater = LayoutInflater.from(context)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        val dialogText: TextView = dialogView.findViewById(R.id.dialog_text)
        dialogText.text = context.applicationContext.getText(R.string.use_discount)
        builder.setView(dialogView)
            .setPositiveButton(R.string.yes) { _, _ ->
                center_text.text = context.applicationContext.getText(R.string.discount_used)
                center_text.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                center_text.alpha = 1f
                center_text.bringToFront()
                // TODO(use discount here)
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.dismiss()
            }.show()
        builder.setOnDismissListener {
            collapseButton()
        }
    }
}