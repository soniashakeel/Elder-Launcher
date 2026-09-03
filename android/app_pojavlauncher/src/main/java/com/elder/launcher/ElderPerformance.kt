package com.elder.launcher

import android.app.ActivityManager
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import net.kdt.pojavlaunch.Tools
import net.kdt.pojavlaunch.prefs.LauncherPreferences
import net.kdt.pojavlaunch.utils.MCOptionUtils
import java.io.File
import kotlin.math.ceil
import kotlin.math.min

/**
 * Small-device settings shared by the Compose shell and Pojav's Java launch path.
 *
 * The Android process cannot reliably terminate arbitrary foreground applications,
 * so background cleanup is deliberately best effort and never touches system apps.
 */
object ElderPerformance {
    private const val TAG = "ElderPerformance"
    private const val PREFS = "elder_performance"
    private const val KEY_LOW_END = "low_end_mode"
    private const val KEY_FPS_BOOST = "fps_boost"
    private const val KEY_PREVIOUS_SAVED = "previous_saved"
    private const val KEY_PREVIOUS_ALLOCATION = "previous_allocation"
    private const val KEY_PREVIOUS_RESOLUTION = "previous_resolution"
    private const val KEY_PREVIOUS_RENDERER = "previous_renderer"
    private const val KEY_PREVIOUS_VSYNC = "previous_vsync"
    private const val KEY_PREVIOUS_ARGS = "previous_args"
    private const val KEY_AUTO_HIDE_TOUCH = "auto_hide_touch"
    private const val KEY_SMALL_TOUCH = "small_touch"

