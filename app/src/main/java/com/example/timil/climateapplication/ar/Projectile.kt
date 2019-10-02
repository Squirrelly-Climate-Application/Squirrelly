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

/**
 * A class for the thrown projectile (acorn in the final app).
 * @author Ville Lohkovuori
 * */

private const val RISE_ANIM_Z_TARGET = -0.8f

class Projectile(private val observer: IonThrowAnimEndListener): WorldEntity() {

    companion object {

        const val THROWN_PROJECTILE_NAME = "thrown"

        private val DEFAULT_POSITION = Vector3(0f, -0.35f, -0.9f) // low center position
        private val DEFAULT_ROTATION = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 40f) // not very necessary atm

        // set on app start from Static.kt
        lateinit var projRenderable: ModelRenderable

        private const val firstAnimDura = 1000L
        private const val secondAnimDura = 1000L

        // factory method (this alone should be used for creation!)
        fun create(cameraNode: Node, obs: IonThrowAnimEndListener): Projectile {

            return Projectile(obs).apply {
                localPosition = DEFAULT_POSITION
                localRotation = DEFAULT_ROTATION
                name = Static.DEFAULT_PROJECTILE_NAME
                renderable = projRenderable
                setParent(cameraNode)
            }
        } // create
    } // companion object

    // for communicating with the AR fragment
    interface IonThrowAnimEndListener {

        fun onRiseAnimEnd()
        fun onDropAnimEnd()
    }

    // can't name it 'throw' because it's a reserved keyword
    fun launch(throwTarget: Vector3) {

        // to prevent hit detection to the thrown nut (in CustomArFragment)
        name = THROWN_PROJECTILE_NAME

        val throwStr = throwStrength(throwTarget) // it should be used somehow... figure out the proper launch speed equation!
        // Log.d("HUUH", "throwStr: $throwStr")

        val finalTarget = Vector3(throwTarget)
        // Log.d("HUUH", "orig. throwTarget: $throwTarget")

        val intermediateTarget = Vector3(throwTarget)
        intermediateTarget.apply {

            x = throwTarget.x * 0.5f // 50 %
            // x += wind.xComp * 0.5f // it needs to be scaled as well
            z = RISE_ANIM_Z_TARGET
            y = localPosition.y + (abs(localPosition.y) + throwTarget.y) * 0.5f
            // y += wind.yComp * 0.5f
        }
        // finalTarget.x += wind.xComp // gets the full effect
        // finalTarget.y += wind.yComp
        // Log.d("HUUH", "intermediateTarget: $intermediateTarget")

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

                    observer.onRiseAnimEnd()
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

                    observer.onDropAnimEnd()
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