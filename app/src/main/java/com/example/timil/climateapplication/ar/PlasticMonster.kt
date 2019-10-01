package com.example.timil.climateapplication.ar

import android.util.Log
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable

/**
 * A type of monster that looks like a plastic bottle (for now).
 * @author Ville Lohkovuori
 * */

class PlasticMonster: Monster() {

    override var hitPoints = 5
        set(value) {
            field = value
            Log.d("HUUH", "hp: $hitPoints")
            checkForDeath()
        }

    override var monsterAI = AI.create(this, AIType.BASIC)

    companion object {

        // set on app start from Static.kt
        lateinit var monsterRenderable: ModelRenderable

        // factory method (this alone should be used for creation!)
        fun create(cameraNode: Node): PlasticMonster {

            return PlasticMonster().apply {
                setPosition(0f, 0.05f, -1.0f) // high center position
                localRotation = Quaternion.axisAngle(Vector3(1f, 0f, 0f), 20f) // so that it looks good
                name = Static.PLASTIC_MONSTER_NAME
                renderable = monsterRenderable
                setParent(cameraNode)
            }.also {
                it.monsterAI?.execute()
            }
        } // create
    } // companion object

    // auto-called when hp reaches 0
    override fun onDeath() {
        super.onDeath() // destroys the visual monster model
        monsterAI?.terminate() // destroys the AI
        Log.d("HUUH", "monster is dead!")
    }

} // PlasticMonster