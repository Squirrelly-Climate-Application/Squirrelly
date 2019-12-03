package com.example.timil.climateapplication.ar

import kotlin.math.*

/**
 * A convenience abstraction for the wind effect that's applied in the AR game.
 * @author Ville Lohkovuori
 * */

private const val WIND_X_MIN = -0.15f // NOTE: larger values may introduce inaccuracy
private const val WIND_X_MAX = 0.15f
private const val WIND_Y_MIN = -0.2f
private const val WIND_Y_MAX = 0.2f

class Wind private constructor() {

    // might make these variable (during the game) in the future.
    // NOTE: these are ARCore coordinate system values
    val xComp = Static.randomFloatBetween(WIND_X_MIN, WIND_X_MAX)
        get() {
            return if (field == 0.0f) 0.0001f else field // to avoid divide by zero error
        }

    val yComp = Static.randomFloatBetween(WIND_Y_MIN, WIND_Y_MAX)
    val force = (sqrt(xComp.pow(2.0f) + yComp.pow(2.0f)) * 100) // max 25; looks better in the UI

    // the angle (from 0 to 2 PI) of the 'wind power vector' (relative to the x-plane).
    // to be used in positioning the wind indicator (arrow).
    // NOTE: due to the ARCore coordinate system, the 'negative' y direction means the wind is blowing away from the player
    val radAngle: Float = if (xComp <= 0f && yComp <= 0f) {

        // e.g.: y = -0.15; x = -0.10
        (PI - atan(yComp / xComp)).toFloat() // about 124 degrees
    } else if (xComp <= 0f && yComp > 0f) {

        // e.g.: y = 0.15; x = -0.10
        (PI + atan(-yComp / xComp)).toFloat() // 236 degrees
    } else if (xComp > 0 && yComp <= 0 ) {

        // e.g.: y = -0.15; x = 0.10
        (atan(-yComp / xComp)) // 56 degrees
    } else { // xComp > 0 && yComp > 0

        // e.g.: y = 0.15; x = 0.10
        (2 * PI - atan(yComp / xComp)).toFloat() // 304 degrees
    } // radAngle if

    val degreeAngle = (radAngle * 180f / PI).toFloat()

    companion object {

        // redundant for now, but I'm keeping the creation pattern for consistency
        fun create(): Wind {

            return Wind()
        }
    } // companion object

} // Wind