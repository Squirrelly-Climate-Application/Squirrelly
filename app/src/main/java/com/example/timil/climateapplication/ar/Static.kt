package com.example.timil.climateapplication.ar

import android.content.Context
import android.net.Uri
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.collision.Sphere
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import kotlin.random.Random

/**
* An object for holding static resources (the 3d models), and for storing some
* utility functions.
 * @author Ville Lohkovuori
**/

object Static {

    private val rGen = Random(System.currentTimeMillis())

    fun load3dModelResources(context: Context) {

        // nut
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

        // bottle
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

        // cloud
        ModelRenderable.builder()
            .setSource(context, Uri.parse("model.sfb"))
            .build().thenAccept {
                Co2Monster.monsterRenderable = it
                Co2Monster.monsterRenderable.apply {

                    isShadowReceiver = false
                    isShadowCaster = false
                    collisionShape = Sphere(0.14f, Vector3(0f, 0.25f, 0f))
                }
            } // thenAccept
    } // load3dModelResources

    // for randomly spinning things around (the acorns, etc).
    fun randomizedQuaternion(baseAngle: Float = 180f): Quaternion {

        val rndX = rGen.nextInt(0,2).toFloat()
        val rndY = rGen.nextInt(0, 2).toFloat()
        val rndZ = rGen.nextInt(0, 2).toFloat()

        return Quaternion.axisAngle(Vector3(rndX, rndY, rndZ), baseAngle)
    }

    fun randomFloatBetween(min: Float, max: Float): Float {

        return min + rGen.nextFloat() * (max - min)
    }

} // Static