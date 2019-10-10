package com.example.timil.climateapplication

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import com.example.timil.climateapplication.ar.Monster
import com.example.timil.climateapplication.ar.PlasticMonster
import com.example.timil.climateapplication.ar.Projectile
import com.example.timil.climateapplication.ar.Wind
import com.example.timil.climateapplication.services.SoundService
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_ar.*
import kotlin.math.abs
import kotlin.math.hypot

/**
 * This Activity is only needed because the CustomArFragment's contained UI views cannot be accessed
 * directly from it (due to null object references).
 * @author Ville Lohkovuori, Leo Partanen
 * */

const val AR_FRAGMENT_TAG = "ARFragment"

private const val DEFAULT_THROWS = 5

// the percentage of screen space (from the top of the screen)
// where a projectile-touching finger swipe will have to end to be considered 'upwards'
private const val UPWARD_SWIPE_LIMIT_RATIO = 0.833333f

class ArActivity : AppCompatActivity() {

    // the game score
    var score = 0
        set(value) {
            field = value
            setUIScore(value)
        }

    //TODO: the throw number should arrive from the bundle
    // note: these could be in a 'Game' class, but for now I don't think that's necessary
    var numOfThrows = DEFAULT_THROWS
        set(value) {
            field = value
            setUIThrows(value)
            if (value == 0) {
                endGame(false) // if the monster is dead, the game ends before this call
            }
        }

    private lateinit var arFragment: ArFragment

    // for tracking gesture (swipe) hits to the thrown projectile (acorn)
    private var hitProj = false
    private var projNode: Projectile? = null

    private var hitMonster = false
    private var monsterNode: Monster? = null

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

    // we need this reference to manage saving and restoring the Fragment
    // private var customArFragmentInstance: Fragment? = null

    private var viewGroup: ViewGroup? = null

    var gamePaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide the top UI bar of the app
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        // it gets *and* sets them
        setScreenSizeConstants()
        setContentView(R.layout.activity_ar) // inflates all the child views correctly

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment

        startService(Intent(this@ArActivity, SoundService::class.java))

        // we're restoring the Activity due to minimizing the app, etc
        if (savedInstanceState != null) {

            // Restore the fragment's instance
            // customArFragmentInstance = supportFragmentManager.getFragment(savedInstanceState, AR_FRAGMENT_TAG)
        }

        disablePlaneDetection()
        arFragment.arSceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->

