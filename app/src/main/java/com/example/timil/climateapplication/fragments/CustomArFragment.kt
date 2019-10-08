package com.example.timil.climateapplication.fragments

import android.graphics.Point
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import com.example.timil.climateapplication.ArActivity
import com.example.timil.climateapplication.ar.*
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import kotlin.math.abs
import kotlin.math.hypot

/**
 * Fragment that enables placing and manipulating AR objects in the world space.
 * @author Ville Lohkovuori
 * */

class CustomArFragment : ArFragment() {

    // for tracking gesture (swipe) hits to the thrown projectile (acorn)
    private var hitProj = false
    private var projNode: Projectile? = null

    private var hitMonster = false
    private var monsterNode: Monster? = null

    // this can be here as long as the wind never changes
    private val wind = Wind.create()

    // we need this because the nuts hit a bit further than the end of the finger swipe
    private var actualScaledHitPoint: Point? = null

    // the factor that the distance of the finger swipe is multiplied by
    private val hitScaleFactor = 1.3f

    // to make the coordinate scaling work for all phone models (hopefully...)
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenCenter = Point(0, 0)

    // for animating the throw strength indicator bar
    //TODO: use also for throw strength in Projectile (if needed)
    private var startDistanceY = 0f
    private var startDistanceX = 0f

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)

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

        return view
    } // onCreateView

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as ArActivity).apply {
            setUIHitPoints(monsterNode?.hitPoints ?: 0)
            setUIWindX(wind.xComp)
            setUIWindY(wind.yComp)
        }
    } // onActivityCreated

    /* // could use this instead of the listener and custom function ??
    override fun onPeekTouch(hitTestResult: HitTestResult?, motionEvent: MotionEvent?) {
        super.onPeekTouch(hitTestResult, motionEvent)
        Log.d("HUUH", "peekTouch detected")
    } */

    private fun onPeekTouchDetect(hitTestResult: HitTestResult, motionEvent: MotionEvent) {

        if ((activity as ArActivity).gamePaused) return

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
            (activity as ArActivity).setUIPower((hypot( abs(startDistanceX - motionEvent.rawX), abs(startDistanceY - motionEvent.rawY))).toInt())
        }

        if (hitProj && motionEvent.actionMasked == MotionEvent.ACTION_UP) {

            // Log.d("HUUH", "scr height: $screenHeight")

            val upwardSwipe = motionEvent.rawY < 0.8333 * screenHeight

            if (upwardSwipe) {

                //TODO: it's not easy to get rid of the remaining hardcoded factors, but it should be done.
                // their values depend on the relationship between the two coordinate systems (ARCore's and regular screen touch events'),
                // which is extremely nebulous at the best of times
                val scaledX = ((motionEvent.rawX - screenCenter.x) * 0.0006481481f) * hitScaleFactor + wind.xComp

                val localY = projNode!!.localPosition.y
                val tempY = ((screenCenter.y - motionEvent.rawY) * 0.0007142857f) // gives -0.35 min value (1450 is the lowest y screen point atm)
                val scaledY = localY + (abs(localY) + tempY) * hitScaleFactor + wind.yComp // local y coordinate of the start location = -0.35f

                val target = Vector3(scaledX, scaledY, -1.0f) // sink it down a little bit, with a lower final z-value

                // store the end point of the animation that is launched directly below
                actualScaledHitPoint = convertMEventCoordsToScaledScreenTargetPoint(motionEvent.rawX, motionEvent.rawY)

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

            // these shenanigans are needed because the hit detection should only happen once the
            // throwing animation has finished

            // we'll 'touch' the scaled hit point (30 % further than the finger swipe's end point)
            val actualHitTestMEvent = obtainMotionEvent(actualScaledHitPoint!!) // it always exists if the animation is playing
            val actualHitTestResult = arSceneView.scene.hitTest(actualHitTestMEvent)
            val actuallyHitNode = actualHitTestResult.node

            if (actuallyHitNode is Monster) {

                hitMonster = true
                monsterNode!!.damage(1)
                (activity as ArActivity).score += 2 // each hit is worth 2 points
                (activity as ArActivity).setUIHitPoints(monsterNode!!.hitPoints)
                Log.d("HUUH", "hit monster!")

                // should probably make an interface to communicate with the fragment
                if (!monsterNode!!.isAlive) {

                    (activity as ArActivity).endGame(true)
                }
            } // if Monster
            (activity as ArActivity).numOfThrows-- // the game ends if it goes to zero
            (activity as ArActivity).score-- // used throw = -1 score
            // Log.d("HUUH", "numOfThrows after decrease: $numOfThrows")
            projNode?.dispose() // delete the old nut
            projNode = null
            Projectile.create(arSceneView.scene.camera, this) // immediately create a new nut
            (activity as ArActivity).setUIPower(0)
        } // onDropAnimEnd
    } // onThrowAnimEndCallbackHolder

    // called from the ArActivity (which can 'see' the pause button and manages the game state)
    fun pauseGame() {

        projNode?.pauseAnimations()
        monsterNode?.monsterAI?.pauseExecution()
    }

    // ditto
    fun resumeGame() {

        projNode?.resumeAnimations()
        monsterNode?.monsterAI?.resumeExecution()
    }

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

        val alterXBy = wind.xComp * screenWidth / 0.55f // experimental constant; only works on the Galaxy S7!
        // Log.d("HUUH", "alter x by: $alterXBy")

        val scaledX = screenCenter.x + (x - screenCenter.x) * hitScaleFactor + alterXBy // valid only for the Galaxy S7...

        val alterYBy = wind.yComp * screenHeight / 3.6f
        // Log.d("HUUH", "subtract from y: $alterYBy")

        // reverse axis (from 1920 to 0) and zero-point off-center
        val scaledY = screenHeight - (abs(y - screenHeight)) * hitScaleFactor // - alterYBy // ditto...
        return Point(scaledX.toInt(), scaledY.toInt())
    } // convertMEventCoordsToScaledScreenTargetPoint

} // CustomArFragment