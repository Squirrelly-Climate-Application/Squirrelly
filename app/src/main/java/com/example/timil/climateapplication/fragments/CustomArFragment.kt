package com.example.timil.climateapplication.fragments

import android.graphics.Point
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.example.timil.climateapplication.ar.Monster
import com.example.timil.climateapplication.ar.PlasticMonster
import com.example.timil.climateapplication.ar.Projectile
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.PlaneRenderer
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.ArFragment
import kotlin.math.abs

/**
 * Fragment that enables placing and manipulating ar objects in the world space.
 * @author Ville Lohkovuori
 * */

class CustomArFragment : ArFragment() {

    // for tracking gesture (swipe) hits to the thrown projectile (acorn)
    private var hitProj = false
    private var projNode: Projectile? = null

    private var hitMonster = false
    private var monsterNode: Monster? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        disablePlaneDetection()
        arSceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->

            onPeekTouchDetect(hitTestResult, motionEvent)
        }

        PlasticMonster.create(arSceneView.scene.camera)
        Projectile.create(arSceneView.scene.camera)

        //TODO: we may use the plane renderer for placing non-camera-locked monsters

        // return inflater.inflate(R.layout.fragment_custom_ar, container, false)
/*
        // set plane renderer to red
        val sampler = Texture.Sampler.builder()
            .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
            .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
            .build()

        Texture.builder()
            .setSource(activity, R.drawable.plane_texture)
            .setSampler(sampler)
            .build()
            .thenAccept { texture ->
                arSceneView
                    .planeRenderer.material.thenAccept { material -> material.setTexture(
                    PlaneRenderer.MATERIAL_TEXTURE, texture) }
            } */

        return view
    } // onCreateView

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar!!.hide()
    }

    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.show()
    }

    private fun onPeekTouchDetect(hitTestResult: HitTestResult, motionEvent: MotionEvent) {

        // val hitEntityName = hitTestResult.node?.name ?: "noHit"
        val hitNode = hitTestResult.node

        if (!hitProj && hitNode is Projectile) {

            hitProj = true
            projNode = hitNode
        }

        if (hitProj && motionEvent.actionMasked == MotionEvent.ACTION_UP) {

            val upwardSwipe = motionEvent.y < 1600

            if (upwardSwipe) {

                val scaledX = ((motionEvent.x - 525) * 0.0006842105f) // * 1.3f
                val tempY = ((950 - motionEvent.y) * 0.0006315789f)

                // should work for both positive and negative y values
                val scaledY = -0.35f + (abs(-0.35f) + tempY) * 1.3f  // -0.35f = local y coordinate of the start location

                val target = Vector3(scaledX, scaledY, -1.0f) // sink it down a little bit, with a lower final z-value
                // Log.d("HUUH", "target: " + target)

                projNode!!.launch(target)

                // val hitTestResult2 = arFragment.arSceneView.scene.hitTest(screenCenterMEvent)

                // val monsterHitPoint = Point(motionEvent.x, motionEvent.y) // so that we can hit it on the z-level
                // obtainFakeMotionEvent(monsterHitTarget)

                if (!hitMonster && hitProj && hitNode is Monster) {

                    hitMonster = true
                    monsterNode = hitNode
                    Log.d("HUUH", "hit monster!")
                    /*
                    val x = monsterNode!!.worldPosition.x
                    val y = monsterNode!!.worldPosition.y
                    Log.d("HUUH", "x: $x")
                    Log.d("HUUH", "y: $y") */
                }
            } // if

            hitMonster = false
            monsterNode = null
            hitProj = false
            projNode = null
        } // if
    } // onPeekTouchDetect

    // creates a 'fake' MotionEvent that 'touches' a given point
    private fun obtainFakeMotionEvent(point: Point): MotionEvent {

        // val screenCenter = android.graphics.Point(findViewById<View>(android.R.id.content).width / 2, findViewById<View>(android.R.id.content).height / 2)

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis() + 100
        val x = point.x.toFloat()
        val y = point.y.toFloat()

        val metaState = 0
        return MotionEvent.obtain(
            downTime,
            eventTime,
            MotionEvent.ACTION_UP,
            x,
            y,
            metaState)
    } // end obtainFakeMotionEvent

    // disable the plane detection once a plane has been chosen
    private fun disablePlaneDetection() {

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
    }

    private fun enablePlaneDetection() {
        planeDiscoveryController.show()
        arSceneView.planeRenderer.isEnabled = true
    }

} // CustomArFragment