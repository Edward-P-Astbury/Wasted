package android.system.time.trigger.shortcut

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.system.time.Trigger
import android.system.time.trigger.broadcast.BroadcastReceiver

class ShortcutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BroadcastReceiver.panic(this, intent, Trigger.SHORTCUT)
        finishAndRemoveTask()
    }
}