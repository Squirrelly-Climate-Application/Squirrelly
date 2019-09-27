package com.example.timil.climateapplication.ar

import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3

/**
 * Base class for all ar objects.
 * @author Ville Lohkovuori
 * */

abstract class WorldEntity: Node() {

    // ideally, this class would also contain the renderable, but it needs to be part of the companion object
    // and it can't be made abstract or overridden

    // this might be unnecessary
    fun setPosition(x: Float, y: Float, z: Float) {

        localPosition = Vector3(x, y, z)
    }

    open fun dispose() {

        renderable = null
        setParent(null)
    }

} // WorldEntity