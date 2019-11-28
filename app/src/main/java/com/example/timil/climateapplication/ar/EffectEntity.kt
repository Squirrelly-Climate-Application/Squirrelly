package com.example.timil.climateapplication.ar

/**
 * A class that's used to create temporary effects with visual representations, such
 * as flashing lights or small clouds that gradually disappear.
 * */

class EffectEntity : WorldEntity() {

    override fun dispose() {
        super.dispose()
        light = null
    }

} // EffectEntity