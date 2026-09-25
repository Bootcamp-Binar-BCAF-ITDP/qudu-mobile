package com.example.test2.core.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.io.File

/**
 * What is wrong with the device, if anything.
 *
 * Kept as separate flags rather than one boolean so the dialog can name the
 * problem: "turn off developer options" and "this device is rooted" need very
 * different things from the customer.
 */
data class IntegrityStatus(
    val rooted: Boolean = false,
    val developerOptions: Boolean = false,
    val usbDebugging: Boolean = false,
) {
    val isCompromised: Boolean get() = rooted || developerOptions || usbDebugging
}

/**
 * A best-effort check that the phone is not rooted and not in developer mode.
 *
 * **This is a deterrent, not a security boundary.** Every check here runs
 * inside the app, on the device being checked, so anything with root can hide
 * from all of it — Magisk DenyList does exactly that. It raises the cost of
 * casual tampering and satisfies the usual banking-app expectation; it does
 * not make the device trustworthy. Real assurance needs server-side
 * attestation (Play Integrity), which is a separate piece of work.
 */
object DeviceIntegrity {

    private val SU_PATHS = listOf(
        "/sbin/su",
        "/system/bin/su",
        "/system/xbin/su",
        "/system/sbin/su",
        "/vendor/bin/su",
        "/su/bin/su",
        "/data/local/su",
        "/data/local/bin/su",
        "/data/local/xbin/su",
        "/system/app/Superuser.apk",
        "/system/bin/failsafe/su",
    )

    private val ROOT_PACKAGES = listOf(
        "com.topjohnwu.magisk",
        "eu.chainfire.supersu",
        "com.koushikdutta.superuser",
        "com.noshufou.android.su",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.zachspong.temprootremovejb",
        "com.ramdroid.appquarantine",
    )

    fun check(context: Context): IntegrityStatus = IntegrityStatus(
        rooted = isRooted(context),
        developerOptions = isDeveloperOptionsEnabled(context),
        usbDebugging = isUsbDebuggingEnabled(context),
    )

    fun isDeveloperOptionsEnabled(context: Context): Boolean =
        Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0,
        ) == 1

    fun isUsbDebuggingEnabled(context: Context): Boolean =
        Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1

    fun isRooted(context: Context): Boolean =
        hasTestKeys() || hasSuBinary() || hasRootPackage(context) || canRunSu()

    /** A production ROM is signed with release-keys; a custom one usually is not. */
    private fun hasTestKeys(): Boolean =
        Build.TAGS?.contains("test-keys") == true

    private fun hasSuBinary(): Boolean =
        SU_PATHS.any { path -> runCatching { File(path).exists() }.getOrDefault(false) }

    private fun hasRootPackage(context: Context): Boolean {
        val manager = context.packageManager

        return ROOT_PACKAGES.any { name ->
            runCatching { manager.getPackageInfo(name, 0) }.isSuccess
        }
    }

    /**
     * `which su` succeeds only when su is on the path. Wrapped because the
     * lookup throws on devices that ship no shell binary at all, and a crash
     * here would lock everyone out rather than only the rooted.
     */
    private fun canRunSu(): Boolean = runCatching {
        val process = ProcessBuilder("which", "su").redirectErrorStream(true).start()
        val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
        process.waitFor()
        output.isNotEmpty()
    }.getOrDefault(false)
}
