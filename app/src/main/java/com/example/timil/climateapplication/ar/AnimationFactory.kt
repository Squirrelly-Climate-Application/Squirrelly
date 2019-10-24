package com.example.timil.climateapplication.ar

import android.animation.ObjectAnimator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import android.animation.AnimatorListenerAdapter
import android.util.Log
import android.view.animation.LinearInterpolator
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.collision.Sphere
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3Evaluator

/**
 * For creating animators (that animate ar objects).
 * @author Ville Lohkovuori
 * */

object AnimationFactory {

    // for linear movement between two points
    fun linearMoveAnim(
        node: Node?,
        dura: Long,
        from: Vector3 = Vector3(0f, 0f, 0f),
        to: Vector3? = Vector3(0f, 0f, 0f),
        endListener: AnimatorListenerAdapter? = null): ObjectAnimator {

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

    // for making objects spin
    //TODO: adjust the speed according to the launch speed, maybe?
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
    fun boxCollisionShapeScaleAnim(
        node: Node,
        dura: Long,
        newSize: Vector3
    ): ObjectAnimator {

        return ObjectAnimator().apply {

            val box = node.renderable?.collisionShape as Box

            target = box
            propertyName = "size"
            duration = dura
            interpolator = LinearInterpolator()
            setAutoCancel(false)
            setObjectValues(box.size, newSize)
            setEvaluator(Vector3Evaluator())
        }
    } // boxCollisionShapeScaleAnim

    // untested; not sure which evaluator would work with it
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
        }
    } // sphereCollisionShapeScaleAnim

} // AnimationFactory