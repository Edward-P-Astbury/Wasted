package android.system.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        android.system.time.trigger.broadcast.BroadcastReceiver().onReceive(context, intent)
    }
}