package android.system.time

import android.app.KeyguardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

import android.system.time.admin.DeviceAdminManager
import android.system.time.trigger.notification.NotificationListenerService
import android.system.time.trigger.panic.PanicConnectionActivity
import android.system.time.trigger.panic.PanicResponderActivity
import android.system.time.trigger.shared.ForegroundService
import android.system.time.trigger.shared.RestartReceiver
import android.system.time.trigger.shortcut.ShortcutActivity
import android.system.time.trigger.shortcut.ShortcutManager
import android.system.time.trigger.tile.TileService
import android.system.time.trigger.usb.UsbReceiver

class Utils(private val ctx: Context) {
    companion object {
        fun setFlag(key: Int, value: Int, enabled: Boolean) =
            when(enabled) {
                true -> key.or(value)
                false -> key.and(value.inv())
            }
    }

    private val prefs by lazy { Preferences.new(ctx) }

    fun setEnabled(enabled: Boolean) {
        val triggers = prefs.triggers
        setPanicKitEnabled(enabled && triggers.and(Trigger.PANIC_KIT.value) != 0)
        setTileEnabled(enabled && triggers.and(Trigger.TILE.value) != 0)
        setShortcutEnabled(enabled && triggers.and(Trigger.SHORTCUT.value) != 0)
        setBroadcastEnabled(enabled && triggers.and(Trigger.BROADCAST.value) != 0)
        setNotificationEnabled(enabled && triggers.and(Trigger.NOTIFICATION.value) != 0)
        updateForegroundRequiredEnabled()
        updateApplicationEnabled()
    }

    fun setPanicKitEnabled(enabled: Boolean) {
        setComponentEnabled(PanicConnectionActivity::class.java, enabled)
        setComponentEnabled(PanicResponderActivity::class.java, enabled)
    }

    fun setTileEnabled(enabled: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            setComponentEnabled(TileService::class.java, enabled)
    }

    fun setShortcutEnabled(enabled: Boolean) {
        val shortcut = ShortcutManager(ctx)
        if (!enabled) shortcut.remove()
        setComponentEnabled(ShortcutActivity::class.java, enabled)
        if (enabled) shortcut.push()
    }

    fun setBroadcastEnabled(enabled: Boolean) =
        setComponentEnabled(TriggerReceiver::class.java, enabled)

    fun setNotificationEnabled(enabled: Boolean) =
        setComponentEnabled(NotificationListenerService::class.java, enabled)

    fun updateApplicationEnabled() {
        val prefix = "${ctx.packageName}.trigger.application"
        val options = prefs.triggerApplicationOptions
        val enabled = prefs.isEnabled && prefs.triggers.and(Trigger.APPLICATION.value) != 0
        setComponentEnabled(
            "$prefix.SignalActivity",
            enabled && options.and(ApplicationOption.SIGNAL.value) != 0,
        )
        setComponentEnabled(
            "$prefix.TelegramActivity",
            enabled && options.and(ApplicationOption.TELEGRAM.value) != 0,
        )
        setComponentEnabled(
            "$prefix.ThreemaActivity",
            enabled && options.and(ApplicationOption.THREEMA.value) != 0,
        )
        setComponentEnabled(
            "$prefix.SessionActivity",
            enabled && options.and(ApplicationOption.SESSION.value) != 0,
        )
    }

    fun updateForegroundRequiredEnabled() {
        val enabled = prefs.isEnabled
        val triggers = prefs.triggers
        val isUSB = triggers.and(Trigger.USB.value) != 0
        val foregroundEnabled = enabled && (triggers.and(Trigger.LOCK.value) != 0 || triggers.and(Trigger.CELLULAR.value) != 0 || isUSB)
        setForegroundEnabled(foregroundEnabled)
        setComponentEnabled(RestartReceiver::class.java, foregroundEnabled)
        setComponentEnabled(UsbReceiver::class.java, enabled && isUSB)
    }

    private fun setForegroundEnabled(enabled: Boolean) =
        Intent(ctx.applicationContext, ForegroundService::class.java).also {
            if (enabled) ContextCompat.startForegroundService(ctx.applicationContext, it)
            else ctx.stopService(it)
        }

    private fun setComponentEnabled(cls: Class<*>, enabled: Boolean) =
        setComponentEnabled(ComponentName(ctx, cls), enabled)

    private fun setComponentEnabled(cls: String, enabled: Boolean) =
        setComponentEnabled(ComponentName(ctx, cls), enabled)

    private fun setComponentEnabled(componentName: ComponentName, enabled: Boolean) =
        ctx.packageManager.setComponentEnabledSetting(
            componentName,
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )

    fun fire(trigger: Trigger, safe: Boolean = true) {
        if (!prefs.isEnabled || prefs.triggers.and(trigger.value) == 0) return
        val admin = DeviceAdminManager(ctx)
        try {
            admin.lockNow()
            if (prefs.isWipeData && safe) admin.wipeData()
        } catch (exc: SecurityException) {}
        if (prefs.isRecastEnabled && safe) recast()
    }

    fun isDeviceLocked() = ctx.getSystemService(KeyguardManager::class.java).isDeviceLocked

    private fun recast() {
        val action = prefs.recastAction
        if (action.isEmpty()) return
        ctx.sendBroadcast(Intent(action).apply {
            val cls = prefs.recastReceiver.split('/')
            val packageName = cls.firstOrNull() ?: ""
            if (packageName.isNotEmpty()) {
                setPackage(packageName)
                if (cls.size == 2)
                    setClassName(
                        packageName,
                        "$packageName.${cls[1].trimStart('.')}",
                    )
            }
            addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
            val extraKey = prefs.recastExtraKey
            if (extraKey.isNotEmpty()) putExtra(extraKey, prefs.recastExtraValue)
        })
    }
}