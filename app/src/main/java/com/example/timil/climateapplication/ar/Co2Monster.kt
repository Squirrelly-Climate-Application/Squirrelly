package com.example.timil.climateapplication.ar

import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable

/**
 * A type of monster that looks like a morphing cloud.
 * @author Ville Lohkovuori
 * */

class Co2Monster: Monster() {

    override val pointsValueOnDeath = 10

    override val maxHitPoints = 5

    override var hitPoints = 5
        set(value) {
            field = value
            Log.d("HUUH", "hp: $hitPoints")
            checkForDeath()
        }

    companion object {

        // set on app start from Static.kt
        lateinit var monsterRenderable: ModelRenderable

        // factory method (this alone should be used for creation!)
        fun create(cameraNode: Node): Co2Monster {

            return Co2Monster().apply {
                setPosition(0f, 0.05f, -1.0f) // high center position
                // localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 20f)
                name = Static.CO2_MONSTER_NAME
                renderable = monsterRenderable
                setParent(cameraNode)
            }.also {
                // the AI must be created here in order for node.renderable not to be null
                it.monsterAI = AI.create(it, AIType.MORPHING)
                it.monsterAI.execute()
            }
        } // create
    } // companion object

    // auto-called when hp reaches 0
    override fun onDeath() {
        super.onDeath() // destroys the visual monster model
        monsterAI.terminate() // destroys the AI
        Log.d("HUUH", "monster is dead!")
    }

} // Co2Monster