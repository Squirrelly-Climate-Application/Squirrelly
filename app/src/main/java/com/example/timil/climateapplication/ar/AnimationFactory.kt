package com.example.timil.climateapplication.ar

import android.animation.ObjectAnimator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import android.animation.AnimatorListenerAdapter
import android.animation.FloatEvaluator
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.collision.Sphere
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3Evaluator
import com.google.ar.sceneform.rendering.Light

/**
 * For creating animators (that animate ar objects).
 * @author Ville Lohkovuori
 * */

object AnimationFactory {

    // for linear movement between two points
    fun linearMoveAnim(
        node: Node?,
        dura: Long,
        from: Vector3? = Vector3(0f, 0f, 0f),
        to: Vector3? = Vector3(0f, 0f, 0f),
        endListener: AnimatorListenerAdapter): ObjectAnimator {

        return ObjectAnimator().apply {

            target = node
            propertyName = "localPosition"
            duration = dura
            interpolator = LinearInterpolator() // default, but let's have it for clarity's sake
            setAutoCancel(false)
            setObjectValues(from, to)
            setEvaluator(Vector3Evaluator())
            addListener(endListener)
        } // apply
    } // linearMoveAnim

    // it's a bit untidy, but passing null to addListener leads to a NullPointerException when you
    // start the animation, so we need a separate function in case there is no listener
    fun linearMoveAnimNoEndListener(
        node: Node?,
        dura: Long,
        from: Vector3? = Vector3(0f, 0f, 0f),
        to: Vector3? = Vector3(0f, 0f, 0f)): ObjectAnimator {

        return ObjectAnimator().apply {

            target = node
            propertyName = "localPosition"
            duration = dura
            interpolator = LinearInterpolator() // default, but let's have it for clarity's sake
            setAutoCancel(false)
            setObjectValues(from, to)
            setEvaluator(Vector3Evaluator())
        } // apply
    } // linearMoveAnimNoEndListener

    // for making objects spin
    fun spinAnim(
        node: Node,
        dura: Long,
        toQuaternion: Quaternion): ObjectAnimator {

        return ObjectAnimator().apply {

            target = node
            propertyName = "localRotation"
            duration = dura
            interpolator = LinearInterpolator() // default, but let's have it for clarity's sake
            setAutoCancel(false)
            setObjectValues(node.localRotation, toQuaternion)
            setEvaluator(QuaternionEvaluator())
        } // apply
    } // spinAnim

    fun multiValueSpinAnim(
        node: Node,
        dura: Long,
        vararg quaternions: Quaternion): ObjectAnimator {

        return ObjectAnimator().apply {

            target = node
            propertyName = "localRotation"
            duration = dura
            interpolator = LinearInterpolator() // default, but let's have it for clarity's sake
            setAutoCancel(false)
            setObjectValues(*quaternions)
            setEvaluator(QuaternionEvaluator())
        } // apply
    } // multiValueSpinAnim

    // make objects larger or smaller
    fun scaleAnim(
        node: Node,
        dura: Long,
        newScale: Vector3
    ): ObjectAnimator {

        return ObjectAnimator().apply {

            target = node
            propertyName = "localScale"
            duration = dura
            interpolator = LinearInterpolator()
            setAutoCancel(false)
            setObjectValues(node.localScale, newScale)
            setEvaluator(Vector3Evaluator())
        }
    } // scaleAnim

    // the collisionShape of Renderables needs to be scaled separately from their localScale property
    fun sphereCollisionShapeScaleAnim(
        node: Node,
        dura: Long,
        newRadius: Float
    ): ObjectAnimator {

        return ObjectAnimator().apply {

            val sphere = node.renderable?.collisionShape as Sphere

            target = sphere
            propertyName = "radius"
            duration = dura
            interpolator = LinearInterpolator()
            setAutoCancel(false)
            setObjectValues(sphere.radius, newRadius)
            setEvaluator(FloatEvaluator())
        }
    } // sphereCollisionShapeScaleAnim

    // increases and then decreases the light intensity, giving the impression of a flash
    fun lightOnOffAnimation(light: Light, dura: Long, peakIntensity: Float, endListener: AnimatorListenerAdapter): ObjectAnimator {
        return ObjectAnimator().apply {

            target = light
            propertyName = "intensity"
            duration = dura / 2
            interpolator = BounceInterpolator()
            setAutoCancel(false)
            setObjectValues(peakIntensity)
            setEvaluator(FloatEvaluator())
            addListener(endListener)
        } // apply
    } // lightOnOffAnimation

} // AnimationFactory