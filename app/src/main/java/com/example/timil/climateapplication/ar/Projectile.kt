package com.example.timil.climateapplication.ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class Projectile(private val cameraNode: Node): WorldEntity() {

    companion object {

        // set on app start from Static.kt
        lateinit var projRenderable: ModelRenderable

        private const val windEffect = 0.0f // the wind is always from the left or right atm
        private const val firstAnimDura = 1000L
        private const val secondAnimDura = 1000L

        // factory method (this alone should be used for creation!)
        fun create(cameraNode: Node): Projectile {

            return Projectile(cameraNode).apply {
                setPosition(0f, -0.35f, -0.9f) // low center position
                localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 40f) // so that it looks good
                name = Static.DEFAULT_PROJECTILE_NAME
                renderable = projRenderable
                setParent(cameraNode)
            }
        } // create
    } // companion object

    private val onAnimEndCallbackHolder = object : IonAnimEnd {

        override fun onRiseAnimEnd() {

            // Log.d("HUUH", "localPos after rise: $localPosition")
        }

        override fun onDropAnimEnd() {

            // Log.d("HUUH", "localPos at end: $localPosition")
            // Log.d("HUUH", "world pos: $worldPosition")
            dispose() // delete the old nut
            create(cameraNode) // immediately create a new nut
        }
    } // onAnimEndCallbackHolder

    interface IonAnimEnd {

        fun onRiseAnimEnd()
        fun onDropAnimEnd()
    }

    // can't name it 'throw' because it's a reserved keyword
    fun launch(throwTarget: Vector3) {

        // name = "thrownNut" // should be unnecessary now

        val throwStr = throwStrength(throwTarget) // it should be used somehow... figure out the proper launch speed equation!
        Log.d("HUUH", "throwStr: $throwStr")

        val finalTarget = Vector3(throwTarget)
        Log.d("HUUH", "orig. throwTarget: $throwTarget")

        val intermediateTarget = Vector3(throwTarget)
        intermediateTarget.apply {

            x = throwTarget.x * 0.5f // 50 %
            x += windEffect * 0.5f // it needs to be scaled as well
            z = -0.8f
            y = localPosition.y + (abs(localPosition.y) + throwTarget.y) * 0.5f

            // if (y >= 0f) y * 0.5f else (y + localPosition.y) / 2 // only works with 50 % !!
        }
        finalTarget.x += windEffect // gets the full effect
        Log.d("HUUH", "intermediateTarget: $intermediateTarget")

        playLaunchAnimation(intermediateTarget, finalTarget)
    } // launch

    //TODO: optimize this somehow... creating new animations for every throw is very bad!
    private fun playLaunchAnimation(firstTarget: Vector3, endTarget: Vector3) {

        val risingAnim = AnimationFactory.linearMoveAnim(
            this,
            firstAnimDura,
            localPosition,
            firstTarget,
            object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?) {

                    this@Projectile.onAnimEndCallbackHolder.onRiseAnimEnd()
                    animation?.removeAllListeners()
                }
            }) // risingAnim

        val droppingAnim = AnimationFactory.linearMoveAnim(
            this,
            secondAnimDura,
            firstTarget,
            endTarget,
            object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?) {

                    this@Projectile.onAnimEndCallbackHolder.onDropAnimEnd()
                    animation?.removeAllListeners()
                }
            }) // droppingAnim

        val randomSpinQuaternion = Static.randomizedQuaternion()

        val spinAnim = AnimationFactory.spinAnim(this, firstAnimDura + secondAnimDura, randomSpinQuaternion)

        AnimatorSet().apply {
            play(risingAnim).before(droppingAnim)
            play(spinAnim)
            start()
        }
    } // playLaunchAnimation

    private fun throwStrength(throwTarget: Vector3): Double {

        val x = throwTarget.x.toDouble()
        val y = throwTarget.y.toDouble()

        return sqrt(x.pow(2.0) + y.pow(2.0))
    }

} // Projectile