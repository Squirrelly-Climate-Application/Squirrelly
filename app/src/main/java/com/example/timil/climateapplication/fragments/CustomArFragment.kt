package com.example.timil.climateapplication.fragments

import android.graphics.Point
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import com.example.timil.climateapplication.ArActivity
import com.example.timil.climateapplication.R
import com.example.timil.climateapplication.ar.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.fragment_custom_ar.*
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.hypot

/**
 * Fragment that enables placing and manipulating AR objects in the world space.
 * @author Ville Lohkovuori
 * */

// NOTE: strictly speaking, Fragments should contain only view logic; ViewModels should be used for
// things like the hit logic in this class. but since the app is simple enough and ViewModels are
// hard af to use, let's just do it this way for now :D

class CustomArFragment : ArFragment() {

    // the game score
    private var score = 0.0f

    //TODO: the throw number should arrive from the bundle
    // note: these could be in a 'Game' class, but for now I don't think that's necessary
    private var numOfThrows = 0
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

    private val wind = Wind.create()

    private var setupDone = false

    var startDistanceY = 0f
    var startDistanceX = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

        // inflater.inflate(R.layout.fragment_custom_ar, container, false)
/*
        Log.d("HUUH", "wind x: " + wind.xComp)
        Log.d("HUUH", "wind y: " + wind.yComp)
        Log.d("HUUH", "wind force: " + wind.force)
        Log.d("HUUH", "wind angle: " + wind.radAngle) */

        val display = activity!!.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
        screenCenter = Point(screenWidth / 2, screenHeight / 2)

        disablePlaneDetection()
        arSceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->

            onPeekTouchDetect(hitTestResult, motionEvent)
        }

        // create the first nut and the monster
        monsterNode = PlasticMonster.create(arSceneView.scene.camera)
        Projectile.create(arSceneView.scene.camera, onThrowAnimEndCallbackHolder)

        numOfThrows = (activity as ArActivity).DEFAULT_THROWS

        //TODO: we may use the plane renderer for placing non-camera-locked monsters

        // return inflater.inflate(R.layout.fragment_custom_ar_2, container, false)
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
        // (activity as AppCompatActivity).supportActionBar!!.hide()

        if (!setupDone && isAdded) {

            (activity as ArActivity).apply {

                // why tf the views can't be found from the fragment is anyone's guess -.-
                setHitPoints(monsterNode!!.hitPoints)
                setWindX(wind.xComp)
                setWindY(wind.yComp)
            }
            setupDone = true
        } // if
    } // onResume
/*
    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar!!.show()
    } */

    /* // could use this instead of the listener and custom function ??
    override fun onPeekTouch(hitTestResult: HitTestResult?, motionEvent: MotionEvent?) {
        super.onPeekTouch(hitTestResult, motionEvent)
        Log.d("HUUH", "peekTouch detected")
    } */

    private fun onPeekTouchDetect(hitTestResult: HitTestResult, motionEvent: MotionEvent) {

        // do not detect hits to already thrown projectiles
        if (hitTestResult.node?.name == Projectile.THROWN_PROJECTILE_NAME) return

        val hitNode = hitTestResult.node

        if (!hitProj && hitNode is Projectile) {
            startDistanceY = motionEvent.rawY
            startDistanceX = motionEvent.rawX

            hitProj = true
            projNode = hitNode
        }
        if (startDistanceY > 0f && startDistanceX > 0f) {
            (activity as ArActivity).setPower((hypot( abs(startDistanceX - motionEvent.rawX), abs(startDistanceY - motionEvent.rawY))).toInt())
        }

        if (hitProj && motionEvent.actionMasked == MotionEvent.ACTION_UP) {

            Log.d("HUUH", "scr height: $screenHeight")

            val upwardSwipe = motionEvent.y < 0.8333 * screenHeight

            if (upwardSwipe) {

                //TODO: it's not easy to get rid of the remaining hardcoded factors, but it should be done.
                // their values depend on the relationship between the two coordinate systems (ARCore's and regular screen touch events'),
                // which is extremely nebulous at the best of times
                val scaledX = ((motionEvent.x - screenCenter.x) * 0.0006481481f) * hitScaleFactor + wind.xComp

                val localY = projNode!!.localPosition.y
                val tempY = ((screenCenter.y - motionEvent.y) * 0.0007142857f) // gives -0.35 min value (1450 is the lowest y screen point atm)
                val scaledY = localY + (abs(localY) + tempY) * hitScaleFactor + wind.yComp // local y coordinate of the start location = -0.35f

                val target = Vector3(scaledX, scaledY, -1.0f) // sink it down a little bit, with a lower final z-value

                // store the end point of the animation that is launched directly below
                actualScaledHitPoint = convertMEventCoordsToScaledScreenTargetPoint(motionEvent.x, motionEvent.y)

                projNode!!.launch(target) // triggers the animation; at the end of it comes the hit check to the monster
                startDistanceY = 0f
                startDistanceX = 0f
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
                score += 2
                (activity as ArActivity).setHitPoints(monsterNode!!.hitPoints)
                Log.d("HUUH", "hit monster!")

                // this looks a bit ugly, but we avoid making another interface to communicate with the fragment
                if (!monsterNode!!.isAlive) {

                    endGame(true)
                }
            } // if Monster
            numOfThrows-- // the game ends if it goes to zero
            score-- // used throw = -1 score
            (activity as ArActivity).setScore(score)
            (activity as ArActivity).setThrows(numOfThrows)
            // Log.d("HUUH", "numOfThrows after decrease: $numOfThrows")
            projNode?.dispose() // delete the old nut
            projNode = null
            Projectile.create(arSceneView.scene.camera, this) // immediately create a new nut
            (activity as ArActivity).setPower(0)
        } // onDropAnimEnd
    } // onThrowAnimEndCallbackHolder

    private fun endGame(monsterDead: Boolean) {

        if (monsterDead) score *= 1.5f
        (activity as ArActivity).setScore(score)

        Log.d("HUUH", "final points: $score") // always round upwards
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

    // for communicating with text views through ArActivity (this fragment can't see them)
    interface FragmentCommunicator {

        fun setScore(score: Float)
        fun setHitPoints(hp: Int)
        fun setThrows(throws: Int)
        fun setWindX(windX: Float)
        fun setWindY(windY: Float)
        fun setPower(power: Int)
    }

    // maybe shorten its name, ehh
    private fun convertMEventCoordsToScaledScreenTargetPoint(x: Float, y: Float): Point {

        val alterXBy = wind.xComp * screenWidth / 0.55f // experimental constant; only works on the Galaxy S7!
        // Log.d("HUUH", "alter x by: $alterXBy")

        val scaledX = screenCenter.x + (x - screenCenter.x) * hitScaleFactor + alterXBy // valid only for the Galaxy S7...

        val alterYBy = wind.yComp * screenHeight / 3.6f
        Log.d("HUUH", "subtract from y: $alterYBy")

        // reverse axis (from 1920 to 0) and zero-point off-center
        val scaledY = screenHeight - (abs(y - screenHeight)) * hitScaleFactor // - alterYBy // ditto...
        return Point(scaledX.toInt(), scaledY.toInt())
    } // convertMEventCoordsToScaledScreenTargetPoint

} // CustomArFragment