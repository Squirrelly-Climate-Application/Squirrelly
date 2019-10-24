package com.example.timil.climateapplication.ar

import android.util.Log

/**
 * For encapsulating all the data that's needed for the ar game adversaries.
 * @author Ville Lohkovuori
 * */

private const val DEFAULT_HITPOINTS = 10
private const val DEFAULT_DEATH_POINTS_VALUE = 5

abstract class Monster: WorldEntity() {

    var isAlive: Boolean = true
        get() = this.hitPoints > 0
        private set

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
    }

    fun damage(amount: Int) {

        hitPoints -= amount
    }

} // Monster