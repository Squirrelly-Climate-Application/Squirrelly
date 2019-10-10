package com.example.timil.climateapplication.ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.util.Log
import com.google.ar.sceneform.math.Vector3
import java.util.*

/**
 * An abstraction to hold monster move patterns (animations).
 * May add other features later on.
 * @author Ville Lohkovuori
 * */

enum class AIType {
    BASIC,
    //TODO: add various types of move patterns
}

// NOTE: ideally, these numbers should depend on the screen size, but ARCore makes that very tricky to do
private const val X_MAX_ABS = 0.25f
private const val Y_MAX = 0.25f
private const val Y_MIN = 0.10f

private const val ANIM_LENGTH_MAX = 4000L
private const val ANIM_LENGTH_MIN = 1000L

class AI(private val node: WorldEntity) {

    private var anim: ObjectAnimator? = null

    companion object {

        private val rGen = Random(System.currentTimeMillis())

        fun create(node: WorldEntity, type: AIType): AI? {

            return when (type) {
                AIType.BASIC -> AI(node).apply {

                    anim = AnimationFactory.linearMoveAnim(
                        node,
                        randomDura(),
                        node.localPosition,
                        randomTarget(),
                        object : AnimatorListenerAdapter() {

                            override fun onAnimationEnd(animation: Animator?) {

                                // Log.d("HUUH", "monster position: " + node.localPosition)
                                execute() // never stop moving until death
                            }
                        }) // linearAnim
                } // apply
            } // when
        } // create
    } // companion object

    fun execute() {

        anim?.duration = randomDura()
        anim?.setObjectValues(node.localPosition, randomTarget())
        // Log.d("HUUH", "animation dura: " + animation.duration)
        // Log.d("HUUH", "anim dura: " + anim?.duration)
        anim?.start()
    }

    fun terminate() {

        anim?.removeAllListeners()
        anim = null
    }

    fun pauseExecution() {

        anim?.pause()
    }

    fun resumeExecution() {

        anim?.resume()
    }

    private fun randomDura(): Long {

        return (ANIM_LENGTH_MIN + (rGen.nextFloat() * (ANIM_LENGTH_MAX - ANIM_LENGTH_MIN))).toLong()
    }

    private fun randomTarget(): Vector3 {

        val xSign = if (rGen.nextBoolean()) 1 else -1

        val x = rGen.nextFloat() * X_MAX_ABS * xSign
        val y = Y_MIN + rGen.nextFloat() * (Y_MAX - Y_MIN)
        return Vector3(x, y, -1.0f) // could randomize z-value as well, I guess
    }

} // AI