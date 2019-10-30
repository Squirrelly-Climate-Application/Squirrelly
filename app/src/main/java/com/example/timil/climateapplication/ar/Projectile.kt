package com.example.timil.climateapplication.ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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

    // affects animation logic in ArActivity
    var isThrown = false

    companion object {

        // NOTE: changing the z-value is likely to screw up the hit detection!!!
        // possibly the y-value as well
        private val DEFAULT_POSITION = Vector3(0f, -0.35f, -0.9f) // low center position
        private val DEFAULT_ROTATION = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 40f) // not very necessary atm

        // set on app start from Static.kt
        lateinit var projRenderable: ModelRenderable

        private const val FIRST_ANIM_DURA = 1000L //TODO: make them depend on the throw strength somehow
        private const val SECOND_ANIM_DURA = 1000L

        // in order to be able to pause the anims, we need this reference
        private var anims: AnimatorSet? = null

        // factory method (this alone should be used for creation!)
        fun create(cameraNode: Node, obs: IonThrowAnimEndListener): Projectile {

            return Projectile(obs).apply {
                localPosition = DEFAULT_POSITION
                localRotation = DEFAULT_ROTATION
                renderable = projRenderable
                setParent(cameraNode)
            }
        } // create

    } // companion object

    // for communicating with the AR activity
    interface IonThrowAnimEndListener {

        fun onRiseAnimEnd()
        fun onDropAnimEnd()
    }

    // can't name it 'throw' because it's a reserved keyword
    fun launch(throwTarget: Vector3) {

        isThrown = true

        val finalTarget = Vector3(throwTarget)
        // Log.d("HUUH", "orig. throwTarget: $throwTarget")

        val intermediateTarget = Vector3(throwTarget)
        intermediateTarget.apply {

            x = throwTarget.x * 0.5f // 50 %
            z = RISE_ANIM_Z_TARGET
            y = localPosition.y + (abs(localPosition.y) + throwTarget.y) * 0.5f
        }

        playLaunchAnimation(intermediateTarget, finalTarget)
    } // launch

    //TODO: optimize this somehow... creating new animations for every throw is very bad!
    private fun playLaunchAnimation(firstTarget: Vector3, endTarget: Vector3) {

        val risingAnim = AnimationFactory.linearMoveAnim(
            this,
            FIRST_ANIM_DURA,
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
            SECOND_ANIM_DURA,
            firstTarget,
            endTarget,
            object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?) {

                    observer.onDropAnimEnd()
                    animation?.removeAllListeners()
                }
            }) // droppingAnim

        val randomSpinQuaternion = Static.randomizedQuaternion(90f)

        val spinAnim = AnimationFactory.spinAnim(this, FIRST_ANIM_DURA + SECOND_ANIM_DURA, randomSpinQuaternion)

        anims = AnimatorSet().apply {
            play(risingAnim).before(droppingAnim)
            play(spinAnim)
            start()
        }
    } // playLaunchAnimation

    fun pauseAnimations() {

        anims?.pause()
    }

    fun resumeAnimations() {

        anims?.resume()
    }

} // Projectile