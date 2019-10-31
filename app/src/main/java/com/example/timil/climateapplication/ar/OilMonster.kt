package com.example.timil.climateapplication.ar

import android.util.Log
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable

/**
 * A monster that looks like an oil drop and will divide in smaller drops on hitting it.
 * This monster is different in the spawn mechanic: it's placed on a surface and can be
 * circled around by the player.
 * @author Ville Lohkovuori
 * */

class OilMonster : Monster() {

    override val pointsValueOnDeath = 10

    override val maxHitPoints = 5

    override var hitPoints = 5
        set(value) {
            field = value
            Log.d("HUUH", "hp: $hitPoints")
            checkForDeath()
        }

    override var monsterAI = AI.create(this, AIType.BOUNCING)

    companion object {

        // set on app start from Static.kt
        lateinit var monsterRenderable: ModelRenderable

        // factory method (this alone should be used for creation!)
        fun create(anchorNode: AnchorNode): OilMonster {

            return OilMonster().apply {
                setPosition(0f, 0f, -0.3f)
                renderable = monsterRenderable
                setParent(anchorNode)
            }.also {
                it.monsterAI.execute()
            }
        } // create
    } // companion object

} // OilMonster