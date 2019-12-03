package com.example.timil.climateapplication.ar

/**
 * Base class for the AR game monsters.
 * @author Ville Lohkovuori
 * */

private const val DEFAULT_HITPOINTS = 10
private const val DEFAULT_DEATH_POINTS_VALUE = 5

abstract class Monster : WorldEntity() {

    var isAlive: Boolean = true
        get() = this.hitPoints > 0
        private set

    // used for score-counting purposes
    open val maxHitPoints = DEFAULT_HITPOINTS

    open var hitPoints = DEFAULT_HITPOINTS
        set(value) { // it seems that this custom setter is *not* auto-inherited, but i'm keeping the template for it here just for clarity
            field = value
            checkForDeath()
        }

    // how many points you get for killing the monster
    open val pointsValueOnDeath = DEFAULT_DEATH_POINTS_VALUE

    open lateinit var monsterAI: AI // it can't be initialized here because of the 'leaking context' warning

    protected fun checkForDeath() {

        if (!isAlive) {
            onDeath()
        }
    }

    open fun onDeath() {

        dispose() // destroy the visual monster model
        monsterAI.terminate() // destroy the AI
    }

    fun damage(amount: Int) {

        hitPoints -= amount
    }

    // note: AI starting and termination are handled differently.
    // the ArActivity is doing the pausing, so it's good to have these methods.
    fun pauseAI() {

        monsterAI.pauseExecution()
    }

    fun resumeAI() {

        monsterAI.resumeExecution()
    }

} // Monster