package com.example.timil.climateapplication

import android.content.Intent
import android.graphics.Point
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.TextView
import com.example.timil.climateapplication.ar.*
import com.example.timil.climateapplication.services.SoundService
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_ar.*
import kotlin.math.abs
import kotlin.math.hypot
import android.os.CountDownTimer
import com.example.timil.climateapplication.MainActivity.Companion.MONSTER_TYPE
import com.example.timil.climateapplication.fragments.DiscountsFragment
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.PlaneRenderer
import com.google.ar.sceneform.rendering.Texture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.view.ViewGroup
import android.support.v4.app.SupportActivity
import android.support.v4.app.SupportActivity.ExtraData
import android.support.v4.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Debug
import android.support.constraint.ConstraintLayout
import android.widget.ImageView
import kotlin.math.PI


/**
 * Activity that governs the AR game portion of the app.
 * @author Ville Lohkovuori, Leo Partanen
 * */

private const val DEFAULT_THROWS = 5
private const val CORRECT_ANSWER_THROWS = 10

// For converting between the phone screen's regular coordinates and ARCore's coordinate system.
// 0.36 = the experimentally defined ARCore coordinate system value at the right edge of the screen
// on a Samsung Galaxy S7 (at a distance of half the screen from the center-point of the x axis in the regular Android
// coordinate system; i.e., 540 pixels) ( we'll have to hope the relationship holds for all screen sizes )
private const val COORD_SYS_CONVERT_RATIO = 0.36f / 540

// for UI updates
// (it's a little bit decimated since I removed the wind values from it... maybe refactor)
private enum class ViewType {
    HP,
    THROWS
}

enum class MonsterType: Serializable {
    PLASTIC,
    CO2,
    OIL
}

// the percentage of screen space (from the top of the screen)
// where a projectile-touching finger swipe will have to end to be considered 'upwards'
private const val UPWARD_SWIPE_LIMIT_RATIO = 0.833333f
private const val THROW_TIME_LIMIT = 1200L // how long you have to throw the nut (in ms)

class ArActivity : AppCompatActivity() {

    private var numOfThrows = DEFAULT_THROWS
        set(value) {
            field = value
            updateUI(ViewType.THROWS, value)
        }

    private val totalMonsterHp: Int
        get() {
            return monsterNodes.map { it?.hitPoints ?: 0 }.reduce { acc, it -> acc + it }
        }

    private lateinit var arFragment: ArFragment
    private var anchorNode: AnchorNode? = null // for placing the oil monster

    // for tracking gesture (swipe) hits to the thrown projectile (acorn)
    private var hitProj = false
    private var projNode: Projectile? = null
    private val monsterNodes = arrayOfNulls<Monster>(5) // bad form, but I need the easy index access

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

    private var viewGroup: ViewGroup? = null

    private var throwTimerExpired = false
    private var gamePaused = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // hide the top UI bar (that shows battery level etc)
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContentView(R.layout.activity_ar)

        // it 'gets' *and* sets them
        setScreenSizeConstants()

        setAIMoveBoundaryConstants(screenCenter.x) // NOTE: must be called after setScreenSizeConstants!

        if (AppStatus().soundsOn(this)) { startService(Intent(this@ArActivity, SoundService::class.java)) }

        //val quizAnswerCorrect = intent?.extras?.getBoolean(getString(R.string.quiz_answer_correct_key)) ?: false
        //numOfThrows = if (quizAnswerCorrect) CORRECT_ANSWER_THROWS else DEFAULT_THROWS
        numOfThrows = intent?.extras?.getInt(getString(R.string.quiz_answer_correct_key))!!

        adjustWindArrowIndicator(this.wind, iv_arrow)

        arFragment = supportFragmentManager.findFragmentById(R.id.ar_fragment) as ArFragment
        arFragment.arSceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->