    const val DEFAULT_RAM_MB = 1024
    const val LOW_END_RAM_MB = 768
    const val MAX_RAM_MB = 2048
    const val LOW_END_TARGET_HEIGHT = 540

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @JvmStatic
    fun isLowEndMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_LOW_END, false)

    @JvmStatic
    fun isFpsBoostEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_FPS_BOOST, false)

    @JvmStatic
    fun isDeviceUnderFourGb(context: Context): Boolean =
        Tools.getTotalDeviceMemory(context) < 4096

    @JvmStatic
    fun totalRamMb(context: Context): Int =
        Tools.getTotalDeviceMemory(context)

    @JvmStatic
    fun currentRam(context: Context): Int =
        LauncherPreferences.PREF_RAM_ALLOCATION.coerceIn(LOW_END_RAM_MB, MAX_RAM_MB)

    @JvmStatic
    fun saveRam(context: Context, value: Int) {
        val allocation = value.coerceIn(LOW_END_RAM_MB, MAX_RAM_MB)
        LauncherPreferences.PREF_RAM_ALLOCATION = allocation
        LauncherPreferences.DEFAULT_PREF?.edit()?.putInt("allocation", allocation)?.apply()
    }

    @JvmStatic
    fun lowEndResolutionRatio(context: Context): Int {
        val metrics: DisplayMetrics = context.resources.displayMetrics
        val shortSide = min(metrics.widthPixels, metrics.heightPixels).coerceAtLeast(1)
        return ceil(LOW_END_TARGET_HEIGHT * 100f / shortSide).toInt().coerceIn(25, 100)
    }

    @JvmStatic
    fun setLowEndMode(context: Context, enabled: Boolean) {
        val shared = prefs(context)
        val editor = shared.edit().putBoolean(KEY_LOW_END, enabled)

        if (enabled) {
            if (!shared.getBoolean(KEY_PREVIOUS_SAVED, false)) {
                editor
                    .putBoolean(KEY_PREVIOUS_SAVED, true)
                    .putInt(KEY_PREVIOUS_ALLOCATION, currentRam(context))
                    .putInt(
                        KEY_PREVIOUS_RESOLUTION,
                        LauncherPreferences.DEFAULT_PREF?.getInt("resolutionRatio", 100) ?: 100
                    )
                    .putString(
                        KEY_PREVIOUS_RENDERER,
                        LauncherPreferences.DEFAULT_PREF?.getString("renderer", "opengles2")
                    )
                    .putBoolean(
                        KEY_PREVIOUS_VSYNC,
                        LauncherPreferences.DEFAULT_PREF?.getBoolean("force_vsync", false) ?: false
                    )
                    .putString(
                        KEY_PREVIOUS_ARGS,
                        LauncherPreferences.DEFAULT_PREF?.getString("javaArgs", "") ?: ""
                    )
            }
            editor.apply()
            applyLowEndValues(context)
        } else {
            editor.apply()
            restorePreviousValues(context, shared)
        }
    }

    private fun applyLowEndValues(context: Context) {
        saveRam(context, LOW_END_RAM_MB)
        LauncherPreferences.PREF_SCALE_FACTOR = lowEndResolutionRatio(context) / 100f
        LauncherPreferences.PREF_RENDERER = "opengles2"
        LauncherPreferences.DEFAULT_PREF?.edit()
            ?.putInt("resolutionRatio", lowEndResolutionRatio(context))
            ?.putString("renderer", "opengles2")
            ?.putBoolean("force_vsync", false)
            ?.apply()
    }

    private fun restorePreviousValues(context: Context, shared: android.content.SharedPreferences) {
        if (!shared.getBoolean(KEY_PREVIOUS_SAVED, false)) {
            saveRam(context, DEFAULT_RAM_MB)
            return
        }
        val allocation = shared.getInt(KEY_PREVIOUS_ALLOCATION, DEFAULT_RAM_MB)
        val resolution = shared.getInt(KEY_PREVIOUS_RESOLUTION, 100)
        val renderer = shared.getString(KEY_PREVIOUS_RENDERER, "opengles2") ?: "opengles2"
        val vsync = shared.getBoolean(KEY_PREVIOUS_VSYNC, false)
        val args = shared.getString(KEY_PREVIOUS_ARGS, "") ?: ""
        LauncherPreferences.PREF_SCALE_FACTOR = resolution / 100f
        LauncherPreferences.PREF_RENDERER = renderer
        LauncherPreferences.DEFAULT_PREF?.edit()
            ?.putInt("resolutionRatio", resolution)
            ?.putString("renderer", renderer)
            ?.putBoolean("force_vsync", vsync)
            ?.putString("javaArgs", args)
            ?.apply()
        saveRam(context, allocation)
        shared.edit().remove(KEY_PREVIOUS_SAVED).apply()
    }

    @JvmStatic
    fun setFpsBoost(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_FPS_BOOST, enabled).apply()
        if (enabled) {
            // FPS Boost uses the upstream renderer and loader-compatible profile
            // while the recommended jars are fetched separately from Modrinth.
            LauncherPreferences.DEFAULT_PREF?.edit()
                ?.putString("renderer", "opengles2")
                ?.putBoolean("force_vsync", false)
                ?.apply()
            LauncherPreferences.PREF_RENDERER = "opengles2"
        }
    }

    @JvmStatic
    fun applyGamePerformance(context: Context, gameDirectory: File) {
        if (!isLowEndMode(context) && !isFpsBoostEnabled(context)) return
        try {
            MCOptionUtils.load(gameDirectory.absolutePath)
            MCOptionUtils.set("graphics", "fast")
            MCOptionUtils.set("renderDistance", "6")
            MCOptionUtils.set("simulationDistance", "5")
            MCOptionUtils.set("particles", "decreased")
            MCOptionUtils.set("entityShadows", "false")
            MCOptionUtils.set("clouds", "false")
            MCOptionUtils.set("biomeBlendRadius", "0")
            MCOptionUtils.save()
        } catch (throwable: Throwable) {
            Log.w(TAG, "Could not apply low-end Minecraft options", throwable)
        }
    }

    @JvmStatic
    fun enableTouchPreset(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_AUTO_HIDE_TOUCH, enabled)
            .putBoolean(KEY_SMALL_TOUCH, enabled)
            .apply()
        LauncherPreferences.PREF_BUTTONSIZE = if (enabled) 80f else 100f
        LauncherPreferences.DEFAULT_PREF?.edit()
            ?.putInt("buttonscale", if (enabled) 80 else 100)
            ?.apply()
    }

    @JvmStatic
    fun isTouchPresetEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_HIDE_TOUCH, false)

    @JvmStatic
    fun isTouchAutoHideEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_HIDE_TOUCH, false)

    @JvmStatic
    fun trimBackgroundApps(context: Context) {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return
        val ownPackage = context.packageName
        try {
            manager.runningAppProcesses
                ?.asSequence()
                ?.flatMap { it.pkgList.asSequence() }
                ?.filter { it != ownPackage }
                ?.distinct()
                ?.forEach { manager.killBackgroundProcesses(it) }
        } catch (throwable: Throwable) {
            Log.i(TAG, "Background process cleanup was not available", throwable)
        }
    }

    @JvmStatic
    fun initialize(context: Context) {
        val preferences = LauncherPreferences.DEFAULT_PREF ?: return
        if (!preferences.contains("allocation")) {
            saveRam(context, DEFAULT_RAM_MB)
        } else {
            saveRam(context, preferences.getInt("allocation", DEFAULT_RAM_MB))
        }
    }
}