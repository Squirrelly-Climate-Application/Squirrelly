package com.example.timil.climateapplication.ar

import android.util.Log

/**
 * For encapsulating all the data that's needed for the ar game adversaries.
 * @author Ville Lohkovuori
 * */

private const val DEFAULT_HITPOINTS = 10

abstract class Monster: WorldEntity() {

    var isAlive: Boolean = true
        get() = this.hitPoints > 0
        private set

    open var hitPoints = DEFAULT_HITPOINTS
        set(value) { // it seems that this custom setter is *not* auto-inherited, but i'm keeping the template for it here just for clarity
            field = value
            checkForDeath()
        }

    open var monsterAI: AI? = null // it can't be initialized here because of the 'leaking context' warning

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