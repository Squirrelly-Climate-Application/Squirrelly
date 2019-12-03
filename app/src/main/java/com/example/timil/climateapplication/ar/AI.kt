package com.example.timil.climateapplication.ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.ar.sceneform.collision.Sphere
import com.google.ar.sceneform.math.Vector3
import java.util.*

/**
 * An abstraction to hold monster move patterns (animations).
 * @author Ville Lohkovuori
 * */

enum class AIType {
    BASIC,
    MORPHING,
    BOUNCING
}

private const val ANIM_LENGTH_MAX = 3600L
private const val ANIM_LENGTH_MIN = 2000L

private const val BOUNCE_ANIM_DURA = 3000L

// needed when scaling the Co2Monster's collisionShape radius;
// it's simply a value that gives good results
private const val RADIUS_HIT_SCALE_FACTOR = 3.8f

class AI(private val node: WorldEntity) {

    // we must store the AI type to differentiate logic in execute().
    // (consider subclassing the AI class to make for prettier code)
    private var type = AIType.BASIC

    private var moveAnim: ObjectAnimator? = null
    private var scaleAnim: ObjectAnimator? = null
    private var sphereCollisionShapeScaleAnim: ObjectAnimator? = null
    private var spinAnim: ObjectAnimator? = null

    private val bounceAnimLowTarget = node.localPosition
    private val bounceAnimTopTarget = Vector3(node.localPosition.x, node.localPosition.y+0.17f, node.localPosition.z)
    private val bounceAnimUpScale = Vector3(node.localScale.x * 0.9f, node.localScale.y * 1.1f, node.localScale.z * 0.9f) // stretch the drop when it floats up...
    private val bounceAnimDownScale = Vector3(node.localScale.x * 1.1111111f, node.localScale.y * 0.909090909f, node.localScale.z *1.1111111f) // ... and squeeze it when it floats back down

    private lateinit var durationPattern: DurationPattern

