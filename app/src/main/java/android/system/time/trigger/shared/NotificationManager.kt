package android.system.time.trigger.shared

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat

import android.system.time.R

class NotificationManager(private val ctx: Context) {
    companion object {
        const val CHANNEL_DEFAULT_ID = "default"
    }

    private val manager = NotificationManagerCompat.from(ctx)

    fun createNotificationChannels() {
        manager.createNotificationChannel(NotificationChannelCompat.Builder(
            CHANNEL_DEFAULT_ID,
            NotificationManagerCompat.IMPORTANCE_LOW,
        ).setName(ctx.getString(R.string.notification_channel_default_name)).setShowBadge(false).build())
    }
}