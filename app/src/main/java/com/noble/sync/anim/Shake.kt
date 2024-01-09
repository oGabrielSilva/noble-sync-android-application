package com.noble.sync.anim

import android.view.View
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation

class Shake {
    companion object {
        private fun getShake(time: Long, toX: Float, cycles: Float): TranslateAnimation {
            val shake = TranslateAnimation(0f, toX, 0f, 0f)
            shake.duration = time
            shake.interpolator = CycleInterpolator(cycles)
            return shake
        }

        fun anim(v: View, time: Long, toX: Float, cycles: Float, focus: Boolean = true) {
            if (focus) v.requestFocus()
            v.startAnimation(getShake(time, toX, cycles))
        }
    }
}