    companion object {

        private val rGen = Random(System.currentTimeMillis())

        // boundaries for monster movement. the default values work well for the Samsung Galaxy S7
        private var xMaxAbs = 0.25f
        private var yMax = 0.25f
        private var yMin = 0.10f

        // the boundaries of monster movement should depend on the phone model;
        // to accomplish this, call this function before using the AI class
        fun setMoveBoundaryConstants(xMaxAbsBoundary: Float, yMinBoundary: Float, yMaxBoundary: Float) {

            xMaxAbs = xMaxAbsBoundary
            yMax = yMaxBoundary
            yMin = yMinBoundary
        }

        fun create(node: WorldEntity, AItype: AIType): AI {

            return when (AItype) {

                AIType.BASIC -> AI(node).apply {

                    type = AItype

                    moveAnim = AnimationFactory.linearMoveAnim(
                        node,
                        randomDura(),
                        node.localPosition,
                        randomTarget(),
                        object : AnimatorListenerAdapter() {

                            override fun onAnimationEnd(animation: Animator?) {

                                execute() // never stop moving until death
                            }
                        }) // moveAnim
                } // BASIC apply

                AIType.MORPHING -> AI(node).apply {

                    type = AItype

                    durationPattern = DurationPattern(2000L, 3000L, 4000L)
                    val duration = durationPattern.current() // i.e., 2000L

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
                        }) // moveAnim

                    val newScale = Static.randomScale(0.8f, 1.3f)
                    scaleAnim = AnimationFactory.scaleAnim(node, duration, newScale)

                    val newRadius = maxOf(newScale.x, newScale.y, newScale.z) / 2 // / RADIUS_HIT_SCALE_FACTOR

                    sphereCollisionShapeScaleAnim = AnimationFactory.sphereCollisionShapeScaleAnim(node, duration, newRadius)

                    spinAnim = AnimationFactory.spinAnim(node, duration, Static.randomizedQuaternion())
                } // MORPHING apply
                AIType.BOUNCING -> AI(node).apply {

                    type = AItype

                    moveAnim = AnimationFactory.linearMoveAnim(
                        node,
                        BOUNCE_ANIM_DURA,
                        node.localPosition,
                        bounceAnimTopTarget,
                        object : AnimatorListenerAdapter() {

                            // the ending of the move anim also governs the other animations' 'lifecycles'
                            override fun onAnimationEnd(animation: Animator?) {

                                execute() // never stop moving until death
                            }
                        }) // moveAnim
                    moveAnim?.interpolator = AccelerateDecelerateInterpolator() // makes the bounce look better

                    scaleAnim = AnimationFactory.scaleAnim(node, BOUNCE_ANIM_DURA, bounceAnimUpScale)
                } // BOUNCING apply
            } // when
        } // create
    } // companion object

    fun execute() {

        when (type) {

            AIType.BASIC -> {

                moveAnim?.duration = randomDura()
                moveAnim?.setObjectValues(node.localPosition, randomTarget())
            }

            AIType.MORPHING -> {

                val dura = durationPattern.next()

                moveAnim?.duration = dura
                moveAnim?.setObjectValues(node.localPosition, randomTarget())

                val newScale = Static.randomScale(0.8f, 1.3f)

                scaleAnim?.duration = dura
                scaleAnim?.setObjectValues(node.localScale, newScale)

                val oldRadius = (node.renderable!!.collisionShape as Sphere).radius
                val newRadius = maxOf(newScale.x, newScale.y, newScale.z) / 2 / RADIUS_HIT_SCALE_FACTOR

                sphereCollisionShapeScaleAnim?.duration = dura
                sphereCollisionShapeScaleAnim?.setObjectValues(oldRadius, newRadius)

                spinAnim?.duration = dura
                spinAnim?.setObjectValues(node.localRotation, Static.randomizedQuaternion())
            } // AIType.MORPHING

            AIType.BOUNCING -> {

                // down position, so we'll bounce upwards
                if (node.localPosition == bounceAnimLowTarget) {

                    moveAnim?.setObjectValues(node.localPosition, bounceAnimTopTarget)
                    scaleAnim?.setObjectValues(node.localScale, bounceAnimUpScale)
                } else {

                    moveAnim?.setObjectValues(node.localPosition, bounceAnimLowTarget)
                    scaleAnim?.setObjectValues(node.localScale, bounceAnimDownScale)
                }
            } // AIType.BOUNCING
        } // when

        moveAnim?.start()
        scaleAnim?.start()
        sphereCollisionShapeScaleAnim?.start()
        spinAnim?.start()
    } // execute

    fun terminate() {

        moveAnim?.removeAllListeners()
        moveAnim = null
        scaleAnim?.removeAllListeners()
        scaleAnim = null
        sphereCollisionShapeScaleAnim?.removeAllListeners()
        sphereCollisionShapeScaleAnim = null
        spinAnim?.removeAllListeners()
        spinAnim = null
    } // terminate

    fun pauseExecution() {

        moveAnim?.pause()
        spinAnim?.pause()
        scaleAnim?.pause()
        sphereCollisionShapeScaleAnim?.pause()
    }

    fun resumeExecution() {

        moveAnim?.resume()
        spinAnim?.resume()
        scaleAnim?.resume()
        sphereCollisionShapeScaleAnim?.resume()
    }

    private fun randomDura(): Long {

        return (ANIM_LENGTH_MIN + (rGen.nextFloat() * (ANIM_LENGTH_MAX - ANIM_LENGTH_MIN))).toLong()
    }

    private fun randomTarget(): Vector3 {

        val xSign = if (rGen.nextBoolean()) 1 else -1

        val x = rGen.nextFloat() * xMaxAbs* xSign
        val y = yMin + rGen.nextFloat() * (yMax - yMin)
        return Vector3(x, y, -1.0f) // could randomize z-value as well, I guess
    }

    // used for making looping, predictable movement patterns
    private inner class DurationPattern(vararg durations: Long) {

        private val durationArray = durations.toTypedArray()
        private var index = 0
        private val maxIndex = durationArray.size - 1

        fun current(): Long {

            return durationArray[index]
        }

        fun next(): Long {

            index++
            if (index > maxIndex) {
                index = 0
            }
            return durationArray[index]
        }

    } // DurationPattern

} // AI