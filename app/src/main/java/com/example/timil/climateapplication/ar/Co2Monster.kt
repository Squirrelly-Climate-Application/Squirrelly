package com.example.timil.climateapplication.ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlin.random.Random

/**
 * A type of monster that looks like a morphing cloud.
 * @author Ville Lohkovuori
 * */

class Co2Monster private constructor() : Monster() {

    override val pointsValueOnDeath = 10

    override val maxHitPoints = 5

    override var hitPoints = 5
        set(value) {
            field = value
            checkForDeath()
            playHitAnim() // putting this here is a bit risky in case we'd later add DOT effects, etc
        }

    companion object {

        // used for the light effect when the nuts hit the monster
        private val whiteLight = Light.builder(Light.Type.POINT)
            .setColor(Color(1f, 1f, 1f))
            .setIntensity(0f)
            .setFalloffRadius(200f)
            .setShadowCastingEnabled(false)
            .build()

        // set on app start from Static.kt
        lateinit var monsterRenderable: ModelRenderable

        // factory method (this alone should be used for creation!)
        fun create(cameraNode: Node): Co2Monster {

            return Co2Monster().apply {
                setPosition(0f, 0.05f, -1.0f) // high center position
                renderable = monsterRenderable
                setParent(cameraNode)
            }.also {
                // the AI must be created here in order for node.renderable not to be null
                it.monsterAI = AI.create(it, AIType.MORPHING)
                it.monsterAI.execute()
            }
        } // create
    } // companion object

    private fun playHitAnim() {

        // move the cloud fast a little bit, so it looks like it has been 'pushed' by the projectile
        val moveX = Static.signedRandomFloatBetween(0.05f, 0.07f) // i.e., either -0.07 to -0.05 OR 0.05 to 0.07
        val moveY = Static.signedRandomFloatBetween(0.05f, 0.07f)
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
        val onOffAnim = AnimationFactory.lightOnOffAnimation(lightNode.light!!, 500L, 100000f, lightEndListener)
        onOffAnim.start()

        // these should be spawned on a different thread to prevent choppiness, but it's too much work for too little gain
        val numOfClouds = Random.nextInt(1, 3) // inclusive at both ends
        for (i in 0..numOfClouds) {

            val cloud = EffectEntity()
            cloud.localPosition = localPosition
            cloud.renderable = monsterRenderable
            cloud.localScale = localScale.scaled(Static.randomFloatBetween(0.1f, 0.2f))
            cloud.setParent(parent) // i.e., the camera

            val animDura = Static.randomFloatBetween(1200f, 2000f).toLong()

            val endListener = object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator?) {

                    cloud.dispose()
                    animation?.removeAllListeners()
                }
            } // endListener

            val moveAnim = AnimationFactory.linearMoveAnim(cloud, animDura, cloud.localPosition, Static.uniformlyRandomizedPosition(cloud.localPosition, 0.2f), endListener)
            val spinAnim = AnimationFactory.spinAnim(cloud, animDura, Static.randomizedQuaternion())
            val scaleAnim = AnimationFactory.scaleAnim(cloud, animDura, Static.randomScale(0.4f, 0.7f))
            moveAnim.start()
            spinAnim.start()
            scaleAnim.start()
        } // for
    } // playHitAnim

} // Co2Monster