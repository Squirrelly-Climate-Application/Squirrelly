package com.example.timil.climateapplication.ar

/**
 * A class that's used to create temporary effects with visual representations, such
 * as flashing lights or small clouds that gradually disappear.
 * */

//TODO: move some of the animation logic of the small clouds to a subclass of this class, perhaps
class EffectEntity : WorldEntity() {

    override fun dispose() {
        super.dispose()
        light = null
    }

} // EffectEntity