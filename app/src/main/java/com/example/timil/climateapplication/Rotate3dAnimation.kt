package com.example.timil.climateapplication

import android.view.animation.Animation
import android.view.animation.Transformation
import android.graphics.Camera
import android.graphics.Matrix

class Rotate3dAnimation (
    private val mFromDegrees: Float,
    private val mToDegrees: Float,
    private val mCenterX: Float,
    private val mCenterY: Float,
    private val mDepthZ: Float,
    private val mReverse: Boolean
) : Animation() {
    private var mCamera: Camera? = null

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        mCamera = Camera()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val fromDegrees = mFromDegrees
        val degrees = fromDegrees + (mToDegrees - fromDegrees) * interpolatedTime

        val centerX = mCenterX
        val centerY = mCenterY
        val camera = mCamera

        val matrix = t.matrix

        camera!!.save()
        if (mReverse) {
            camera.translate(0.0f, 0.0f, mDepthZ * interpolatedTime)
        } else {
            camera.translate(0.0f, 0.0f, mDepthZ * (1.0f - interpolatedTime))
        }
        camera.rotateY(degrees)
        camera.getMatrix(matrix)
        camera.restore()

        matrix.preTranslate(-centerX, -centerY)
        matrix.postTranslate(centerX, centerY)
    }
}