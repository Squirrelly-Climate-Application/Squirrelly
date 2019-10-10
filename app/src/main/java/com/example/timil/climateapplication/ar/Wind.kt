package com.example.timil.climateapplication.ar

import kotlin.math.asin
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * A convenience abstraction for the wind effect that's applied in the AR game.
 * @author Ville Lohkovuori
 * */

// NOTE: wind is disabled for now (due to coordinate scaling issues). fix asap!
private const val WIND_X_MIN = -0.15f // NOTE: larger values introduce inaccuracy
private const val WIND_X_MAX = 0.15f
private const val WIND_Y_MIN = -0.2f
private const val WIND_Y_MAX = 0.2f

class Wind {

    // might make these variable in the future
    val xComp = 0.0f // Static.randomFloatBetween(WIND_X_MIN, WIND_X_MAX)
    val yComp = 0.0f // Static.randomFloatBetween(WIND_Y_MIN, WIND_Y_MAX)
    val force = sqrt(xComp.pow(2.0f) + yComp.pow(2.0f))

    // the angle (from 0 to PI) of the 'wind power vector' (relative to the x-plane).
    // to be used in  positioning the wind indicator (arrow).
    val radAngle = asin(yComp / force)

    companion object {

        // redundant for now, but I'm keeping the creation pattern for consistency
        fun create(): Wind {

            return Wind()
        }
    } // companion object

} // Wind