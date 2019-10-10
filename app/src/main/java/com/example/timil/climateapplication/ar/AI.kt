package com.example.timil.climateapplication.ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
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
    MORPHING
    //TODO: add various types of move patterns
}

// NOTE: ideally, these numbers should depend on the screen size, but ARCore makes that very tricky to do
private const val X_MAX_ABS = 0.25f
private const val Y_MAX = 0.25f
private const val Y_MIN = 0.10f

private const val ANIM_LENGTH_MAX = 4000L
private const val ANIM_LENGTH_MIN = 1000L

class AI(private val node: WorldEntity) {

    private var moveAnim: ObjectAnimator? = null
    private var scaleAnim: ObjectAnimator? = null
    private var spinAnim: ObjectAnimator? = null

    companion object {

        private val rGen = Random(System.currentTimeMillis())

        fun create(node: WorldEntity, type: AIType): AI? {

            return when (type) {
                AIType.BASIC -> AI(node).apply {

                    moveAnim = AnimationFactory.linearMoveAnim(
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
                } // BASIC apply

                AIType.MORPHING -> AI(node).apply {

                    val duration = randomDura()

                    moveAnim = AnimationFactory.linearMoveAnim(
                        node,
                        duration,
                        node.localPosition,
                        randomTarget(),
                        object : AnimatorListenerAdapter() {

                            // the ending of the move anim also governs the other animations' 'lifecycles'
                            override fun onAnimationEnd(animation: Animator?) {

                                execute() // never stop moving until death
                            }
                        }) // linearAnim

                    scaleAnim = AnimationFactory.scaleAnim(node, duration, randomScale())

                    spinAnim = AnimationFactory.spinAnim(node, duration, Static.randomizedQuaternion())
                } // MORPHING apply
            } // when
        } // create
    } // companion object

    fun execute() {

        // clumsy, but AnimatorSet doesn't seem to work here for some reason

        val dura = randomDura()

        moveAnim?.duration = dura
        moveAnim?.setObjectValues(node.localPosition, randomTarget())

        scaleAnim?.duration = dura
        scaleAnim?.setObjectValues(node.localScale, randomScale())

        spinAnim?.duration = dura
        spinAnim?.setObjectValues(node.localRotation, Static.randomizedQuaternion())

        moveAnim?.start()
        scaleAnim?.start()
        spinAnim?.start()
    } // execute

    fun terminate() {

        moveAnim?.removeAllListeners()
        moveAnim = null
        scaleAnim?.removeAllListeners()
        scaleAnim = null
        spinAnim?.removeAllListeners()
        spinAnim = null
    } // terminate

    fun pauseExecution() {

        moveAnim?.pause()
        spinAnim?.pause()
        scaleAnim?.pause()
    }

    fun resumeExecution() {

        moveAnim?.resume()
        spinAnim?.resume()
        scaleAnim?.resume()
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

    private fun randomScale(): Vector3 {

        val randX = Static.randomFloatBetween(0.7f, 1.3f)
        val randY = Static.randomFloatBetween(0.7f, 1.3f)
        val randZ = Static.randomFloatBetween(0.7f, 1.3f)
        return Vector3(randX, randY, randZ)
    }

} // AI