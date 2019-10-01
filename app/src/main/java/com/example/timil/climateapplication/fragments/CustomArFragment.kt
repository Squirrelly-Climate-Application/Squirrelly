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
import com.google.ar.sceneform.ux.ArFragment
import kotlin.math.abs
import kotlin.math.ceil

/**
 * Fragment that enables placing and manipulating ar objects in the world space.
 * @author Ville Lohkovuori
 * */

// NOTE: strictly speaking, Fragments should contain only view logic; ViewModels should be used for
// things like the hit logic in this class. but since the app is simple enough and ViewModels are
// hard af to use, let's just do it this way for now :D

private const val DEFAULT_THROWS = 5

class CustomArFragment : ArFragment() {

    //TODO: the throw number should arrive from the quiz part
    // note: these could be in a 'Game' class, but for now I don't think that's necessary
    private var numOfThrows = DEFAULT_THROWS
        set(value) {
            field = value
            if (value == 0) {
                endGame(false) // if the monster is dead, the game ends before this call
            }
        }
    private var usedThrows = 0 // not ideal, but things are a lot easier if this exists

    // for tracking gesture (swipe) hits to the thrown projectile (acorn)
    private var hitProj = false
    private var projNode: Projectile? = null

    private var hitMonster = false
    private var monsterNode: Monster? = null

    // we need this because the nuts hit a bit further than the end of the finger swipe
    private var actualScaledHitPoint: Point? = null

    // the factor that the distance of the finger swipe is multiplied by
    private val hitScaleFactor = 1.3f

    // to make the coordinate scaling work for all phone models (hopefully...)
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenCenter = Point(0, 0)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        screenWidth = activity!!.findViewById<View>(android.R.id.content)!!.width // the activity should always exist
        screenHeight = activity!!.findViewById<View>(android.R.id.content)!!.height
        screenCenter = Point(screenWidth / 2, screenHeight / 2)

        disablePlaneDetection()
        arSceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->

            onPeekTouchDetect(hitTestResult, motionEvent)
        }

        // create the first nut and the monster
        monsterNode = PlasticMonster.create(arSceneView.scene.camera)
        Projectile.create(arSceneView.scene.camera, onThrowAnimEndCallbackHolder)

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

            // Log.d("HUUH", "motionEvent y: " + motionEvent.y)

            val upwardSwipe = motionEvent.y < 0.8333 * screenHeight

            if (upwardSwipe) {

                //TODO: it's not easy to get rid of the remaining hardcoded factors, but it should be done.
                // their values depend on the relationship between the two coordinate systems (ARCore's and regular screen touch events'),
                // which is extremely nebulous at the best of times
                val scaledX = ((motionEvent.x - screenCenter.x) * 0.0006481481f) * hitScaleFactor

                val localY = projNode!!.localPosition.y
                val tempY = ((screenCenter.y - motionEvent.y) * 0.0007142857f) // gives -0.35 min value (1450 is the lowest y screen point atm)
                val scaledY = localY + (abs(localY) + tempY) * hitScaleFactor  // local y coordinate of the start location = -0.35f

                val target = Vector3(scaledX, scaledY, -1.0f) // sink it down a little bit, with a lower final z-value

                // store the end point of the animation that is launched directly below
                actualScaledHitPoint = convertMEventCoordsToScaledScreenTargetPoint(motionEvent.x, motionEvent.y)

                projNode!!.launch(target) // triggers the animation; at the end of it comes the hit check to the monster
            } // if upwardSwipe

            // hitMonster = false
            // monsterNode = null
            hitProj = false
        } // if real MotionEvent == UP event
    } // onPeekTouchDetect

    // for communicating with the thrown projectiles
    private val onThrowAnimEndCallbackHolder = object : Projectile.IonThrowAnimEndListener {

        override fun onRiseAnimEnd() {

            // Log.d("HUUH", "localPos after rise: $localPosition")
        }

        override fun onDropAnimEnd() {

            usedThrows++

            // these shenanigans are needed because the hit detection should only happen once the
            // throwing animation has finished

            // we'll 'touch' the scaled hit point (30 % further than the finger swipe's end point)
            val actualHitTestMEvent = obtainMotionEvent(actualScaledHitPoint!!) // it always exists if the animation is playing
            val actualHitTestResult = arSceneView.scene.hitTest(actualHitTestMEvent)
            val actuallyHitNode = actualHitTestResult.node

            // the two first checks should now be unnecessary
            if (/* !hitMonster && hitProj && */ actuallyHitNode is Monster) {

                hitMonster = true
                monsterNode!!.damage(1)
                Log.d("HUUH", "hit monster!")

                // this looks a bit ugly, but we avoid making another interface to communicate with the fragment
                if (!monsterNode!!.isAlive) {

                    endGame(true)
                }
            } // if Monster
            numOfThrows-- // the game ends if it goes to zero
            // Log.d("HUUH", "numOfThrows after decrease: $numOfThrows")
            projNode?.dispose() // delete the old nut
            projNode = null
            Projectile.create(arSceneView.scene.camera, this) // immediately create a new nut
        } // onDropAnimEnd
    } // onThrowAnimEndCallbackHolder

    private fun endGame(monsterDead: Boolean) {

        var finalPoints = 10.0 - usedThrows
        if (monsterDead) finalPoints *= 1.5

        Log.d("HUUH", "final points: " + ceil(finalPoints).toInt()) // always round upwards
        //TODO: move to the reward screen and send the points there
    } // endGame

    // creates a 'fake' MotionEvent that 'touches' a given screen point
    private fun obtainMotionEvent(point: Point): MotionEvent {

        // Log.d("HUUH", "fake x: " + point.x)
        // Log.d("HUUH", "fake y: " + point.y)

        // val screenCenter = Point(activity!!.findViewById<View>(android.R.id.content).width / 2, activity!!.findViewById<View>(android.R.id.content).height / 2)
        // Log.d("HUUH", "screencenter x: " + screenCenter.x) // 540 on S7
        // Log.d("HUUH", "screencenter y: " + screenCenter.y) // 960 on S7

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

    private fun disablePlaneDetection() {

        planeDiscoveryController.hide()
        planeDiscoveryController.setInstructionView(null)
        arSceneView.planeRenderer.isEnabled = false
    }

    private fun enablePlaneDetection() {
        planeDiscoveryController.show()
        arSceneView.planeRenderer.isEnabled = true
    }

    // maybe shorten its name, ehh
    private fun convertMEventCoordsToScaledScreenTargetPoint(x: Float, y: Float): Point {

        val scaledX = screenCenter.x + (x - screenCenter.x) * hitScaleFactor
        val scaledY = screenHeight - (abs(y - screenHeight)) * hitScaleFactor // reverse axis (from 1900 to 0) and zero point off-center
        return Point(scaledX.toInt(), scaledY.toInt())
    }

} // CustomArFragment