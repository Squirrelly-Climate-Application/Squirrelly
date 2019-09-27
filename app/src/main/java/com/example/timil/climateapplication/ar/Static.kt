package com.example.timil.climateapplication.ar

import android.content.Context
import android.net.Uri
import com.example.timil.climateapplication.ar.PlasticMonster
import com.example.timil.climateapplication.ar.Projectile
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.collision.Sphere
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable

/**
* An object for holding static resources (the 3d models), and for storing some
* utility functions.
 * @author Ville Lohkovuori
**/

object Static {

    // these are not strictly needed atm, but might be used in the near future
    const val DEFAULT_PROJECTILE_NAME = "nut"
    const val PLASTIC_MONSTER_NAME = "plasticMonster"

    fun load3dModelResources(context: Context) {

        ModelRenderable.builder()
            .setSource(context, Uri.parse("nuca1.sfb"))
            .build().thenAccept {
                Projectile.projRenderable = it
                Projectile.projRenderable.apply {

                    isShadowReceiver = false
                    isShadowCaster = false
                    collisionShape = Sphere(0.04f, Vector3(0f, 0.1f, 0f))
                }
            } // thenAccept

        ModelRenderable.builder()
            .setSource(context, Uri.parse("Bottle A.sfb"))
            .build().thenAccept {
                PlasticMonster.monsterRenderable = it
                PlasticMonster.monsterRenderable.apply {

                    isShadowReceiver = false
                    isShadowCaster = false
                    collisionShape = Box(Vector3(0.18f, 0.36f, 0.18f), Vector3(0.02f, 0.18f, 0f))
                }
            } // thenAccept
    } // load3dModelResources

    // for randomly spinning things around (the acorns, etc)
    fun randomizedQuaternion(): Quaternion {

        val rndX = Math.random().toFloat() // between 0 and 1
        val rndY = Math.random().toFloat()
        val rndZ = Math.random().toFloat()
        val rndAngle = Math.random().toFloat() * 360 // first part could be removed, perhaps

        return Quaternion.axisAngle(Vector3(rndX, rndY, rndZ), rndAngle)
    }

} // Static