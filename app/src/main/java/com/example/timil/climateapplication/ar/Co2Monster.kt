package com.example.timil.climateapplication.ar

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.Light
import com.google.ar.sceneform.rendering.ModelRenderable

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
            Log.d("HUUH", "hp: $hitPoints")
            checkForDeath()
            playHitAnim()
        }

    companion object {

        // used for the light effect when the nuts hit the monster
        private val whiteLight = Light.builder(Light.Type.POINT)
            .setColor(Color(1f, 1f, 1f))
            .setIntensity(100000f)
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
                // it.monsterAI.execute()
            }
        } // create
    } // companion object

    private fun playHitAnim() {

        // move the cloud fast a little bit, so it looks like it has been 'pushed' by the projectile
        val moveX = Static.signedRandomFloatBetween(0.05f, 0.07f) // i.e., either -0.08 to -0.05 OR 0.05 to 0.08
        val moveY = Static.signedRandomFloatBetween(0.05f, 0.07f)
        val dura = Static.randomFloatBetween(300f, 500f).toLong()
        val moveAnim = AnimationFactory.linearMoveAnimNoEndListener(this, dura, localPosition, Vector3(localPosition.x + moveX, localPosition.y + moveY, localPosition.z))
        moveAnim.start()

        // make a light effect at the hit location
        val lightNode = EffectEntity()
        lightNode.setParent(this) // what else could it even be?
        lightNode.light = whiteLight
        lightNode.localPosition = Vector3(0f, 0f, 0.2f) // move the light towards the player, out of the cloud, so it can be seen
        val endListener = object : AnimatorListenerAdapter() {

            override fun onAnimationEnd(animation: Animator?) {

                lightNode.dispose()
                animation?.removeAllListeners()
            }
        }
        val onOffAnim = AnimationFactory.lightOnOffAnimation(lightNode.light!!, 500L, endListener)
        onOffAnim.start()

        //TODO: make small cloud objects spawn from the hit location, drift around and disappear (needs a new class)
    } // playHitAnim

} // Co2Monster