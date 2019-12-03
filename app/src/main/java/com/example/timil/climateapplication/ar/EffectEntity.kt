package com.example.timil.climateapplication.ar

/**
 * A class that's used to create temporary effects with visual representations, such
 * as flashing lights or small clouds that gradually disappear.
 * @author Ville Lohkovuori
 * */

class EffectEntity : WorldEntity() {

    // the main purpose of this class (for now) is to be able to use this method, as WorldEntity is abstract
    // and can't be created as a 'holder object' (although an object expression could be used, it would
    // be confusing imo)
    override fun dispose() {
        super.dispose()
        light = null
    }

} // EffectEntity