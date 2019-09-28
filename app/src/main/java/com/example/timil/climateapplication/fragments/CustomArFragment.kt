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

    /* // could use this instead of the listener and custom function ??
    override fun onPeekTouch(hitTestResult: HitTestResult?, motionEvent: MotionEvent?) {
        super.onPeekTouch(hitTestResult, motionEvent)
        Log.d("HUUH", "peekTouch detected")
    } */

    private fun onPeekTouchDetect(hitTestResult: HitTestResult, motionEvent: MotionEvent) {

        // val hitEntityName = hitTestResult.node?.name ?: "noHit"
        val hitNode = hitTestResult.node

        if (!hitProj && hitNode is Projectile) {

            hitProj = true
            projNode = hitNode
        }

        if (hitProj && motionEvent.actionMasked == MotionEvent.ACTION_UP) {

            Log.d("HUUH", "motionEvent y: " + motionEvent.y)

            val upwardSwipe = motionEvent.y < 1600

            if (upwardSwipe) {

                val scaledX = ((motionEvent.x - 540) * 0.0006842105f) * 1.3f

                // should be correct now...
                val tempY = ((960 - motionEvent.y) * 0.0006315789f)
                val scaledY = -0.35f + (abs(-0.35f) + tempY) * 1.3f  // -0.35f = local y coordinate of the start location

                val target = Vector3(scaledX, scaledY, -1.0f) // sink it down a little bit, with a lower final z-value

                projNode!!.launch(target) // it's only an animation; the actual hit logic is directly below

                val actualScaledHitPoint = convertMEventCoordsToScaledScreenTargetPoint(motionEvent.x, motionEvent.y, 1.3f)

                // we'll 'touch' the scaled hit point (30 % further than the finger swipe's end point)
                val actualHitTestMEvent = obtainMotionEvent(actualScaledHitPoint)
                val actualHitTestResult = arSceneView.scene.hitTest(actualHitTestMEvent)
                val actuallyHitNode = actualHitTestResult.node

                if (!hitMonster && hitProj && actuallyHitNode is Monster) {

                    hitMonster = true
                    monsterNode = actuallyHitNode
                    Log.d("HUUH", "hit monster!")
                }
            } // if upwardSwipe
            hitMonster = false
            monsterNode = null
            hitProj = false
            projNode = null
        } // if real MotionEvent == UP event
    } // onPeekTouchDetect

    // creates a 'fake' MotionEvent that 'touches' a given point
    private fun obtainMotionEvent(point: Point): MotionEvent {

        Log.d("HUUH", "fake x: " + point.x)
        Log.d("HUUH", "fake y: " + point.y)

        val screenCenter = Point(activity!!.findViewById<View>(android.R.id.content).width / 2, activity!!.findViewById<View>(android.R.id.content).height / 2)
        // Log.d("HUUH", "screencenter x: " + screenCenter.x) // 540
        // Log.d("HUUH", "screencenter y: " + screenCenter.y) // 960

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
    } // end obtainMotionEvent

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

    private fun convertMEventCoordsToScaledScreenTargetPoint(x: Float, y: Float, scaleFactor: Float): Point {

        val scaledX = 540 + (x - 540) * scaleFactor
        val scaledY = 1920 - (abs(y - 1920)) * scaleFactor // negative axis (from 1900 to 0)
        return Point(scaledX.toInt(), scaledY.toInt())
    }

} // CustomArFragment