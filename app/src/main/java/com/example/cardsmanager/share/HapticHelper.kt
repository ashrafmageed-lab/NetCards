package com.example.cardsmanager.share

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticHelper {

    fun performClick(context: Context) {
        vibrate(context, 20)
    }

    fun performSuccess(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 40, 40)
            val amplitudes = intArrayOf(0, 180, 0, 255)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            getVibrator(context)?.vibrate(effect)
        } else {
            vibrate(context, 40)
        }
    }

    fun performWarning(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
            getVibrator(context)?.vibrate(effect)
        } else {
            vibrate(context, 80)
        }
    }

    private fun vibrate(context: Context, durationMs: Long) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }
}