            onPeekTouchDetect(hitTestResult, motionEvent)
        }

        // create the first nut and the monster
        monsterNode = PlasticMonster.create(arFragment.arSceneView.scene.camera)
        setUIHitPoints(monsterNode?.hitPoints ?: 0)
        Projectile.create(arFragment.arSceneView.scene.camera, onThrowAnimEndCallbackHolder)

        setUIWindX(wind.xComp)
        setUIWindY(wind.yComp)

        btn_pause.setOnClickListener {

            if (!gamePaused) pauseGame() else resumeGame()
        } // onClickListener
    } // onCreate

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save the fragment's instance (it should always exist)
        // supportFragmentManager.putFragment(outState, AR_FRAGMENT_TAG, customArFragmentInstance!!)
    }

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        startService(Intent(this@ArActivity, SoundService::class.java))
    }

    override fun onStop() {
        super.onStop()
        supportActionBar?.show()
    }

    override fun onDestroy() {
        stopService(Intent(this@ArActivity, SoundService::class.java))
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        stopService(Intent(this@ArActivity, SoundService::class.java))
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_close_app, viewGroup)
        builder.setView(dialogView)
            .setPositiveButton(R.string.yes) { _, _ ->
                val mainIntent = Intent(this@ArActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
            .setNegativeButton(R.string.no) { _, _ ->
            }.show()
    } // onBackPressed

    private fun onPeekTouchDetect(hitTestResult: HitTestResult, motionEvent: MotionEvent) {

        if (gamePaused) return

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

            setUIPower((hypot( abs(startDistanceX - motionEvent.rawX), abs(startDistanceY - motionEvent.rawY))).toInt())
        }

        if (hitProj && motionEvent.actionMasked == MotionEvent.ACTION_UP) {

            val upwardSwipe = motionEvent.rawY < UPWARD_SWIPE_LIMIT_RATIO * screenHeight

            if (upwardSwipe) {

                // 0.36 = the experimentally defined ARCore coordinate system value at the right edge of the screen
                // on a Samsung Galaxy S7 (at a distance of half the screen from the center-point of the x axis in the regular Android
                // coordinate system; i.e., 540 pixels)
                val coordSystemConvertRatio = 0.36f / 540 // we'll have to hope the relationship holds for all screen sizes

                val scaledX = ((motionEvent.rawX - screenCenter.x) * coordSystemConvertRatio) * hitScaleFactor // + wind.xComp

                val localY = projNode!!.localPosition.y
                val tempY = ((screenCenter.y - motionEvent.rawY) * coordSystemConvertRatio) // gives -0.33156f min value
                val scaledY = localY + (abs(localY) + tempY) * hitScaleFactor // + wind.yComp

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
            val actualHitTestResult = arFragment.arSceneView.scene.hitTest(actualHitTestMEvent)
            val actuallyHitNode = actualHitTestResult.node

            if (actuallyHitNode is Monster) {

                hitMonster = true
                monsterNode!!.damage(1)
                score += 2 // each hit is worth 2 points
                setUIHitPoints(monsterNode!!.hitPoints)
                Log.d("HUUH", "hit monster!")

                if (!monsterNode!!.isAlive) {

                    endGame(true)
                }
            } // if Monster
            numOfThrows-- // the game ends if it goes to zero
            score-- // used throw = -1 score
            // Log.d("HUUH", "numOfThrows after decrease: $numOfThrows")
            projNode?.dispose() // delete the old nut
            projNode = null
            Projectile.create(arFragment.arSceneView.scene.camera, this) // immediately create a new nut
            setUIPower(0)
        } // onDropAnimEnd
    } // onThrowAnimEndCallbackHolder

    private fun pauseGame() {

        gamePaused = true
        projNode?.pauseAnimations()
        monsterNode?.monsterAI?.pauseExecution()
        btn_pause.text = "Resume"
        stopService(Intent(this@ArActivity, SoundService::class.java))
    }

    private fun resumeGame() {

        gamePaused = false
        projNode?.resumeAnimations()
        monsterNode?.monsterAI?.resumeExecution()
        btn_pause.text = "Pause"
        startService(Intent(this@ArActivity, SoundService::class.java))
    }

    private fun endGame(monsterDead: Boolean) {

        if (monsterDead) score += monsterNode!!.pointsValueOnDeath

        Log.d("HUUH", "final points: $score")
        //TODO: move to the reward screen and send the points there
    } // endGame

    private fun setUIScore(score: Int) {

        if (tv_score == null) return
        tv_score.text = "Score: $score"
    }

    private fun setUIThrows(throws: Int) {

        if (tv_throws == null) return
        tv_throws.text = "Throws: $throws"
    }

    private fun setUIHitPoints(hp: Int) {

        if (tv_hitpoints == null) return
        tv_hitpoints.text = "HP: $hp"
    }

    private fun setUIWindX(windX: Float) {

        if (tv_wind_x == null) return
        tv_wind_x.text = "Wind(x): $windX"
    }

    private fun setUIWindY(windY: Float) {

        if (tv_wind_y == null) return
        tv_wind_y.text = "Wind(x): $windY"
    }

    private fun setUIPower(power: Int) {

        if (launch_power_meter == null) return
        launch_power_meter.progress = power
    }

    private fun disablePlaneDetection() {

        arFragment.planeDiscoveryController.hide()
        arFragment.planeDiscoveryController.setInstructionView(null)
        arFragment.arSceneView.planeRenderer.isEnabled = false
    }

    private fun enablePlaneDetection() {
        arFragment.planeDiscoveryController.show()
        arFragment.arSceneView.planeRenderer.isEnabled = true
    }

    // creates a 'fake' MotionEvent that 'touches' a given screen point
    private fun obtainMotionEvent(point: Point): MotionEvent {

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

    // maybe shorten its name, ehh
    private fun convertMEventCoordsToScaledScreenTargetPoint(x: Float, y: Float): Point {

        // val alterXBy = wind.xComp * screenWidth / 0.55f // experimental constant; only works on the Galaxy S7!
        // Log.d("HUUH", "alter x by: $alterXBy")

        val scaledX = screenCenter.x + (x - screenCenter.x) * hitScaleFactor // + alterXBy

        // val alterYBy = wind.yComp * screenHeight / 3.6f
        // Log.d("HUUH", "subtract from y: $alterYBy")

        // reverse axis (from 1920 to 0) and zero-point off-center
        val scaledY = screenHeight - (abs(y - screenHeight)) * hitScaleFactor // - alterYBy
        return Point(scaledX.toInt(), scaledY.toInt())
    } // convertMEventCoordsToScaledScreenTargetPoint

    private fun setScreenSizeConstants() {

        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
        screenCenter = Point(screenWidth / 2, screenHeight / 2)
    } // setScreenSizeConstants

} // ArActivity