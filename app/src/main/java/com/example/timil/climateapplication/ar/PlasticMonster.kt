package com.example.timil.climateapplication.ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable

/**
 * A type of monster that looks like a plastic bottle (for now).
 * @author Ville Lohkovuori
 * */

class PlasticMonster private constructor() : Monster() {

    override val pointsValueOnDeath = 10

    override val maxHitPoints = 5

    override var hitPoints = 5
        set(value) {
            field = value
            Log.d("HUUH", "hp: $hitPoints")
            checkForDeath()
            playHitAnim()
        }

    override var monsterAI = AI.create(this, AIType.BASIC)

    companion object {

        // set on app start from Static.kt
        lateinit var monsterRenderable: ModelRenderable

        // used for the light effect when the nuts hit the monster
        private val whiteLight = Light.builder(Light.Type.POINT)
            .setColor(Color(1f, 1f, 1f))
            .setIntensity(0f)
            .setFalloffRadius(200f)
            .setShadowCastingEnabled(false)
            .build()

        // factory method (this alone should be used for creation!)
        fun create(cameraNode: Node): PlasticMonster {

            return PlasticMonster().apply {
                setPosition(0f, 0.05f, -1.0f) // high center position
                localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 20f)
                renderable = monsterRenderable
                setParent(cameraNode)
            }.also {
                it.monsterAI.execute()
            }
        } // create
    } // companion object

    private fun playHitAnim() {

        // move the bottle fast a little bit, so it looks like it has been 'pushed' by the projectile
        val moveX = Static.signedRandomFloatBetween(0.05f, 0.065f) // i.e., either -0.07 to -0.05 OR 0.05 to 0.07
        val moveY = Static.signedRandomFloatBetween(0.05f, 0.065f)
        val dura = Static.randomFloatBetween(300f, 500f).toLong()
        val shakeAnim = AnimationFactory.linearMoveAnimNoEndListener(this, dura, localPosition, Vector3(localPosition.x + moveX, localPosition.y + moveY, localPosition.z))
        shakeAnim.start()

        // make a light effect at the hit location
        val lightNode = EffectEntity()
        lightNode.setParent(this)
        lightNode.light = whiteLight
        lightNode.localPosition = Vector3(0f, 0f, 0.28f) // move the light towards the player, out of the cloud, so it can be seen
        val lightEndListener = object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {

                lightNode.dispose()
                animation?.removeAllListeners()
            }
        }
        val onOffAnim = AnimationFactory.lightOnOffAnimation(lightNode.light!!, dura, 100000f, lightEndListener)
        onOffAnim.start()
    } // playHitAnim

} // PlasticMonster