package com.example.timil.climateapplication.ar

import android.util.Log
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.collision.Sphere
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable

/**
 * A monster that looks like an oil drop and will divide in smaller drops on hitting it.
 * This monster is different in the spawn mechanic: it's placed on a surface and can be
 * circled around by the player.
 * @author Ville Lohkovuori
 * */

class OilMonster private constructor() : Monster() {

    // for the unique logic of having multiple monsters
    var isInitialMonster = true

    override val pointsValueOnDeath = 2

    // these must be 1 for the big oil monster as well, as they're vals...
    // things are a little rigid, as I originally envisioned only a single monster existing
    // at any one time
    override val maxHitPoints = 1

    override var hitPoints = 1
        set(value) {
            field = value
            Log.d("HUUH", "hp: $hitPoints")
            checkForDeath()
        }

    // override var monsterAI = AI.create(this, AIType.BOUNCING)

    companion object {

        // set on app start from Static.kt
        lateinit var monsterRenderable: ModelRenderable

        // factory method (this alone should be used for creation!)
        fun create(anchorNode: AnchorNode): OilMonster {

            return OilMonster().apply {
                localPosition = anchorNode.localPosition
                renderable = monsterRenderable
                setParent(anchorNode)
            }.also {
                it.monsterAI = AI.create(it, AIType.BOUNCING) // spawn it here to get the correct localPosition
                it.monsterAI.execute()
            }
        } // create

        // for creating the smaller OilMonsters (on destruction of their 'mother')
        fun createSmall(node: Node, pos: Vector3): OilMonster {

            Log.d("HUUH", "creating small monster at position: $pos")
            return OilMonster().apply {
                localPosition = pos
                renderable = monsterRenderable
                setParent(node)
                scaleSize(Static.randomFloatBetween(0.34f, 0.5f))
                isInitialMonster = false
            }.also {
                it.monsterAI = AI.create(it, AIType.BOUNCING) // spawn it here to get the correct localPosition
                it.monsterAI.execute()
            }
        } // createSmall
    } // companion object

    // for making smaller OilMonsters
    private fun scaleSize(scaleFactor: Float) {

        localScale = localScale.scaled(scaleFactor)
        // TODO: disabling this for now; figure out the correct scaling!
        // (renderable?.collisionShape as Sphere).radius *= scaleFactor
    }

} // OilMonster