            onPeekTouchDetect(hitTestResult, motionEvent)
        }

        val monsterType = intent.getSerializableExtra(MONSTER_TYPE) as MonsterType

        if (monsterType == MonsterType.OIL) {

            toggleHpViewVisibility(false) // hide the hp view since there is no hp to show
            enableArObjectPlacement() // the monster is spawned after choosing where to place it
        } else {
            disablePlaneDetection()

            // create the first nut and the monster
            monsterNodes[0] = when (monsterType) {

                MonsterType.PLASTIC ->  PlasticMonster.create(arFragment.arSceneView.scene.camera)
                MonsterType.CO2 -> Co2Monster.create(arFragment.arSceneView.scene.camera)
                else -> Co2Monster.create(arFragment.arSceneView.scene.camera) // it's required and can't be null... should never be reached
            }
            updateUI(ViewType.HP, monsterNodes[0]!!.hitPoints)
            Projectile.create(arFragment.arSceneView.scene.camera, onThrowAnimEndCallbackHolder)
        } // if-else

        btn_pause.setOnClickListener {

            if (!gamePaused) pauseGame() else resumeGame()
        } // onClickListener
    } // onCreate

    override fun onResume() {
        super.onResume()
        supportActionBar?.hide()
        if (AppStatus().soundsOn(this)) { startService(Intent(this@ArActivity, SoundService::class.java)) }
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

        val hitNode = hitTestResult.node

        if (!hitProj && hitNode is Projectile) {

            // do not detect hits to already thrown projectiles
            if (hitNode.isThrown) return

            startDistanceY = motionEvent.rawY
            startDistanceX = motionEvent.rawX

            hitProj = true
            projNode = hitNode

            throwTimerExpired = false
            // we must throw the nut within a certain amount of time or the throw is canceled
            startThrowTimer(hitNode, THROW_TIME_LIMIT)
        } // if !hitProj && is Projectile

        if (throwTimerExpired) return // note: this check should remain exactly here!

        if (startDistanceY > 0f && startDistanceX > 0f) {

            setUIPower((hypot( abs(startDistanceX - motionEvent.rawX), abs(startDistanceY - motionEvent.rawY))).toInt())
        }

        if (hitProj && motionEvent.actionMasked == MotionEvent.ACTION_UP) {

            val upwardSwipe = motionEvent.rawY < UPWARD_SWIPE_LIMIT_RATIO * screenHeight

            if (upwardSwipe) {

                val scaledX = ((motionEvent.rawX - screenCenter.x) * COORD_SYS_CONVERT_RATIO) * hitScaleFactor + wind.xComp

                val localY = projNode!!.localPosition.y
                val tempY = ((screenCenter.y - motionEvent.rawY) * COORD_SYS_CONVERT_RATIO)
                val scaledY = localY + (abs(localY) + tempY) * hitScaleFactor - wind.yComp

                // the target coordinate in the ARCore coordinate system
                val target = Vector3(scaledX, scaledY, -1.0f) // sink it down a little bit, with a lower final z-value

                // store the end point (screen coordinate) of the animation that is launched directly below
                actualScaledHitPoint = convertMEventCoordsToScaledScreenTargetPoint(motionEvent.rawX, motionEvent.rawY)

                projNode!!.launch(target) // triggers the animation; at the end of it comes the hit check to the monster

                startDistanceY = 0f
                startDistanceX = 0f
            } // if upwardSwipe

            hitProj = false
        } // if real MotionEvent == UP event
    } // onPeekTouchDetect

    // for communicating with the thrown projectiles
    private val onThrowAnimEndCallbackHolder = object : Projectile.IonThrowAnimEndListener {

        override fun onRiseAnimEnd() {

            // Log.d("HUUH", "localPos after rise: $localPosition")
        }

        // TODO: un-spaghettify the game end logic
        override fun onDropAnimEnd() {

            // these shenanigans are needed because the hit detection should only happen once the
            // throwing animation has finished

            // we'll 'touch' the scaled hit point (xx % further than the finger swipe's end point)
            val actualHitTestMEvent = obtainMotionEvent(actualScaledHitPoint!!) // it always exists if the animation is playing
            val actualHitTestResult = arFragment.arSceneView.scene.hitTest(actualHitTestMEvent)
            val actuallyHitNode = actualHitTestResult.node

            numOfThrows--

            if (actuallyHitNode is Monster) {

                actuallyHitNode.damage(1)
                if (AppStatus().vibrationOn(this@ArActivity)) {
                    Vibrator().vibrate(this@ArActivity, Vibrator.VIBRATION_TIME_REGULAR)
                }

                // non-optimal, but ehh, there's very few monsters
                updateUI(ViewType.HP, totalMonsterHp)
                Log.d("HUUH", "hit monster!")

                if (!actuallyHitNode.isAlive) {

                    if (actuallyHitNode is OilMonster && actuallyHitNode.isInitialMonster) {

                        spawnSmallOilMonsters(actuallyHitNode.localPosition)
                    }

                    if (allMonstersDead()) {

                        endGame(true)
                        return // so that we won't 'end' the game twice if this was the last throw
                    }
                }
            } // if Monster

            if (numOfThrows <= 0) { // should only ever reach 0
                endGame(false) // if the monsters are all dead, the game ends before this call
            }

            projNode?.dispose() // delete the old nut
            projNode = null
            Projectile.create(arFragment.arSceneView.scene.camera, this) // immediately create a new nut

            setUIPower(0)
        } // onDropAnimEnd
    } // onThrowAnimEndCallbackHolder

    private fun pauseGame() {

        gamePaused = true
        projNode?.pauseAnimations()
        monsterNodes.forEach {
            it?.pauseAI()
        }
        btn_pause.text = getString(R.string.txt_resume)
        stopService(Intent(this@ArActivity, SoundService::class.java))
    }

    private fun resumeGame() {

        gamePaused = false
        projNode?.resumeAnimations()
        monsterNodes.forEach {
            it?.resumeAI()
        }
        btn_pause.text = getString(R.string.txt_pause)
        if (AppStatus().soundsOn(this)) { startService(Intent(this@ArActivity, SoundService::class.java)) }
    }

    private fun endGame(allMonstersDead: Boolean) {

        var score = numOfThrows

        monsterNodes.forEach {

            if (it != null) {
                score += if (!it.isAlive) {
                    it.pointsValueOnDeath
                } else {
                    it.maxHitPoints - it.hitPoints
                }
            }
        }

        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_end_game, viewGroup)

        // can't 'see' the views without this trick
        dialogView.findViewById<TextView>(R.id.tv_end_score).text = getString(R.string.txt_score, score)
        dialogView.findViewById<TextView>(R.id.tv_loss_victory).text = if (allMonstersDead) getString(R.string.txt_victory) else getString(R.string.txt_loss)

        // save the points in the database
        saveScoreToDb(score)

        builder.setView(dialogView)
            .setPositiveButton(getString(R.string.txt_rewards)) { _, _ ->
                val mainIntent = Intent(this@ArActivity, MainActivity::class.java)
                mainIntent.putExtra("discountsFragment", "discountsFragment")
                startActivity(mainIntent)
                finish()
            }
            .setNegativeButton(getString(R.string.txt_start)) { _, _ ->
                val mainIntent = Intent(this@ArActivity, MainActivity::class.java)
                startActivity(mainIntent)
                finish()
            }
            .setCancelable(false)
            .show()

        Log.d("HUUH", "final points: $score")
    } // endGame

    private fun updateUI(viewType: ViewType, value: Any) {

        when(viewType) {
            // i'm sure there's a better way to do this than these idiotic casts...
            ViewType.HP -> tv_hitpoints.text = getString(R.string.txt_HP, value as Int)
            ViewType.THROWS -> tv_throws.text = getString(R.string.txt_throws, value as Int)
        }
    } // updateUI

    // the functionality is a bit different, so let's keep it as a separate function from other UI stuffs
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

    private fun enableArObjectPlacement() {

        setCustomPlaneTexture()

        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->

            val anchor = hitResult.createAnchor()
            anchorNode = AnchorNode(anchor)
            anchorNode?.setParent(arFragment.arSceneView.scene)

            monsterNodes[0] = OilMonster.create(anchorNode!!)

            // technically a false value, but it's easiest to deal with multiple monsters in this way
            updateUI(ViewType.HP, 5)
            Projectile.create(arFragment.arSceneView.scene.camera, onThrowAnimEndCallbackHolder)

            toggleHpViewVisibility(true) // we can now show the hp view

            // Disable the placement ability after one monster has been placed
            disablePlaneDetection()
            arFragment.setOnTapArPlaneListener(null)
        }
    } // enableArObjectPlacement

    private fun setCustomPlaneTexture() {

        // set plane renderer to red
        val sampler = Texture.Sampler.builder()
            .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
            .setWrapMode(Texture.Sampler.WrapMode.REPEAT)
            .build()

        Texture.builder()
            .setSource(this, R.drawable.plane_texture)
            .setSampler(sampler)
            .build()
            .thenAccept { texture ->
                arFragment
                    .arSceneView
                    .planeRenderer
                    .material
                    .thenAccept { material ->
                        material.setTexture(
                    PlaneRenderer.MATERIAL_TEXTURE, texture)
                    }
            } // outer thenAccept
    } // setCustomPlaneTexture

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
    } // obtainMotionEvent

    // we need to scale the target of the fake motion event, since it is to be xx % further than
    // the end of the finger swipe
    private fun convertMEventCoordsToScaledScreenTargetPoint(x: Float, y: Float): Point {

        val alterXBy = wind.xComp / COORD_SYS_CONVERT_RATIO
        // Log.d("HUUH", "alter x by: $alterXBy")

        val scaledX = screenCenter.x + (x - screenCenter.x) * hitScaleFactor + alterXBy

        val alterYBy = wind.yComp / COORD_SYS_CONVERT_RATIO
        // Log.d("HUUH", "alter y by: $alterYBy")

        // reverse axis (from 1920 to 0) and zero-point off-center
        val scaledY = screenHeight - (abs(y - screenHeight)) * hitScaleFactor + alterYBy
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

    // we have a limited amount of time to throw the nut
    private fun startThrowTimer(projectile: Projectile, timeInMs: Long) {

        object : CountDownTimer(timeInMs, 200) {

            override fun onTick(millisUntilFinished: Long) {

                // Log.d("HUUH", "ticking down!")
            }

            override fun onFinish() {

                throwTimerExpired = true
                hitProj = false // this assignment is needed, but it's bad code; change if possible

                // for thrown projectiles, the power bar animation should keep playing for the throw's whole length
                if (!projectile.isThrown) {

                    setUIPower(0)
                }
                // Log.d("HUUH", "out of time!")
            }
        }.start()
    } // startThrowTimer

    private fun allMonstersDead(): Boolean {

        monsterNodes.forEach {

            if (it != null) {
                if (it.isAlive) {
                    return false
                }
            }
        }
        return true
    } // allMonstersDead

    private fun spawnSmallOilMonsters(localPos: Vector3) {

        // Log.d("HUUH", "original localPos: $localPos")

        monsterNodes[1] = OilMonster.createSmall(anchorNode!!, Vector3(localPos.x-0.2f, localPos.y+Static.randomFloatBetween(-0.05f, 0.05f), localPos.z-0.2f))
        monsterNodes[2] = OilMonster.createSmall(anchorNode!!, Vector3(localPos.x-0.2f, localPos.y+Static.randomFloatBetween(-0.05f, 0.05f), localPos.z+0.2f))
        monsterNodes[3] = OilMonster.createSmall(anchorNode!!, Vector3(localPos.x+0.2f, localPos.y+Static.randomFloatBetween(-0.05f, 0.05f), localPos.z-0.2f))
        monsterNodes[4] = OilMonster.createSmall(anchorNode!!, Vector3(localPos.x+0.2f, localPos.y+Static.randomFloatBetween(-0.05f, 0.05f), localPos.z+0.2f))

        updateUI(ViewType.HP, totalMonsterHp)
    } // spawnSmallOilMonsters

    private fun toggleHpViewVisibility(visible: Boolean) {

        tv_hitpoints.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun setAIMoveBoundaryConstants(screenCenterXValue: Int) {

        val screenEdgeInArCoords = COORD_SYS_CONVERT_RATIO * screenCenterXValue // 0.36 on the Samsung Galaxy S7

        // these values give good behavior (i.e., the monster models do not move off-screen)
        val xMaxAbs = screenEdgeInArCoords * 0.7f
        val yMin = screenEdgeInArCoords * 0.278f
        val yMax = screenEdgeInArCoords * 0.7f

        AI.setMoveBoundaryConstants(xMaxAbs, yMin, yMax)
    } // setAIMoveBoundaryConstants

    private fun saveScoreToDb(score: Int){
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    var userPoints = 0

                    if(document.data != null){
                        if(document.data!!.getValue("points") != null){
                            userPoints = document.data!!.getValue("points").toString().toInt()
                        }
                        val total = userPoints+score
                        db.collection("users").document(userId)
                            .update("points", total)
                            .addOnSuccessListener {
                                Log.d("tester", "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e -> Log.w("tester", "Error writing document", e) }
                    } else {
                        val total = userPoints + score

                        val userData = hashMapOf(
                            "points" to total
                        )
                        db.collection("users").document(userId)
                            .set(userData)
                            .addOnSuccessListener {
                                Log.d("tester", "DocumentSnapshot successfully written!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(
                                    "tester",
                                    "Error writing document",
                                    e
                                )
                            }
                    }
                } else {
                    Log.d("tester", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("tester", "get failed with ", exception)
            }
    } // saveScoreToDb

    // correctly orient and scale the wind arrow indicator (to show the wind force & direction)
    private fun adjustWindArrowIndicator(wind: Wind, icon: ImageView) {

        //icon.rotation = -wind.degreeAngle // for some reason, clockwise == positive direction here
        icon.animate().rotation(-wind.degreeAngle).start()

        val scaleXyBy = (1 + wind.force / 25) // value range: 1-2
        val newX = (icon.layoutParams.width * scaleXyBy).toInt()
        val newY = (icon.layoutParams.height * scaleXyBy).toInt()
        icon.layoutParams.width = newX
        icon.layoutParams.height = newY
        icon.requestLayout()

        //TODO: ideally, the arrow's width would stay the same and only the length would change
        // according to the force attribute (atm we scale the entire arrow)

        tv_force.text = wind.force.toInt().toString()
    } // adjustWindArrowIndicator

} // ArActivity