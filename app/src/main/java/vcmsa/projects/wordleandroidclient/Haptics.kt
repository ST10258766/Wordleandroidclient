package vcmsa.projects.wordleandroidclient

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object Haptics {

    private fun vibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= 31) {
            val vm = context.getSystemService(VibratorManager::class.java)
            vm?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun tick(context: Context) {
        val vib = vibrator(context) ?: return

        when {
            Build.VERSION.SDK_INT >= 29 -> {
                // Modern TICK vibration
                vib.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                )
            }
            Build.VERSION.SDK_INT >= 26 -> {
                // Fallback for API 26–28
                vib.vibrate(
                    VibrationEffect.createOneShot(
                        15,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
            else -> {
                @Suppress("DEPRECATION")
                vib.vibrate(15)
            }
        }
    }

    fun success(context: Context) {
        val vib = vibrator(context) ?: return

        when {
            Build.VERSION.SDK_INT >= 29 -> {
                // Modern CLICK vibration
                vib.vibrate(
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                )
            }
            Build.VERSION.SDK_INT >= 26 -> {
                vib.vibrate(
                    VibrationEffect.createOneShot(
                        25,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            }
            else -> {
                @Suppress("DEPRECATION")
                vib.vibrate(25)
            }
        }
    }
}
